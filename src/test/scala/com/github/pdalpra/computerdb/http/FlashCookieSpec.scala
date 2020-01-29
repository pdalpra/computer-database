package com.github.pdalpra.computerdb.http

import com.github.pdalpra.computerdb.ComputerDatabaseSpec
import com.github.pdalpra.computerdb.http.FlashCookie._

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Set-Cookie`
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import org.http4s.laws.discipline.arbitrary._

class FlashCookieSpec extends ComputerDatabaseSpec {

  private val dsl = Http4sDsl[Task]

  import dsl._

  "FlashCookie" should "leave the response unmodified if there is no flash cookie" in {
    forAll { (method: Method, uri: Uri, status: Status) =>
      val routes = HttpRoutes.of[Task] { case _ => Task.succeed(Response(status = status)) }

      val request           = Request[Task](method = method, uri = uri)
      val withoutMiddleware = runtime.unsafeRun(routes.orNotFound(request))
      val withMiddleware    = runtime.unsafeRun(FlashCookie(routes).orNotFound(request))

      withMiddleware.headers shouldBe withoutMiddleware.headers
    }
  }

  it should "remove the flash cookie if there was one set in the incoming request" in {
    forAll { (method: Method, uri: Uri, status: Status) =>
      val routes = HttpRoutes.of[Task] { case _ => Task.succeed(Response(status = status)) }

      val request  = Request[Task](method = method, uri = uri).addCookie(flashCookieName, "")
      val response = runtime.unsafeRun(FlashCookie(routes).orNotFound(request))

      response.headers.toList should contain(ResponseCookie(flashCookieName, "").clearCookie)
    }
  }

  "withFlashData + flashData " should "be able to encode then decode the flash cookie data" in {
    forAll { (method: Method, uri: Uri, cookieData: String) =>
      val routes = HttpRoutes.of[Task] { case _ => Ok().withFlashData(cookieData) }

      val request  = Request[Task](method = method, uri = uri)
      val response = runtime.unsafeRun(FlashCookie(routes).orNotFound(request))

      val flashCookiesContent  = `Set-Cookie`.from(response.headers).filter(_.cookie.name === flashCookieName).map(_.cookie.content)
      val requestWithFlashData = flashCookiesContent.foldLeft(request)(_.addCookie(flashCookieName, _))

      requestWithFlashData.flashData.value shouldBe cookieData
    }
  }
}
