import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.std.Console

object Application extends IOApp{
  override def run(args: List[String]): IO[ExitCode] =
    Console[IO].println("HOLLA").as(ExitCode.Success)
}
