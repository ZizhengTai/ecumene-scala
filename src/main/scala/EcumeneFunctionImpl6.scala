package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

import io.ecumene.core._
import io.ecumene.core.Implicits._

final class EcumeneFunctionImpl6[-T1: CanUnpack, -T2: CanUnpack, -T3: CanUnpack, -T4: CanUnpack, -T5: CanUnpack, -T6: CanUnpack, +R: CanPack](
  ecmKey: String,
  localEndpoint: String,
  publicEndpoint: String,
  val func: (T1, T2, T3, T4, T5, T6) => R
) extends EcumeneFunctionImpl(
  ecmKey,
  localEndpoint,
  publicEndpoint,
  { (unpacker, packer) =>
    implicit val unpk = unpacker
    if (unpacker.unpackArrayHeader != 6) {
      throw new IllegalArgumentException
    }
    val r = func(unpack[T1].get, unpack[T2].get, unpack[T3].get, unpack[T4].get, unpack[T5].get, unpack[T6].get)

    implicit val pk = packer
    pack(r)
  }
) with Function6[T1, T2, T3, T4, T5, T6, R] {
  def apply(v1: T1, v2: T2, v3: T3, v4: T4, v5: T5, v6: T6): R = func(v1, v2, v3, v4, v5, v6)
}
