package com.github.pdalpra.computerdb

import cats.Applicative
import cats.implicits._
import org.http4s.headers.Location
import org.http4s._
import org.http4s.implicits._

package object http {
  def redirectToHome[F[_]: Applicative]: F[Response[F]] =
    Response[F](status = Status.SeeOther, headers = Headers.of(Location(uri"/computers"))).pure[F]
}
