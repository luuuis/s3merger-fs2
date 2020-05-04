package s3merger

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2.{Pipe, Stream}
import squants.information.Information
import squants.information.InformationConversions._

case class S3Bucket(name: String)
case class S3File(bucket: S3Bucket, key: String, size: Information)

trait S3Lib {
  def listFiles(bucket: S3Bucket): Stream[IO, S3File]
  def readFile(readFrom: S3File): Stream[IO, Byte]
  def writeFile(writeTo: S3File): Pipe[IO, Byte, Unit]
}

object S3Merger extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    s3merger(
      s3 = S3.localhost(port = 9000, chunkSize = 2.megabytes),
      inBucket = S3Bucket("fragments"),
      outBucket = S3Bucket("rollup")).compile.drain.as(ExitCode.Success)

  def s3merger(s3: S3Lib, inBucket: S3Bucket, outBucket: S3Bucket): Stream[IO, Unit] = {
    val outFile = S3File(outBucket, "one_big_chunk", size=0.bytes)
    s3.listFiles(inBucket)
      .flatMap(s3.readFile)
      .through(s3.writeFile(outFile))
  }
}
