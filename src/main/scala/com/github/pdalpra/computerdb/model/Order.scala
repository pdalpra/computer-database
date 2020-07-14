package com.github.pdalpra.computerdb.model

import scala.collection.immutable

import enumeratum._

sealed trait Order extends EnumEntry with EnumEntry.Lowercase {
  def inverse: Order
}
object Order extends Enum[Order] with CatsEnum[Order] {
  override def values: immutable.IndexedSeq[Order] = findValues

  case object Asc  extends Order { override def inverse: Order = Desc }
  case object Desc extends Order { override def inverse: Order = Asc  }
}
