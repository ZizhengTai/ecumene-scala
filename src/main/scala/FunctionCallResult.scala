package io.ecumene.client

import org.zeromq.ZFrame

object FunctionCallResult {

  object Status extends Enumeration {
    type Status = Value
    val Success, InvalidArgument, UndefinedReference, NetworkError, BadCast, UnknownError = Value
  }

  def apply(status: ZFrame, result: ZFrame): FunctionCallResult = {
    import Status._

    FunctionCallResult(
      if (status streq "") Success
      else if (status streq "I") InvalidArgument
      else if (status streq "U") UndefinedReference
      else if (status streq "N") NetworkError
      else UnknownError,
      result.getData)
  }
}

case class FunctionCallResult(
  status: FunctionCallResult.Status.Value,
  data: Array[Byte])
