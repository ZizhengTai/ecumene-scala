package io.ecumene.client

import scala.collection.mutable.Map
import java.nio.ByteBuffer
import org.zeromq._

final object ClientAgent {

  val PROTOCOL_VERSION: Short = 0
  val versionBytes =
    ByteBuffer.allocate(2).putShort(PROTOCOL_VERSION).array

  private val context = new ZContext
  private val socks = Map.empty[String, (ZMQ.Socket, Int)]
  private var sequence = 0
  private val calls = Map.empty[Long, FunctionCall]
    
  private val actorPipe: ZMQ.Socket = {
    val attached = new ZThread.IAttachedRunnable {
      def run(args: Array[Object], ctx: ZContext, pipe: ZMQ.Socket) = {

        Runtime.getRuntime addShutdownHook new Thread {
          override def run() = term()
        }

        val ecm = context createSocket ZMQ.DEALER
        ecm connect "tcp://ecumene.io:23332"

        val poller = new ZMQ.Poller(2)
        val pipeIdx = poller register (pipe, ZMQ.Poller.POLLIN)
        val ecmIdx = poller register (ecm, ZMQ.Poller.POLLIN)

        var terminated = false
        while (!terminated) {
          poller poll 1000

          if (poller pollin pipeIdx) {
            // Internal command

            val msg = ZMsg recvMsg pipe
            val cmd = msg.pollFirst()

            if (cmd streq "$TERM") {
              terminated = true
            } else if (cmd streq "$SEND" ) {
              require(msg.size == 1)

              val id = new String(msg.pollFirst().getData)

              calls.synchronized {
                calls.get(id.toLong) foreach { call =>

                  socks.get(call.ecmKey) match {
                    case Some((worker, idx)) =>
                      // Use existing worker socket

                      require(worker send (id, ZMQ.SNDMORE))
                      require(call.args send worker)

                    case None =>
                      // Ask Ecumene for new worker

                      require(ecm send (versionBytes, ZMQ.SNDMORE))
                      require(ecm send (id, ZMQ.SNDMORE))
                      require(ecm send call.ecmKey)
                  }
                }
              }
            }
          } else if (poller pollin ecmIdx) {
            // Worker assignment from Ecumene

            val msg = ZMsg recvMsg ecm
            require(msg.size == 4)

            val id = new String(msg.pollFirst().getData)
            val ecmKey = new String(msg.pollFirst().getData)
            val status = new String(msg.pollFirst().getData)
            val endpoint = new String(msg.pollFirst().getData)

            status match {
              case "" =>
                // Success

                if (!socks.contains(ecmKey)) {
                  val worker = ctx createSocket ZMQ.DEALER
                  worker connect endpoint

                  val idx = poller register (worker, ZMQ.Poller.POLLIN)

                  socks(ecmKey) = (worker, idx)

                  actorPipe.synchronized {
                    actorPipe sendMore "$SEND"
                    actorPipe send id
                  }
                }
              case "U" =>
                // Undefined reference

                calls.synchronized {
                  calls.get(id.toLong) foreach { call =>
                    calls -= id.toLong
                    call.callback(FunctionCallResult(new ZFrame("U"), new ZFrame("")))
                  }
                }
            }
          } else {
            // Response from some worker

            socks.values foreach { case (worker, idx) =>
              if (poller pollin idx) {
                val msg = ZMsg recvMsg worker
                require(msg.size == 3)

                val id = new String(msg.pollFirst().getData)
                val statusFrame = msg.pollFirst()
                val resultFrame = msg.pollFirst()

                calls.synchronized {
                  calls.get(id.toLong) foreach { call =>
                    calls -= id.toLong
                    call.callback(FunctionCallResult(statusFrame, resultFrame))
                  }
                }
              }
            }
          }

          // Check timeout
          val now = System.currentTimeMillis
          calls.synchronized {
            calls foreach { case (id, call) =>
              if (now >= call.timeoutAt) {
                calls -= id
                call.callback(FunctionCallResult(new ZFrame("N"), new ZFrame("")))
              }
            }
          }
        }

        ecm.close()
        println("Cleaning up client agent...")
      }
    }
    ZThread fork (context, attached)
  }

  def term(): Unit = {
    actorPipe.synchronized {
      actorPipe send "$TERM"
    }
  }

  def send(call: FunctionCall): Unit = {
    calls.synchronized {
      val id = sequence.toString
      calls(sequence) = call
      sequence += 1

      actorPipe.synchronized {
        actorPipe sendMore "$SEND"
        actorPipe send id
      }
    }
  }
}
