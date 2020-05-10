package com.github.pdalpra.computerdb.db

import scala.collection.immutable

import cats.kernel.Eq
import enumeratum.values._

sealed abstract class ComputerSort(val value: String, val column: String) extends StringEnumEntry

object ComputerSort extends StringEnum[ComputerSort] {
  implicit val eq: Eq[ComputerSort] = Eq.fromUniversalEquals

  override def values: immutable.IndexedSeq[ComputerSort] = findValues

  case object Name         extends ComputerSort("name", "lower(computer.name)")
  case object Introduced   extends ComputerSort("introduced", "computer.introduced")
  case object Discontinued extends ComputerSort("discontinued", "computer.discontinued")
  case object CompanyName  extends ComputerSort("companyName", "lower(company.name)")
}
