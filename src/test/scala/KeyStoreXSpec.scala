import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import mx.cinvestav.keystore.KeystoreXDSL._

class KeyStoreXSpec extends AnyFunSuite{
  private val TARGET_PATH   = "/home/nacho/Programming/Scala/cinvestav-is-dsdct/target"
  private val KEYSTORE_FILENAME = "store.jceks"
  private val KEYSTORE_PATH = s"$TARGET_PATH/keystore/$KEYSTORE_FILENAME"
  test("Create keystore"){
    val keyStore = keystoreXIO
      .create(KEYSTORE_PATH,"changeit",Option("jceks"))
    keyStore.unsafeRunSync()
    assert(true)
  }
  test("Load a keystore"){
    val keyStore = keystoreXIO
      .load(KEYSTORE_PATH,"changeit")
      .map(_.getType)
   val _type =  keyStore.unsafeRunSync()
//    println(k)
    assert(_type.equals("jks"))
  }

}
