package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

import io.ecumene.core._
import io.ecumene.core.Implicits._

final class EcumeneFunctionImpl3[-T1: CanUnpack, -T2: CanUnpack, -T3: CanUnpack, +R: CanPack](
  val func: (T1, T2, T3) => R,
  ecmKey: String,
  localEndpoint: String,
  publicEndpoint: String
) extends EcumeneFunctionImpl(
  ecmKey,
  localEndpoint,
  publicEndpoint,
  { (unpacker, packer) =>
    implicit val unpk = unpacker
    if (unpacker.unpackArrayHeader != 3) {
      throw new IllegalArgumentException
    }
    val r = func(unpack[T1].get, unpack[T2].get, unpack[T3].get)

    implicit val pk = packer
    pack(r)
  }
) with Function3[T1, T2, T3, R] {
  def apply(v1: T1, v2: T2, v3: T3): R = func(v1, v2, v3)
}
