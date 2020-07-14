package com.github.pdalpra.computerdb.model

import java.time.LocalDate

import cats.implicits._
import cats.{ Eq, Show }
import eu.timepit.refined.cats._
import io.circe.Encoder
import io.circe.refined._

object Computer {
  implicit val encoder: Encoder[Computer] =
    Encoder.forProduct5("id", "name", "introduced", "discontinued", "company")(computer =>
      (computer.id, computer.name, computer.introduced, computer.discontinued, computer.company)
    )

  object Id {
    implicit val eq: Eq[Id]           = Eq[UniqueId].contramap(_.value)
    implicit val show: Show[Id]       = Show[UniqueId].contramap(_.value)
    implicit val encoder: Encoder[Id] = Encoder[UniqueId].contramap(_.value)
  }
  final case class Id(value: UniqueId)

  def apply(
      id: Computer.Id,
      name: NonEmptyString,
      introduced: Option[LocalDate],
      discontinued: Option[LocalDate],
      company: Option[Company]
  ): Either[String, Computer] =
    validateDates(introduced, discontinued).as(new Computer(id, name, introduced, discontinued, company) {})

  def validateDates(introduced: Option[LocalDate], discontinued: Option[LocalDate]): Either[String, Unit] =
    (introduced, discontinued)
      .mapN {
        case (introduced, discontinued) =>
          Either.cond(introduced.isBeforeOrSameDay(discontinued), (), "Discontinued date is before introduction date")
      }
      .getOrElse(Right(()))
}
sealed abstract case class Computer(
    id: Computer.Id,
    name: NonEmptyString,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate],
    company: Option[Company]
)
