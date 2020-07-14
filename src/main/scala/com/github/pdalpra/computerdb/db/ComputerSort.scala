package com.github.pdalpra.computerdb.db

import scala.collection.immutable

import enumeratum.values._

sealed abstract class ComputerSort(val value: String, val column: String) extends StringEnumEntry

object ComputerSort extends StringEnum[ComputerSort] with CatsValueEnum[String, ComputerSort] {
  override def values: immutable.IndexedSeq[ComputerSort] = findValues

  case object Name         extends ComputerSort("name", "lower(computer.name)")
  case object Introduced   extends ComputerSort("introduced", "computer.introduced")
  case object Discontinued extends ComputerSort("discontinued", "computer.discontinued")
  case object CompanyName  extends ComputerSort("companyName", "lower(company.name)")
}
