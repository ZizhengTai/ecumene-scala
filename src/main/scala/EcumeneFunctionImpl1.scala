package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

import io.ecumene.core._
import io.ecumene.core.Implicits._

final class EcumeneFunctionImpl1[-T1: CanUnpack, +R: CanPack](
  ecmKey: String,
  localEndpoint: String,
  publicEndpoint: String,
  val func: (T1) => R
) extends EcumeneFunctionImpl(
  ecmKey,
  localEndpoint,
  publicEndpoint,
  { (unpacker, packer) =>
    implicit val unpk = unpacker
    if (unpacker.unpackArrayHeader != 1) {
      throw new IllegalArgumentException
    }
    val r = func(unpack[T1].get)

    implicit val pk = packer
    pack(r)
  }
) with Function1[T1, R] {
  def apply(v1: T1): R = func(v1)
}
