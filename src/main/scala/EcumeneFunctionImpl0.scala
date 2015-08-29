package io.ecumene.worker

import org.msgpack.core.{ MessagePacker, MessageUnpacker }

import io.ecumene.core._
import io.ecumene.core.Implicits._

final class EcumeneFunctionImpl0[+R: CanPack](
  val func: () => R,
  ecmKey: String,
  localEndpoint: String,
  publicEndpoint: String
) extends EcumeneFunctionImpl(
  ecmKey,
  localEndpoint,
  publicEndpoint,
  { (unpacker, packer) =>
    if (unpacker.unpackArrayHeader != 0) {
      throw new IllegalArgumentException
    }
    val r = func()

    implicit val pk = packer
    pack(r)
  }
) with Function0[R] {
  def apply(): R = func()
}
