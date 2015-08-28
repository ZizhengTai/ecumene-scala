package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

abstract class EcumeneFunctionImpl(
  val ecmKey: String,
  val localEndpoint: String,
  val publicEndpoint: String,
  val callback: (MessageUnpacker, MessagePacker) => Unit
) {
  private val agent = new WorkerAgent(
    ecmKey,
    localEndpoint,
    publicEndpoint,
    callback)
}
