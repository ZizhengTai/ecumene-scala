package io.ecumene.client

import scala.concurrent.{ Future, Promise }
import scala.util.{ Try, Success, Failure }
import java.net.SocketTimeoutException
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import org.msgpack.core.{ MessagePack, MessagePacker, MessageUnpacker }

import io.ecumene.core._

abstract class EcumeneFunction[R: CanUnpack] {
  val ecmKey: String
  var timeout: Long

  private def handleResult(p: Promise[R])(result: FunctionCallResult): Unit = {
    import FunctionCallResult.Status

    result.status match {
      case Status.Success =>
        implicit val unpacker =
          MessagePack.newDefaultUnpacker(new ByteArrayInputStream(result.data))

        unpack[R] match {
          case Success(r) => p success r
          case Failure(e) => p failure e
        }
        unpacker.close()
      case Status.UndefinedReference =>
        p failure UndefinedReferenceException(s"undefined reference to $ecmKey")
      case Status.InvalidArgument =>
        p failure new IllegalArgumentException(s"illegal argument to $ecmKey")
      case Status.NetworkError =>
        p failure new SocketTimeoutException(s"failed to call $ecmKey due to network error")
      case Status.UnknownError =>
        p failure new RuntimeException(s"unknown error when calling $ecmKey")
    }
  }

  protected def futureWithPacker(packArgs: MessagePacker => Unit): Future[R] = {
    val out = new ByteArrayOutputStream
    implicit val packer = MessagePack.newDefaultPacker(out)

    packArgs(packer)
    packer.close()

    val p = Promise[R]()
    ClientAgent send FunctionCall(ecmKey, out.toByteArray, handleResult(p) _, timeout)
    return p.future
  }
}
