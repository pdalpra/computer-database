package com.github.pdalpra.computerdb.db

import scala.collection.immutable

import cats.kernel.Eq
import enumeratum._

sealed trait Order extends EnumEntry with EnumEntry.Lowercase {
  def inverse: Order
}
object Order extends Enum[Order] {
  implicit val eq: Eq[Order] = Eq.fromUniversalEquals

  override def values: immutable.IndexedSeq[Order] = findValues

  case object Asc  extends Order { override def inverse: Order = Desc }
  case object Desc extends Order { override def inverse: Order = Asc  }
}
