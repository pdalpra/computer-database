package com.github.pdalpra.computerdb.db.sql

import java.time.LocalDate

import com.github.pdalpra.computerdb.db.ComputerSort
import com.github.pdalpra.computerdb.model._

import doobie._
import doobie.implicits.javatime._
import doobie.refined.implicits._

private[sql] object DoobieInstances {

  implicit val computerSortPut: Put[ComputerSort] = Put[String].contramap(_.column)

  implicit val companyIdMeta: Meta[Company.Id]   = Meta[UniqueId].imap(Company.Id.apply)(_.value)
  implicit val computerIdMeta: Meta[Computer.Id] = Meta[UniqueId].imap(Computer.Id.apply)(_.value)

  implicit val company: Read[Company] =
    Read[(Company.Id, NonEmptyString)].map(Function.tupled(Company.apply))

  implicit val computerGet: Read[Computer] =
    Read[(Computer.Id, NonEmptyString, Option[LocalDate], Option[LocalDate], Option[Company])].map(Function.tupled(Computer.apply))
}
