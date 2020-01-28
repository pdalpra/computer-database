package com.github.pdalpra.computerdb.model

import java.time.LocalDate

final case class UnsavedComputer(
    name: NonEmptyString,
    introduced: Option[LocalDate],
    discontinued: Option[LocalDate],
    company: Option[Company.Id]
)
