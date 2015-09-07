package io.ecumene.worker

import scala.util.{ Try, Success, Failure }
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.net.SocketTimeoutException
import org.zeromq._
import org.msgpack.core.{ MessagePack, MessagePacker, MessageUnpacker, MessageTypeException }

import io.ecumene.core._

final class WorkerAgent(
  val ecmKey: String,
  val localEndpoint: String,
  val publicEndpoint: String,
  val callback: (MessageUnpacker, MessagePacker) => Unit
) {

  private val context = new ZContext

  private val actorPipe: ZMQ.Socket = {
    val attached = new ZThread.IAttachedRunnable {
      def run(args: Array[Object], ctx: ZContext, pipe: ZMQ.Socket) = {

        Runtime.getRuntime addShutdownHook new Thread {
          override def run() = term()
        }

        val worker = ctx createSocket ZMQ.ROUTER
        worker bind localEndpoint

        val poller = new ZMQ.Poller(2)
        val pipeIdx = poller register (pipe, ZMQ.Poller.POLLIN)
        val workerIdx = poller register (worker, ZMQ.Poller.POLLIN)

        HeartbeatService registerWorker (ecmKey, publicEndpoint)

        var terminated = false
        while (!terminated) {
          poller.poll()

          if (poller pollin pipeIdx) {
            // Internal command
            if (pipe.recvStr() == "$TERM") {
              terminated = true
            }
          } else if (poller pollin workerIdx) {
            val msg = ZMsg recvMsg worker
            require(msg.size == 3)

            val identity = msg.pollFirst()
            val id = msg.pollFirst()
            val args = msg.pollFirst().getData

            val unpacker =
              MessagePack.newDefaultUnpacker(new ByteArrayInputStream(args))
            val out = new ByteArrayOutputStream
            val packer = MessagePack.newDefaultPacker(out)

            Try(callback(unpacker, packer)) match {
              case Success(_) =>
                packer.close()

                require(identity send (worker, ZFrame.MORE))
                require(id send (worker, ZFrame.MORE))
                require(worker send ("", ZMQ.SNDMORE))
                require(worker send out.toByteArray)
              case Failure(e) =>
                def respond(status: String): Unit = {
                  require(identity send (worker, ZFrame.MORE))
                  require(id send (worker, ZFrame.MORE))
                  require(worker send (status, ZMQ.SNDMORE))
                  require(worker send "")
                }
                respond(e match {
                  case _: MessageTypeException => "I"
                  case UndefinedReferenceException(_) => "U"
                  case _: IllegalArgumentException => "I"
                  case _: SocketTimeoutException => "N"
                  case _: RuntimeException => "?"
                })
            }
          }
        }

        HeartbeatService unregisterWorker (ecmKey, publicEndpoint)

        println("Cleaning up worker agent...")
      }
    }
    ZThread fork (context, attached)
  }

  def term(): Unit = {
    actorPipe.synchronized {
      actorPipe send "$TERM"
    }
  }
}
