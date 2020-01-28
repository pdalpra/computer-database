package com.github.pdalpra.computerdb.http.forms

import org.http4s.UrlForm

object syntax {
  implicit class UrlFormSyntax(val urlForm: UrlForm) {
    def decode[A](implicit decoder: FormDecoder[A]): FormResult[A] =
      decoder.decodeForm(urlForm)
  }
}
