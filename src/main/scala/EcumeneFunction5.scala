package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

class EcumeneFunction5[-T1: CanPack, -T2: CanPack, -T3: CanPack, -T4: CanPack, -T5: CanPack, R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function5[T1, T2, T3, T4, T5, R] {

  def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5): R = {
    Await.result(future(v1, v2, v3, v4, v5), Duration.Inf)
  }

  def future(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5): Future[R] = {
    import EcumeneFunction._

    futureWithPacker { packer =>
      implicit val pk = packer

      packer packArrayHeader 5
      pack(v1)
      pack(v2)
      pack(v3)
      pack(v4)
      pack(v5)
    }
  }
}
