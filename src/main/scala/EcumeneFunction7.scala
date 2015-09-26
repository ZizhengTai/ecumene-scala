package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

import io.ecumene.core._

final class EcumeneFunction7[-T1: CanPack, -T2: CanPack, -T3: CanPack, -T4: CanPack, -T5: CanPack, -T6: CanPack, -T7: CanPack, R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function7[T1, T2, T3, T4, T5, T6, T7, R] {

  def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7): R = {
    Await.result(future(v1, v2, v3, v4, v5, v6, v7), Duration.Inf)
  }

  def future(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6, v7: T7): Future[R] = {
    futureWithPacker { implicit packer =>
      packer packArrayHeader 7
      pack(v1)
      pack(v2)
      pack(v3)
      pack(v4)
      pack(v5)
      pack(v6)
      pack(v7)
    }
  }
}
