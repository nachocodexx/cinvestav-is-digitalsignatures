package mx.cinvestav.dsa
import cats.effect.IO
import cats.implicits._
import cats.effect.std.Console

import java.security.{KeyPair, KeyPairGenerator, PrivateKey, PublicKey, SecureRandom, Signature}

trait DigitalSignatureX[F[_]] {
  def keyPairGen(instance:String,keySize:Int,provider:Option[String]=None):F[KeyPair]
  def sign(instance:String,data:Array[Byte],privateKey:PrivateKey):F[Array[Byte]]
  def verify(instance:String, data:Array[Byte], signatureBytes:Array[Byte], publicKey:PublicKey):F[Boolean]
}
object DigitalSignatureXDSL {
  implicit val digitalSignatureX = new DigitalSignatureX[IO] {
    def getSignatureInstance(algorithm:String,provider:Option[String]=None): IO[Signature] = IO(
      Signature.getInstance(algorithm))
    override def sign(instance:String,data: Array[Byte],privateKey: PrivateKey): IO[Array[Byte]] = for {
      dsa       <- getSignatureInstance(instance)
      _         <- dsa.initSign(privateKey).pure[IO]
      _         <- dsa.update(data).pure[IO]
      signature <- dsa.sign().pure[IO]
    } yield signature

    override def verify(instance: String, data: Array[Byte], signBytes:Array[Byte], publicKey: PublicKey): IO[Boolean] =
      for {
     dsa   <- getSignatureInstance(instance)
     _     <- dsa.initVerify(publicKey).pure[IO]
      _    <- dsa.update(data).pure[IO]
      res  <- dsa.verify(signBytes).pure[IO]
    } yield res

    override def keyPairGen(instance:String,keySize:Int,provider:Option[String]): IO[KeyPair] = for {
//      kpInst       <- KeyPairGenerator.getInstance(instance,provider.getOrElse("SUN")).pure[IO]
      kpInst       <- KeyPairGenerator.getInstance(instance).pure[IO]
      secureRandom <- SecureRandom.getInstance("SHA1PRNG","SUN").pure[IO]
      _            <- kpInst.initialize(keySize,secureRandom).pure[IO]
      _            <- Console[IO].println(kpInst.getProvider)
      keyPair      <- kpInst.generateKeyPair().pure[IO]
    } yield keyPair

  }
}
