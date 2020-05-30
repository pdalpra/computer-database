package com.github.pdalpra.computerdb

import java.time.LocalDate

import eu.timepit.refined.api._
import eu.timepit.refined.collection._
import eu.timepit.refined.numeric._

package object model {
  type UniqueId       = Long Refined Positive
  type NonEmptyString = String Refined NonEmpty

  object UniqueId       extends RefinedTypeOps[UniqueId, Long]
  object NonEmptyString extends RefinedTypeOps[NonEmptyString, String]

  implicit class LocalDateOps(val date: LocalDate) extends AnyVal {
    def isBeforeOrSameDay(other: LocalDate): Boolean =
      date.isBefore(other) || date.isEqual(other)
  }
}
