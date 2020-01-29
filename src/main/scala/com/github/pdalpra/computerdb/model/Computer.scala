package com.github.pdalpra.computerdb.model

import java.time.LocalDate

import cats.implicits._
import cats.{ derived, Eq }
import eu.timepit.refined.cats._

object Computer {
  object Id {
    implicit val eq: Eq[Id] = derived.semi.eq
  }
  final case class Id(value: UniqueId) {
    override def toString: String = value.toString()
  }
}
final case class Computer(
    id: Computer.Id,
    name: NonEmptyString,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate],
    company: Option[Company]
)
