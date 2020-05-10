package com.github.pdalpra.computerdb.http

import com.github.pdalpra.computerdb.ComputerDatabaseBuildInfo
import com.github.pdalpra.computerdb.http.FlashCookie._
import com.github.pdalpra.computerdb.http.forms.ComputerForm._
import com.github.pdalpra.computerdb.http.forms.FieldError
import com.github.pdalpra.computerdb.http.forms.syntax._
import com.github.pdalpra.computerdb.http.html.Forms.InvalidFormState
import com.github.pdalpra.computerdb.http.ScalatagsInstances._
import com.github.pdalpra.computerdb.http.html._
import com.github.pdalpra.computerdb.model._
import com.github.pdalpra.computerdb.service._

import cats.data.{ NonEmptyChain, OptionT }
import cats.effect.{ Blocker, ContextShift, Sync }
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{ `Content-Type`, Location }
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.staticcontent._

object Routes {

  def apply[F[_]: Sync: ContextShift](computerService: ComputerService[F], blocker: Blocker): HttpApp[F] =
    new Routes[F](computerService, blocker).httpApp

  private class Routes[F[_]: Sync: ContextShift](computerService: ComputerService[F], blocker: Blocker)
      extends Http4sDsl[F]
      with Extractors {

    def httpApp: HttpApp[F] = FlashCookie(router).orNotFound

    private def router =
      Router(
        "/"          -> miscRoutes,
        "/computers" -> (computerReadRoutes <+> computerWriteRoutes),
        "/assets"    -> assetsRoutes
      )

    private def miscRoutes =
      HttpRoutes.of[F] {
        case GET -> Root             => redirectToHome
        case GET -> Root / "version" => Ok(ComputerDatabaseBuildInfo.toJson, `Content-Type`(MediaType.application.json))
      }

    private def computerReadRoutes =
      HttpRoutes.of[F] {
        case req @ GET -> Root :? PageNumber(page) +& PageSize(pageSize) +& Sort(sort) +& SortOrder(order) +& SearchQuery(rawQuery) =>
          val query      = rawQuery.flatten
          val parameters = ComputerListParameters(page, pageSize, sort, order, query)

          for {
            page     <- computerService.fetchComputers(parameters)
            flashData = req.flashData
            context   = ComputersListView.Context(page, parameters)
            response <- Ok(ComputersListView.computersList(context, flashData))
          } yield response

        case GET -> Root / "new" =>
          for {
            companies <- computerService.fetchCompanies
            response  <- Ok(Forms.creationForm(companies, None))
          } yield response

        case GET -> Root / ComputerId(id) =>
          (for {
            computer  <- OptionT(computerService.fetchComputer(id))
            companies <- OptionT.liftF(computerService.fetchCompanies)
            response  <- OptionT.liftF(Ok(Forms.editionForm(computer, companies, None)))
          } yield response).getOrElseF(NotFound())
      }

    private def computerWriteRoutes =
      HttpRoutes.of[F] {
        case req @ POST -> Root =>
          def onFormSuccess(computer: UnsavedComputer) =
            computerService.insertComputer(computer) *>
              redirectToHome.withFlashData(s"Computer ${computer.name} has been created")

          def onFormError(form: UrlForm)(errors: NonEmptyChain[FieldError]) =
            for {
              companies <- computerService.fetchCompanies
              response  <- BadRequest(Forms.creationForm(companies, InvalidFormState(form, errors).some))
            } yield response

          for {
            urlForm    <- req.as[UrlForm]
            decodedForm = urlForm.decode[UnsavedComputer]
            response   <- decodedForm.fold(onFormError(urlForm), onFormSuccess)
          } yield response

        case req @ POST -> Root / ComputerId(id) =>
          def onFormSuccess(id: Computer.Id)(computer: UnsavedComputer) =
            computerService.updateComputer(id, computer) *>
              redirectToHome.withFlashData(s"Computer ${computer.name} has been updated")

          def onFormError(computer: Computer, form: UrlForm)(errors: NonEmptyChain[FieldError]) =
            for {
              companies <- computerService.fetchCompanies
              response  <- BadRequest(Forms.editionForm(computer, companies, InvalidFormState(form, errors).some))
            } yield response

          (for {
            computer   <- OptionT(computerService.fetchComputer(id))
            urlForm    <- OptionT.liftF(req.as[UrlForm])
            decodedForm = urlForm.decode[UnsavedComputer]
            response   <- OptionT.liftF(decodedForm.fold(onFormError(computer, urlForm), onFormSuccess(id)))
          } yield response).getOrElseF(NotFound())

        case POST -> Root / ComputerId(id) / "delete" =>
          (for {
            computer <- OptionT(computerService.deleteComputer(id))
            response <- OptionT.liftF(redirectToHome.withFlashData(s"Computer ${computer.name} has been deleted"))
          } yield response).getOrElseF(NotFound())

        case GET -> Root / "reset" =>
          for {
            _        <- computerService.loadDefaultComputers
            response <- redirectToHome.withFlashData("Computer data reset to reference data.")
          } yield response
      }

    private def assetsRoutes =
      resourceService(ResourceService.Config("assets", blocker, cacheStrategy = MemoryCache[F]()))

    private val redirectToHome = SeeOther(Location(uri"/computers"))
  }
}
