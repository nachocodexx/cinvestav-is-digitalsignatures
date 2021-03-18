package mx.cinvestav.cipher

import cats.effect.IO
import cats.implicits._

import javax.crypto.Cipher

trait CipherX [F[_]]{
  def encrypt(instance:String,data:Array[Byte]):F[Array[Byte]]
}
object CipherXDSL {
  implicit val cipherX = new CipherX[IO] {
    override def encrypt(instance:String,data: Array[Byte]): IO[Array[Byte]] = for {
      cipher <- IO(Cipher.getInstance(instance))

    } yield ???
  }
}
