package io.ecumene.client

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.msgpack.core.MessagePack

import io.ecumene.core._

final class EcumeneFunction1[-T1: CanPack, R: CanUnpack](
  val ecmKey: String,
  var timeout: Long = 15000
) extends EcumeneFunction[R] with Function1[T1, R] {

  def apply(v1: T1): R = {
    Await.result(future(v1), Duration.Inf)
  }

  def future(v1: T1): Future[R] = {
    futureWithPacker { implicit packer =>
      packer packArrayHeader 1
      pack(v1)
    }
  }
}
