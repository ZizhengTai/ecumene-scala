package io.ecumene

import scala.util.Try
import org.msgpack.core.{ MessagePacker, MessageUnpacker }

package object core {
  trait Packer[-T] {
    def pack(x: T)(implicit mp: MessagePacker): Unit
  }

  trait CanPack[-T] {
    def packer: Packer[T]
  }

  trait Unpacker[T] {
    def unpack(implicit mup: MessageUnpacker): T
  }

  trait CanUnpack[T] {
    def unpacker: Unpacker[T]
  }

  object Implicits {
    implicit object BooleanPacker extends Packer[Boolean] {
      def pack(x: Boolean)(implicit mp: MessagePacker) = mp packBoolean x
    }
    implicit object CanPackBoolean extends CanPack[Boolean] {
      val packer = BooleanPacker
    }

    implicit object ShortPacker extends Packer[Short] {
      def pack(x: Short)(implicit mp: MessagePacker) = mp packShort x
    }
    implicit object CanPackShort extends CanPack[Short] {
      val packer = ShortPacker
    }

    implicit object IntPacker extends Packer[Int] {
      def pack(x: Int)(implicit mp: MessagePacker) = mp packInt x
    }
    implicit object CanPackInt extends CanPack[Int] {
      val packer = IntPacker
    }

    implicit object LongPacker extends Packer[Long] {
      def pack(x: Long)(implicit mp: MessagePacker) = mp packLong x
    }
    implicit object CanPackLong extends CanPack[Long] {
      val packer = LongPacker
    }

    implicit object StringPacker extends Packer[String] {
      def pack(x: String)(implicit mp: MessagePacker) = mp packString x
    }
    implicit object CanPackString extends CanPack[String] {
      val packer = StringPacker
    }

    implicit def SeqPacker[T: CanPack]: Packer[Seq[T]] = new Packer[Seq[T]] {
      def pack(seq: Seq[T])(implicit mp: MessagePacker) = {
        mp packArrayHeader seq.length
        seq foreach { x => core.pack(x) }
      }
    }
    implicit def CanPackSeq[T: CanPack]: CanPack[Seq[T]] = new CanPack[Seq[T]] {
      def packer = SeqPacker
    }

    implicit def MapPacker[T: CanPack, U: CanPack]: Packer[Map[T, U]] = new Packer[Map[T, U]] {
      def pack(map: Map[T, U])(implicit mp: MessagePacker) = {
        mp packMapHeader map.size
        map foreach { case (k, v) => core.pack(k); core.pack(v) }
      }
    }
    implicit def CanPackMap[T: CanPack, U: CanPack]: CanPack[Map[T, U]] = new CanPack[Map[T, U]] {
      def packer = MapPacker
    }

    implicit object BooleanUnpacker extends Unpacker[Boolean] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackBoolean
    }
    implicit object CanUnpackBoolean extends CanUnpack[Boolean] {
      val unpacker = BooleanUnpacker
    }

    implicit object ShortUnpacker extends Unpacker[Short] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackShort
    }
    implicit object CanUnpackShort extends CanUnpack[Short] {
      val unpacker = ShortUnpacker
    }

    implicit object IntUnpacker extends Unpacker[Int] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackInt
    }
    implicit object CanUnpackInt extends CanUnpack[Int] {
      val unpacker = IntUnpacker
    }

    implicit object LongUnpacker extends Unpacker[Long] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackLong
    }
    implicit object CanUnpackLong extends CanUnpack[Long] {
      val unpacker = LongUnpacker
    }

    implicit object FloatUnpacker extends Unpacker[Float] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackFloat
    }
    implicit object CanUnpackFloat extends CanUnpack[Float] {
      val unpacker = FloatUnpacker
    }

    implicit object DoubleUnpacker extends Unpacker[Double] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackDouble
    }
    implicit object CanUnpackDouble extends CanUnpack[Double] {
      val unpacker = DoubleUnpacker
    }

    implicit object StringUnpacker extends Unpacker[String] {
      def unpack(implicit mup: MessageUnpacker) = mup.unpackString
    }
    implicit object CanUnpackString extends CanUnpack[String] {
      val unpacker = StringUnpacker
    }

    implicit def SeqUnpacker[T: CanUnpack]: Unpacker[Seq[T]] = new Unpacker[Seq[T]] {
      def unpack(implicit mup: MessageUnpacker) = {
        val length = mup.unpackArrayHeader
        (0 until length).map(_ => core.unpack[T].get).toSeq
      }
    }
    implicit def CanUnpackSeq[T: CanUnpack]: CanUnpack[Seq[T]] = new CanUnpack[Seq[T]] {
      def unpacker = SeqUnpacker
    }

    implicit def MapUnpacker[T: CanUnpack, U: CanUnpack]: Unpacker[Map[T, U]] = new Unpacker[Map[T, U]] {
      def unpack(implicit mup: MessageUnpacker) = {
        val size = mup.unpackMapHeader
        (0 until size).map(_ => core.unpack[T].get -> core.unpack[U].get).toMap
      }
    }
    implicit def CanUnpackMap[T: CanUnpack, U: CanUnpack]: CanUnpack[Map[T, U]] = new CanUnpack[Map[T, U]] {
      def unpacker = MapUnpacker
    }
  }

  def pack[T: CanPack](x: T)(implicit mp: MessagePacker): Unit = {
    implicitly[CanPack[T]].packer pack x
  }

  def unpack[T: CanUnpack](implicit mup: MessageUnpacker): Try[T] = {
    Try(implicitly[CanUnpack[T]].unpacker.unpack)
  }
}
