package com.github.pdalpra.computerdb.http.json

import com.github.pdalpra.computerdb.http._
import com.github.pdalpra.computerdb.model._
import com.github.pdalpra.computerdb.service._

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import io.circe.{ Encoder, Printer }
import org.http4s._
import org.http4s.circe.jsonEncoderWithPrinterOf
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.CORS

private[http] object Routes {

  private val noSpacesNoNullsPrinter = Printer.noSpaces.copy(dropNullValues = true)

  def apply[F[_]: Sync](computerService: ComputerService[F]): HttpRoutes[F] =
    new Routes[F](computerService).routes

  private class Routes[F[_]: Sync](computerService: ComputerService[F]) extends Http4sDsl[F] with Extractors {

    private implicit def circeEntityEncoder[A: Encoder]: EntityEncoder[F, A] =
      jsonEncoderWithPrinterOf(noSpacesNoNullsPrinter)

    def routes: HttpRoutes[F] = CORS(computerReadRoutes <+> computerWriteRoutes)

    private def computerReadRoutes =
      HttpRoutes.of[F] {
        case GET -> Root :? PageNumber(page) +& PageSize(pageSize) +& Sort(sort) +& SortOrder(order) +& SearchQuery(rawQuery) =>
          val query      = rawQuery.flatten
          val parameters = ComputerListParameters(page, pageSize, sort, order, query)
          Ok(computerService.fetchComputers(parameters))

        case GET -> Root / ComputerId(id) =>
          (for {
            computer <- OptionT(computerService.fetchComputer(id))
            response <- OptionT.liftF(Ok(computer))
          } yield response).getOrElseF(NotFound())
      }

    private def computerWriteRoutes =
      HttpRoutes.of[F] {
        case req @ POST -> Root =>
          for {
            unsavedComputer <- req.as[UnsavedComputer]
            savedComputer   <- computerService.insertComputer(unsavedComputer)
            response        <- Ok(savedComputer)
          } yield response

        case req @ POST -> Root / ComputerId(id) =>
          (for {
            unsavedComputer <- OptionT.liftF(req.as[UnsavedComputer])
            _               <- OptionT(computerService.fetchComputer(id))
            _               <- OptionT.liftF(computerService.updateComputer(id, unsavedComputer))
            updatedComputer <- OptionT(computerService.fetchComputer(id))
            response        <- OptionT.liftF(Ok(updatedComputer))
          } yield response).getOrElseF(NotFound())

        case DELETE -> Root / ComputerId(id) =>
          (for {
            _        <- OptionT(computerService.deleteComputer(id))
            response <- OptionT.liftF(NoContent())
          } yield response).getOrElseF(NotFound())
      }
  }
}
