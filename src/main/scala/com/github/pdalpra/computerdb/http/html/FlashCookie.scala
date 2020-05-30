package com.github.pdalpra.computerdb.http.html

import java.nio.charset.StandardCharsets
import java.util.Base64

import cats.Functor
import cats.data.Kleisli
import cats.implicits._
import org.http4s.{ Http, Request, Response, ResponseCookie }

private[html] object FlashCookie {

  private val flashCookieName = "http4s-flash"

  def apply[F[_]: Functor, G[_]: Functor](http: Http[F, G]): Http[F, G] =
    Kleisli { request: Request[G] =>
      request.cookies
        .find(_.name == flashCookieName)
        .map(_ => http(request).map(_.removeCookie(flashCookieName)))
        .getOrElse(http(request))
    }

  implicit class FlashRequestSyntax[F[_]](request: Request[F]) {

    def flashData: Option[String] =
      request.cookies.collectFirst { case cookie if cookie.name === flashCookieName => base64Decode(cookie.content) }

    private def base64Decode(encoded: String) =
      new String(Base64.getDecoder.decode(encoded), StandardCharsets.UTF_8.name)
  }

  implicit class FlashResponseSyntax[F[_]: Functor](response: F[Response[F]]) {

    def withFlashData(data: String): F[Response[F]] =
      response.map(_.addCookie(ResponseCookie(flashCookieName, base64Encode(data), path = "/".some)))

    private def base64Encode(content: String) =
      Base64.getEncoder.encodeToString(content.getBytes(StandardCharsets.UTF_8.name))
  }
}
