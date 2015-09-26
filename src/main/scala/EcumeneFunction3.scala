package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

import io.ecumene.core._

final class EcumeneFunction3[-T1: CanPack, -T2: CanPack, -T3: CanPack, R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function3[T1, T2, T3, R] {

  def apply(v1: T1, v2: T2, v3: T3): R = {
    Await.result(future(v1, v2, v3), Duration.Inf)
  }

  def future(v1: T1, v2: T2, v3: T3): Future[R] = {
    futureWithPacker { implicit packer =>
      packer packArrayHeader 3
      pack(v1)
      pack(v2)
      pack(v3)
    }
  }
}
