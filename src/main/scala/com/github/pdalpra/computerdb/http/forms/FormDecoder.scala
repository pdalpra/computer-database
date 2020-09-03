package com.github.pdalpra.computerdb.http.forms

import cats.syntax.all._
import org.http4s.UrlForm
import shapeless._
import shapeless.labelled._

trait FormDecoder[A] { self =>
  def decodeForm(form: UrlForm): FormResult[A]

  def emap[B](validation: A => FormResult[B]): FormDecoder[B] =
    new FormDecoder[B] {
      override def decodeForm(form: UrlForm): FormResult[B] =
        self.decodeForm(form).andThen(validation)
    }
}

object FormDecoder {
  def apply[A](implicit ev: FormDecoder[A]): FormDecoder[A] = ev

  def instance[A](f: UrlForm => FormResult[A]): FormDecoder[A] =
    new FormDecoder[A] {
      override def decodeForm(form: UrlForm): FormResult[A] = f(form)
    }

  def derive[A](implicit decoder: Lazy[DerivedFormDecoder[A]]): FormDecoder[A] = decoder.value
}

trait DerivedFormDecoder[A] extends FormDecoder[A]

object DerivedFormDecoder {
  implicit val hnilDecoder: DerivedFormDecoder[HNil] =
    new DerivedFormDecoder[HNil] {
      override def decodeForm(form: UrlForm): FormResult[HNil] = HNil.validNec
    }

  implicit def hlistDecoder[FieldName <: Symbol, Head, Tail <: HList](implicit
      w: Witness.Aux[FieldName],
      hEncoder: Lazy[FieldDecoder[Head]],
      tEncoder: DerivedFormDecoder[Tail]
  ): DerivedFormDecoder[FieldType[FieldName, Head] :: Tail] =
    new DerivedFormDecoder[FieldType[FieldName, Head] :: Tail] {
      override def decodeForm(form: UrlForm): FormResult[FieldType[FieldName, Head] :: Tail] = {
        (hEncoder.value.decodeField(form, w.value.name).toValidatedNec, tEncoder.decodeForm(form))
          .mapN((head, tail) => field[FieldName](head) :: tail)
      }
    }

  implicit def genericDecoder[A, H <: HList](implicit
      gen: LabelledGeneric.Aux[A, H],
      decoder: Lazy[DerivedFormDecoder[H]]
  ): DerivedFormDecoder[A] =
    new DerivedFormDecoder[A] {
      override def decodeForm(form: UrlForm): FormResult[A] =
        decoder.value.decodeForm(form).map(gen.from)
    }

}
