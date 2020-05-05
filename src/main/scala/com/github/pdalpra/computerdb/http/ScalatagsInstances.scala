package com.github.pdalpra.computerdb.http

import org.http4s.{ DefaultCharset, EntityEncoder, MediaType }
import org.http4s.headers.`Content-Type`
import scalatags.Text.TypedTag

object ScalatagsInstances {

  implicit def scalatagsEncoder[F[_]]: EntityEncoder[F, TypedTag[String]] =
    contentEncoder(MediaType.text.html)

  private def contentEncoder[F[_], C <: TypedTag[String]](mediaType: MediaType): EntityEncoder[F, C] =
    EntityEncoder
      .stringEncoder[F]
      .contramap[C](content => "<!DOCTYPE html>" + content.render)
      .withContentType(`Content-Type`(mediaType, DefaultCharset))
}
