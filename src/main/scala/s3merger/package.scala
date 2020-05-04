import cats.effect.{ExitCase, IO}
import cats.kernel.Monoid
import fs2.Stream
import org.slf4j.Logger
import squants.information.{Bytes, Information}

package object s3merger {
  implicit val sumBytes: Monoid[Information] = Monoid.instance(Bytes(0), _ + _)
  val log: Logger = org.slf4j.LoggerFactory.getLogger("s3merger")

  def withLog(label: String): Stream[IO, Unit] =
    Stream.bracketCase {
      IO.delay(log.info(s"start> $label"))
    } { (_, exitCase) =>
      exitCase match {
        case ExitCase.Error(e) => IO.delay(log.info(s"fail> $label: $e"))
        case _ => IO.delay(log.info(s"stop> $label"))
      }
    }
}
