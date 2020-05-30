package com.github.pdalpra.computerdb.model

import java.time.LocalDate

import cats.implicits._
import cats.{ derived, Eq }
import eu.timepit.refined.cats._
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.refined._

object Computer {
  implicit val encoder: Encoder[Computer] = deriveEncoder

  object Id {
    implicit val eq: Eq[Id]           = derived.semi.eq
    implicit val encoder: Encoder[Id] = Encoder[UniqueId].contramap(_.value)
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
