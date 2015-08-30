package io.ecumene.client

import org.zeromq._

final case class FunctionCall(
  ecmKey: String,
  args: ZMsg,
  callback: FunctionCallResult => Unit,
  timeoutAt: Long)

final object FunctionCall {
  def apply(
    ecmKey: String,
    args: Array[Byte],
    callback: FunctionCallResult => Unit,
    timeout: Long): FunctionCall = {
    val msg = new ZMsg
    msg.add(args)

    return FunctionCall(
      ecmKey,
      msg,
      callback,
      System.currentTimeMillis + timeout)
  }
}
