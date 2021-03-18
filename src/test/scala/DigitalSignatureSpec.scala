import cats.effect.IO
import cats.implicits._
import cats.effect.std.Console
import cats.effect.unsafe.implicits._
import org.scalatest.funsuite.AnyFunSuite
import mx.cinvestav.dsa.DigitalSignatureXDSL._
import mx.cinvestav.keystore.KeystoreXDSL._
import mx.cinvestav.utils.CodingXDSL._

import java.util.Base64

class DigitalSignatureSpec extends AnyFunSuite{
  private val TARGET_PATH   = "/home/nacho/Programming/Scala/cinvestav-is-dsdct/target"
  private val KEYSTORE_FILENAME = "store.jks"
  private val KEYSTORE_PATH = s"$TARGET_PATH/keystore/$KEYSTORE_FILENAME"
  test("Get certificate"){
    val app = keystoreXIO
      .load(KEYSTORE_PATH,"changeit")
      .flatMap(keystoreXIO.getCertificate(_,"mycert"))
      .flatTap(x=>Console[IO].println(x.getPublicKey))
    app.unsafeRunSync()
  }
  test("Sign data"){
    val text ="HOLA".getBytes
    val app = digitalSignatureX
      .keyPairGen("RSA",1024)
      .flatMap{ keyPair=>
        digitalSignatureX
          .sign("SHA1withRSA",text,keyPair.getPrivate)
          .flatMap(x=>
            digitalSignatureX
            .verify("SHA1withRSA",text,x,keyPair.getPublic)
            .flatTap(x=>Console[IO].println(s"Is valid?: $x"))
          )
      }
    app.unsafeRunSync()
//    val app = digitalSignatureX.sign("SHA1withDSA",text,)
    assert(true)
  }

}
