package com.github.pdalpra.computerdb.http.forms

import cats.syntax.all._
import org.http4s.UrlForm
import shapeless._
import shapeless.labelled._

trait FormDecoder[A] { self =>
  def decodeForm(form: UrlForm): FormResult[A]

  def emap[B](validation: A => FormResult[B]): FormDecoder[B] =
    self.decodeForm(_).andThen(validation)
}

object FormDecoder {
  def apply[A](implicit ev: FormDecoder[A]): FormDecoder[A] = ev

  def instance[A](f: UrlForm => FormResult[A]): FormDecoder[A] =
    f(_)

  def derive[A](implicit decoder: Lazy[DerivedFormDecoder[A]]): FormDecoder[A] = decoder.value
}

trait DerivedFormDecoder[A] extends FormDecoder[A]

object DerivedFormDecoder {
  implicit val hnilDecoder: DerivedFormDecoder[HNil] =
    _ => HNil.validNec

  implicit def hlistDecoder[FieldName <: Symbol, Head, Tail <: HList](implicit
      w: Witness.Aux[FieldName],
      hEncoder: Lazy[FieldDecoder[Head]],
      tEncoder: DerivedFormDecoder[Tail]
  ): DerivedFormDecoder[FieldType[FieldName, Head] :: Tail] =
    form => {
      (hEncoder.value.decodeField(form, w.value.name).toValidatedNec, tEncoder.decodeForm(form))
        .mapN((head, tail) => field[FieldName](head) :: tail)
    }

  implicit def genericDecoder[A, H <: HList](implicit
      gen: LabelledGeneric.Aux[A, H],
      decoder: Lazy[DerivedFormDecoder[H]]
  ): DerivedFormDecoder[A] =
    decoder.value.decodeForm(_).map(gen.from)

}
