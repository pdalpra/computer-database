package com.github.pdalpra.computerdb.model

import java.time.LocalDate

import cats.implicits._
import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.refined._

object UnsavedComputer {
  implicit val decoder: Decoder[UnsavedComputer] =
    deriveDecoder[UnsavedComputer].emap { computer =>
      val introducedBeforeDiscontinued = (computer.introduced, computer.discontinued).mapN(_.isBeforeOrSameDay(_)).getOrElse(true)
      Either.cond(introducedBeforeDiscontinued, computer, "Discontinued date is before introduction date")
    }
}
final case class UnsavedComputer(
    name: NonEmptyString,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate],
    company: Option[Company.Id]
)
