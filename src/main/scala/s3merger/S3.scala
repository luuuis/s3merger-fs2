package s3merger

import java.net.URI

import blobstore.Path
import blobstore.s3.S3Store
import cats.effect.{Blocker, Concurrent, ContextShift, IO}
import fs2.{Pipe, Stream}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import squants.information.Information
import squants.information.InformationConversions._

/**
 * For demo, read from s3_read local dir and write to /dev/null. :)
 */
class S3(client: S3AsyncClient, chunkSize: Information)(implicit F: Concurrent[IO], CS: ContextShift[IO]) extends S3Lib {
  val s3 = new S3Store[IO](client)

  override def listFiles(bucket: S3Bucket): Stream[IO, S3File] =
    withLog(s"list $bucket") >> {
      for {
        path <- s3.list(toPath(bucket))
        size <- path.size.map(Stream.emit).getOrElse(Stream.raiseError[IO](new IllegalStateException(s"No size: $path")))
      } yield S3File(bucket, path.key, size.bytes)
    }

  override def readFile(readFrom: S3File): Stream[IO, Byte] =
    withLog(s"read $readFrom") >> s3.get(toPath(readFrom), chunkSize.toBytes.toInt)

  override def writeFile(writeTo: S3File): Pipe[IO, Byte, Unit] ={
    val uploader = s3.put(toPath(writeTo))

    inBytes => withLog(s"write $writeTo") >> inBytes.through(uploader).drain
  }

  private def toPath(bucket: S3Bucket) = Path(s"${bucket.name}")
  private def toPath(file: S3File) = Path(s"${file.bucket.name}/${file.key}")
}

object S3 {
  def localhost(port: Int, chunkSize: Information)(implicit F: Concurrent[IO], CS: ContextShift[IO]): S3 = {
    val client = S3AsyncClient.builder()
      .endpointOverride(URI.create(s"http://localhost:$port"))
      .region(Region.EU_WEST_1)
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("minioadmin", "minioadmin")))
      .build()

    new S3(client, chunkSize)
  }
}
