package com.github.pdalpra.computerdb.http.forms

import java.time.LocalDate
import java.time.format.DateTimeParseException

import cats.data.Validated
import cats.implicits._
import eu.timepit.refined.api.{ RefType, Validate }
import mouse.string._

trait StringDecoder[A] { self =>
  def decode(s: String): Result[A]

  def map[B](f: A => B): StringDecoder[B] =
    new StringDecoder[B] {
      override def decode(s: String) = self.decode(s).map(f)
    }

  def emap[B](validation: A => Result[B]): StringDecoder[B] =
    new StringDecoder[B] {
      override def decode(s: String) = self.decode(s).andThen(validation)
    }
}

object StringDecoder {
  def apply[A](implicit ev: StringDecoder[A]): StringDecoder[A] = ev

  def instance[A](f: String => Result[A]): StringDecoder[A] =
    new StringDecoder[A] {
      override def decode(s: String) = f(s)
    }

  implicit val decodeString: StringDecoder[String] = instance(_.valid)

  implicit val decodeLong: StringDecoder[Long] =
    instance(_.parseLongValidated.leftMap(new NumberParseException(_)))

  implicit val decodeLocalDate: StringDecoder[LocalDate] =
    instance(s =>
      Validated
        .catchOnly[DateTimeParseException](LocalDate.parse(s))
        .leftMap(new DateParseException(_))
    )

  implicit def refinedDecoder[T: StringDecoder, P, F[_, _]: RefType](implicit validate: Validate[T, P]): StringDecoder[F[T, P]] =
    StringDecoder[T].emap { decoded =>
      val refined = RefType[F].refine(decoded).leftMap(new RefinedException(_))
      Validated.fromEither(refined)
    }
}
