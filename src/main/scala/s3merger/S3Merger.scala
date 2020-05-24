package s3merger

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2.{Chunk, Pipe, Stream}
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
      outBucket = S3Bucket("rollup"),
      maxConcurrentUploads = 4,
    ).compile.drain.as(ExitCode.Success)

  def s3merger(s3: S3Lib, inBucket: S3Bucket, outBucket: S3Bucket, maxConcurrentUploads: Int): Stream[IO, Unit] =
    s3.listFiles(inBucket)
      .through(chunkMinimumSize(32.megabytes))
      .map({ case (slot, chunk) => (chunk, S3File(outBucket, s"chunked_$slot", chunk.map(_.size).fold)) })
      .balanceThrough(1, maxConcurrentUploads)(_.flatMap({ case (inFiles, outFile) => Stream.chunk(inFiles)
        .covary[IO]
        .flatMap(s3.readFile)
        .through(s3.writeFile(outFile))
      }))

  def chunkMinimumSize(chunkAtLeast: Information): Pipe[IO, S3File, (Int, Chunk[S3File])] =
    s3Files => s3Files
      .mapAccumulate((0, 0.bytes))({ case ((slot, seenSize), s3File) =>
        if (seenSize >= chunkAtLeast)
          ((slot + 1, s3File.size), s3File) // next slot
        else
          ((slot, s3File.size + seenSize), s3File)
      })
      .groupAdjacentBy({ case ((slot, seenSize), s3File) => slot })
      .map({ case (slot, chunk) => (slot, chunk.map(_._2)) })
}
