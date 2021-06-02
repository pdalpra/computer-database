package com.github.pdalpra.computerdb

import cats.Applicative
import cats.syntax.all._
import org.http4s.headers.Location
import org.http4s._
import org.http4s.implicits._

package object http {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial")) // Option#get usage is wrongly detected
  def redirectToHome[F[_]: Applicative]: F[Response[F]] =
    Response[F](status = Status.SeeOther, headers = Headers(Location(uri"/computers"))).pure[F]
}
