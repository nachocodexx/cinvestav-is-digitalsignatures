package mx.cinvestav
import cats.implicits._
import fs2.Stream
import fs2.io.file.Files
import fs2.text
import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import mx.cinvestav.dsa.DigitalSignatureX
import mx.cinvestav.dsa.DigitalSignatureXDSL._
import mx.cinvestav.keystore.KeystoreX
import mx.cinvestav.keystore.KeystoreXDSL._

import java.nio.file.{Path, Paths}
import java.nio.file.Files.readAllBytes

object Application extends IOApp{
  private val TARGET_PATH          = "/home/nacho/Programming/Scala/cinvestav-is-dsdct/target"
  private val DATA_PATH            = s"$TARGET_PATH/data"
  private val KEYSTORE_FILENAME    = "store.jks"
  private val KEYSTORE_PATH        = s"$TARGET_PATH/keystore/$KEYSTORE_FILENAME"
  private val SIGNATURE_PATH       = s"$TARGET_PATH/signatures"
  private val SIGNATURE_ALGORITHM  = "SHA256withRSA"
  private val KEYSTORE_PASSWORD    = "changeit"

  def programSign(path:String,algorithm:String)(implicit DSA:DigitalSignatureX[IO],KS:KeystoreX[IO]) =
    Stream
      .eval(KS.load(KEYSTORE_PATH,KEYSTORE_PASSWORD))
      .evalMap(KS.getKeyPair(_,"mycert",KEYSTORE_PASSWORD))
      .flatMap{keyPair=>

        Files[IO]
          .directoryStream(Paths.get(path))
          .debug(x=>s"Sign ${x.getFileName}")
          .flatMap{_path=>
            val readAllBytesIO=readAllBytes(_path).pure[IO].flatMap(DSA.sign(algorithm,_,keyPair.getPrivate))
            Stream
              .eval(readAllBytesIO)
              .flatMap(Stream.emits(_))
              .through(Files[IO].writeAll(Paths.get(s"$SIGNATURE_PATH/${_path.getFileName}.ds")))
          }
      }
  def programVerify(path:String,algorithm:String)(implicit DSA:DigitalSignatureX[IO],KS:KeystoreX[IO])=
    Stream
      .eval(KS.load(KEYSTORE_PATH,KEYSTORE_PASSWORD))
    .evalMap(KS.getKeyPair(_,"mycert",KEYSTORE_PASSWORD))
    .flatMap{keyPair=>
      val getSignatures     = Files[IO].directoryStream(Paths.get(path))
        .fold(List.empty[Path])(_:+_)
        .map(_.sorted)
        .flatMap(Stream.emits(_))
      val getData           = Files[IO].directoryStream(Paths.get(DATA_PATH))
        .fold(List.empty[Path])((x,y)=>x:+y)
        .map(_.sorted)
        .flatMap(Stream.emits(_))
      val signaturesAndData =getData
        .zip(getSignatures)
      signaturesAndData.flatMap{
        case (data,signature)=>
          val dataBytes      = readAllBytes(data)
          val signatureBytes =  readAllBytes(signature)
          Stream
            .eval(DSA.verify(SIGNATURE_ALGORITHM,dataBytes,signatureBytes,keyPair.getPublic))
          .debug(x=>s"${signature.getFileName} is the signature of ${data.getFileName}? ${if(x) "yes" else "no"}")
        }
      }
//      .debug(x=>s"$x")


  override def run(args: List[String]): IO[ExitCode] ={
//    programSign(DATA_PATH,SIGNATURE_ALGORITHM)
    programVerify(SIGNATURE_PATH,SIGNATURE_ALGORITHM)
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
