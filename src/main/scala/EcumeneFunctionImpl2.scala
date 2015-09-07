package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

import io.ecumene.core._
import io.ecumene.core.Implicits._

final class EcumeneFunctionImpl2[-T1: CanUnpack, -T2: CanUnpack, +R: CanPack](
  ecmKey: String,
  localEndpoint: String,
  publicEndpoint: String,
  val func: (T1, T2) => R
) extends EcumeneFunctionImpl(
  ecmKey,
  localEndpoint,
  publicEndpoint,
  { (unpacker, packer) =>
    implicit val unpk = unpacker
    if (unpacker.unpackArrayHeader != 2) {
      throw new IllegalArgumentException
    }
    val r = func(unpack[T1].get, unpack[T2].get)

    implicit val pk = packer
    pack(r)
  }
) with Function2[T1, T2, R] {
  def apply(v1: T1, v2: T2): R = func(v1, v2)
}
