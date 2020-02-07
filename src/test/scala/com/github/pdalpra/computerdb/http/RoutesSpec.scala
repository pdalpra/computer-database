package com.github.pdalpra.computerdb.http

import com.github.pdalpra.computerdb.ComputerDatabaseSpec
import com.github.pdalpra.computerdb.db.testkit._

import org.http4s._
import org.http4s.headers.{ `Content-Type`, Location }
import org.http4s.implicits._
import zio._
import zio.interop.catz._

class RoutesSpec extends ComputerDatabaseSpec {

  "GET /" should "redirect to /computers" in {
    val routes = Routes(computerRepositoryWith(), companyRepositoryWith(), blocker)

    val request  = Request[Task](uri = Uri.unsafeFromString("/"))
    val response = runtime.unsafeRun(routes(request))

    response.status shouldBe Status.SeeOther
    response.headers.get(Location).value.uri shouldBe uri"/computers"
  }

  "GET /assets/*" should "render assets bundled in the classpath" in {
    val routes = Routes(computerRepositoryWith(), companyRepositoryWith(), blocker)

    val request  = Request[Task](uri = Uri.unsafeFromString("/assets/css/main.css"))
    val response = runtime.unsafeRun(routes(request))

    response.status shouldBe Status.Ok
    response.contentType.value shouldBe `Content-Type`(MediaType.text.css)
  }

  "GET /computers/new" should "render the form to add a new computer" in {
    val routes = Routes(computerRepositoryWith(), companyRepositoryWith(), blocker)

    val request  = Request[Task](uri = Uri.unsafeFromString("/computers/new"))
    val response = runtime.unsafeRun(routes(request))

    response.status shouldBe Status.Ok
    response.contentType.value shouldBe `Content-Type`(MediaType.text.html, Charset.`UTF-8`)
  }

  it should "fail if the list of companies could not be loaded" in {
    val companyRepository = companyRepositoryWith(fetchAllF = Task.fail(new IllegalStateException))
    val routes            = Routes(computerRepositoryWith(), companyRepository, blocker)

    val request = Request[Task](uri = Uri.unsafeFromString("/computers/new"))

    a[Throwable] should be thrownBy runtime.unsafeRun(routes(request))
  }

}
