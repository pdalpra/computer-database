package com.github.pdalpra.computerdb.http

import cats.data.{ Validated, ValidatedNec }

package object forms {
  type Result[A]      = Validated[Error, A]
  type FieldResult[A] = Validated[FieldError, A]
  type FormResult[A]  = ValidatedNec[FieldError, A]
}
