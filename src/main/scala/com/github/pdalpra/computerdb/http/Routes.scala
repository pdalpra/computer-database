package com.github.pdalpra.computerdb.http

import com.github.pdalpra.computerdb.BuildInfo
import com.github.pdalpra.computerdb.db._
import com.github.pdalpra.computerdb.http.FlashCookie._
import com.github.pdalpra.computerdb.http.forms.ComputerForm._
import com.github.pdalpra.computerdb.http.forms.FieldError
import com.github.pdalpra.computerdb.http.forms.syntax._
import com.github.pdalpra.computerdb.http.html.Forms.InvalidFormState
import com.github.pdalpra.computerdb.http.html._
import com.github.pdalpra.computerdb.model._

import cats.data.{ NonEmptyChain, OptionT }
import cats.effect.{ Blocker, ContextShift, Effect }
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{ `Content-Type`, Location }
import org.http4s.implicits._
import org.http4s.scalatags._
import org.http4s.server.Router
import org.http4s.server.staticcontent._

object Routes {

  def apply[F[_]: Effect: ContextShift](
      computerRepository: ComputerRepository[F],
      companyRepository: CompanyRepository[F],
      blocker: Blocker
  ): HttpApp[F] =
    new Routes[F](computerRepository, companyRepository, blocker).httpApp

  private class Routes[F[_]: Effect: ContextShift](
      computerRepository: ComputerRepository[F],
      companyRepository: CompanyRepository[F],
      blocker: Blocker
  ) extends Http4sDsl[F]
      with Extractors {

    def httpApp: HttpApp[F] = FlashCookie(router).orNotFound

    private def router = Router(
      "/"          -> miscRoutes,
      "/computers" -> (computerReadRoutes <+> computerWriteRoutes),
      "/assets"    -> assetsRoutes
    )

    private def miscRoutes = HttpRoutes.of[F] {
      case GET -> Root             => redirectToHome
      case GET -> Root / "version" => Ok(BuildInfo.toJson, `Content-Type`(MediaType.application.json))
    }

    private def computerReadRoutes = HttpRoutes.of[F] {
      case req @ GET -> Root :? PageNumber(pageOpt) +& PageSize(pageSizeOpt) +&
            Sort(sortOpt) +& SortOrder(orderOpt) +& SearchQuery(query) =>
        val page  = pageOpt.getOrElse(Page.DefaultPage)
        val sort  = sortOpt.getOrElse(ComputerSort.Name)
        val order = orderOpt.getOrElse(Order.Asc)

        for {
          page      <- computerRepository.fetchPaged(page, pageSizeOpt, sort, order, query)
          flashData = req.flashData
          context   = ComputersListView.Context(page, query, sort, order)
          response  <- Ok(ComputersListView.computersList(context, flashData))
        } yield response

      case GET -> Root / "new" =>
        for {
          companies <- companyRepository.fetchAll
          response  <- Ok(Forms.creationForm(companies, None))
        } yield response

      case GET -> Root / ComputerId(id) =>
        (for {
          computer  <- OptionT(computerRepository.fetchOne(id))
          companies <- OptionT.liftF(companyRepository.fetchAll)
          response  <- OptionT.liftF(Ok(Forms.editionForm(computer, companies, None)))
        } yield response).getOrElseF(NotFound())
    }

    private def computerWriteRoutes = HttpRoutes.of[F] {
      case req @ POST -> Root =>
        def onFormSuccess(computer: UnsavedComputer) =
          computerRepository.insert(computer) *>
            redirectToHome.withFlashData(s"Computer ${computer.name} has been created")

        def onFormError(form: UrlForm)(errors: NonEmptyChain[FieldError]) =
          for {
            companies <- companyRepository.fetchAll
            response  <- BadRequest(Forms.creationForm(companies, InvalidFormState(form, errors).some))
          } yield response

        for {
          urlForm     <- req.as[UrlForm]
          decodedForm = urlForm.decode[UnsavedComputer]
          response    <- decodedForm.fold(onFormError(urlForm), onFormSuccess)
        } yield response

      case req @ POST -> Root / ComputerId(id) =>
        def onFormSuccess(id: Computer.Id)(computer: UnsavedComputer) =
          computerRepository.update(id, computer) *>
            redirectToHome.withFlashData(s"Computer ${computer.name} has been updated")

        def onFormError(computer: Computer, form: UrlForm)(errors: NonEmptyChain[FieldError]) =
          for {
            companies <- companyRepository.fetchAll
            response  <- BadRequest(Forms.editionForm(computer, companies, InvalidFormState(form, errors).some))
          } yield response

        (for {
          computer    <- OptionT(computerRepository.fetchOne(id))
          urlForm     <- OptionT.liftF(req.as[UrlForm])
          decodedForm = urlForm.decode[UnsavedComputer]
          response    <- OptionT.liftF(decodedForm.fold(onFormError(computer, urlForm), onFormSuccess(id)))
        } yield response).getOrElseF(NotFound())

      case POST -> Root / ComputerId(id) / "delete" =>
        (for {
          computer <- OptionT(computerRepository.fetchOne(id))
          _        <- OptionT.liftF(computerRepository.deleteOne(id))
          response <- OptionT.liftF(redirectToHome.withFlashData(s"Computer ${computer.name} has been deleted"))
        } yield response).getOrElseF(NotFound())
    }

    private def assetsRoutes =
      resourceService(ResourceService.Config("assets", blocker, cacheStrategy = MemoryCache[F]()))

    private val redirectToHome = SeeOther(Location(uri"/computers"))
  }
}
