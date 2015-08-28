package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

class EcumeneFunction2[-T1: CanPack, -T2: CanPack, R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function2[T1, T2, R] {

  def apply(v1: T1, v2: T2): R = {
    Await.result(future(v1, v2), Duration.Inf)
  }

  def future(v1: T1, v2: T2): Future[R] = {
    import EcumeneFunction._

    futureWithPacker { packer =>
      implicit val pk = packer

      packer packArrayHeader 2
      pack(v1)
      pack(v2)
    }
  }
}
