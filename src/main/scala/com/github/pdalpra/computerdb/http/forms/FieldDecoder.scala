package com.github.pdalpra.computerdb.http.forms

import cats.syntax.all._
import org.http4s.UrlForm

trait FieldDecoder[A] { self =>
  def decodeField(urlForm: UrlForm, fieldName: String): FieldResult[A]

  def emap[B](validation: A => FieldResult[B]): FieldDecoder[B] =
    self.decodeField(_, _).andThen(validation)
}

object FieldDecoder {
  def apply[A](implicit ev: FieldDecoder[A]): FieldDecoder[A] = ev

  def instance[A](f: (UrlForm, String) => FieldResult[A]): FieldDecoder[A] =
    f(_, _)

  implicit def idInstance[A: StringDecoder]: FieldDecoder[A] =
    instance { (urlForm, fieldName) =>
      urlForm
        .getFirst(fieldName)
        .map(StringDecoder[A].decode(_).leftMap(FieldError(fieldName, _)))
        .getOrElse(FieldError(fieldName, new MissingField(fieldName)).invalid)
    }

  implicit def optionInstance[A: StringDecoder]: FieldDecoder[Option[A]] =
    instance { (urlForm, fieldName) =>
      urlForm
        .getFirst(fieldName)
        .filter(_.nonEmpty)
        .map(StringDecoder[A].decode(_).leftMap(FieldError(fieldName, _)).map(_.some))
        .getOrElse(none.valid)
    }

}
