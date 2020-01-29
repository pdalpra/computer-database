package com.github.pdalpra.computerdb.http.forms

import com.github.pdalpra.computerdb.model._

import cats.data.Validated
import cats.implicits._

object ComputerForm {

  implicit val companyId: StringDecoder[Company.Id] = StringDecoder[UniqueId].map(Company.Id.apply)

  implicit val decoder: FormDecoder[UnsavedComputer] =
    FormDecoder.derive[UnsavedComputer].emap(validateDates)

  private def validateDates(computer: UnsavedComputer): FormResult[UnsavedComputer] = {
    val introducedBeforeDiscontinued = (computer.introduced, computer.discontinued).mapN(_.isBefore(_)).getOrElse(true)
    Validated.condNec(
      introducedBeforeDiscontinued,
      computer,
      FieldError("discontinued", new Error("Discontinued date is before introduction date"))
    )
  }
}
