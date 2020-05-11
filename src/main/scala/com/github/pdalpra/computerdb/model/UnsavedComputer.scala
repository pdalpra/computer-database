package com.github.pdalpra.computerdb.model

import java.time.LocalDate

import io.circe.Decoder
import io.circe.generic.semiauto._
import io.circe.refined._

object UnsavedComputer {
  implicit val decoder: Decoder[UnsavedComputer] = deriveDecoder
}
final case class UnsavedComputer(
    name: NonEmptyString,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate],
    company: Option[Company.Id]
)
