package mx.cinvestav.keystore
import cats.effect.IO
import cats.effect.std.Console
import cats.implicits._

import java.io.{FileInputStream, FileOutputStream}
import java.security.{KeyPair, KeyStore, PrivateKey}
import javax.crypto.SecretKey
import java.security.cert.X509Certificate

trait KeystoreX[F[_]]{
  def create(path:String, password:String,ksType:Option[String]=None):F[KeyStore]
  def load(path:String,password:String):F[KeyStore]
  def saveSymmetricKey(keystore:KeyStore,secretKey: SecretKey,alias:String,password:String):F[Boolean]
  def savePrivateKey(keyStore: KeyStore,privateKey:PrivateKey,cert:X509Certificate,alias:String,password:String):F[Boolean]
  def getCertificate(keyStore: KeyStore,alias:String):F[X509Certificate]
  def getPrivateKey(keyStore: KeyStore,alias:String,password:String):F[PrivateKey]
  def getKeyPair(keyStore: KeyStore,alias:String,password:String):F[KeyPair]
}

object KeystoreXDSL {
  implicit val keystoreXIO = new KeystoreX[IO] {
    override def create(path: String, password: String,ksType:Option[String]=None): IO[KeyStore] =
      for {
        ks   <- KeyStore.getInstance(ksType.getOrElse(KeyStore.getDefaultType)).pure[IO]
        _    <- ks.load(null,password.toCharArray).pure[IO]
        out  <- new FileOutputStream(path).pure[IO]
        _    <- ks.store(out,password.toCharArray).pure[IO]
        _    <- Console[IO].println(ks)
      } yield ks

    override def load(path: String, password: String): IO[KeyStore] = for {
      ks  <- KeyStore.getInstance(KeyStore.getDefaultType).pure[IO]
      in  <- new FileInputStream(path).pure[IO]
      _   <- ks.load(in,password.toCharArray).pure[IO]
    } yield ks

    override def saveSymmetricKey(keystore: KeyStore, sk:SecretKey, alias: String, password: String): IO[Boolean] = for {
      skEntry         <- IO(new KeyStore.SecretKeyEntry(sk))
      protectionParam <-IO(new KeyStore.PasswordProtection(password.toCharArray))
      _               <- keystore.setEntry(alias,skEntry,protectionParam).pure[IO]
    } yield true

    override def savePrivateKey(keyStore: KeyStore, privateKey: PrivateKey,certificate: X509Certificate, alias: String, password: String)
    : IO[Boolean] = for {
//      chain <- IO(new X509Certificate[2])
      _     <- keyStore.setKeyEntry(alias,privateKey,password.toCharArray,Array(certificate)).pure[IO]
    } yield true

    override def getCertificate(keyStore: KeyStore, alias: String): IO[X509Certificate] = for {
    cert <- keyStore.getCertificate(alias).asInstanceOf[X509Certificate].pure[IO]
    } yield cert

    override def getPrivateKey(keyStore: KeyStore, alias: String, password: String): IO[PrivateKey] = for {
      pk  <- keyStore.getKey(alias,password.toCharArray).asInstanceOf[PrivateKey].pure[IO]
    } yield pk

    override def getKeyPair(keyStore: KeyStore, alias: String, password: String): IO[KeyPair] = for {
      cert <- getCertificate(keyStore,alias)
      pk   <- cert.getPublicKey.pure[IO]
      sk   <- getPrivateKey(keyStore,alias,password)
      pair <- new KeyPair(pk,sk).pure[IO]
    } yield  pair
  }
}
