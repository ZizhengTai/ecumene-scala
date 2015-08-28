package io.ecumene.client

import org.zeromq._

case class FunctionCall(
  ecmKey: String,
  args: ZMsg,
  callback: FunctionCallResult => Unit,
  timeoutAt: Long)

object FunctionCall {
  def apply(
    ecmKey: String,
    buffer: Array[Byte],
    callback: FunctionCallResult => Unit,
    timeout: Long): FunctionCall = {
    val msg = new ZMsg
    msg.add(buffer)

    return FunctionCall(
      ecmKey,
      msg,
      callback,
      System.currentTimeMillis + timeout)
  }
}
