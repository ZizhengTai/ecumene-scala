package io.ecumene.worker

import scala.collection.mutable.{ HashMap, MultiMap, Set }
import java.nio.ByteBuffer
import org.zeromq._

object HeartbeatService {

  val HEARTBEAT_PROTOCOL_VERSION: Short = 0
  val HEARTBEAT_INTERVAL = 5000
  val versionBytes =
    ByteBuffer.allocate(2).putShort(HEARTBEAT_PROTOCOL_VERSION).array

  private val context = new ZContext

  private val actorPipe: ZMQ.Socket = {
    val attached = new ZThread.IAttachedRunnable {
      def run(args: Array[Object], ctx: ZContext, pipe: ZMQ.Socket) = {
        
        Runtime.getRuntime addShutdownHook new Thread {
          override def run() = term()
        }

        val ecm = ctx createSocket ZMQ.PUSH
        ecm connect "tcp://ecumene.io:23331"

        val poller = new ZMQ.Poller(1)
        val pipeIdx = poller register (pipe, ZMQ.Poller.POLLIN)

        var beatAt = System.currentTimeMillis

        var terminated = false
        while (!terminated) {
          poller poll 100

          if (poller pollin pipeIdx) {
            // Internal command
            if (pipe.recvStr() == "$TERM") {
              terminated = true
            }
          }

          workers.synchronized {

            // Unregister
            for {
              (ecmKey, endpointSet) <- unreg
              endpoint <- endpointSet
            } {
              require(ecm send (versionBytes, ZMQ.SNDMORE))
              require(ecm send ("U", ZMQ.SNDMORE))
              require(ecm send (ecmKey, ZMQ.SNDMORE))
              require(ecm send endpoint)
            }
            unreg.clear()

            val now = System.currentTimeMillis
            if (now >= beatAt) {
              // Heartbeat

              println("Beat!")

              for {
                (ecmKey, endpointSet) <- workers
                endpoint <- endpointSet
              } {
                require(ecm send (versionBytes, ZMQ.SNDMORE))
                require(ecm send ("", ZMQ.SNDMORE))
                require(ecm send (ecmKey, ZMQ.SNDMORE))
                require(ecm send endpoint)
              }

              beatAt = System.currentTimeMillis + HEARTBEAT_INTERVAL
            }
          }
        }

        println("Cleaning up heartbeat service...")
      }
    }
    ZThread fork (context, attached)
  }

  private val workers = new HashMap[String, Set[String]] with MultiMap[String, String]
  private val unreg = new HashMap[String, Set[String]] with MultiMap[String, String]

  def term(): Unit = {
    actorPipe.synchronized {
      actorPipe send "$TERM"
    }
  }

  def registerWorker(ecmKey: String, endpoint: String): Unit = {
    workers.synchronized {
      workers addBinding (ecmKey, endpoint)
    }
  }

  def unregisterWorker(ecmKey: String, endpoint: String): Unit = {
    workers.synchronized {
      workers removeBinding (ecmKey, endpoint)
      unreg addBinding (ecmKey, endpoint)
    }
  }
}
