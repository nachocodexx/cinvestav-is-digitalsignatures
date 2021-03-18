package mx.cinvestav.utils
import cats.effect.IO
import cats.implicits._

import java.util.Base64

trait CodingX[F[_]]{
  def base64Encode(x:Array[Byte]):F[String]
  def base64Decode(x:String):F[Array[Byte]]
}
object CodingXDSL{
  implicit val codingX = new CodingX[IO] {
    override def base64Encode(x: Array[Byte]): IO[String] = IO(
      Base64.getEncoder.encodeToString(x)
    )

    override def base64Decode(x: String): IO[Array[Byte]] = IO(
      Base64.getDecoder.decode(x)
    )
  }
}
