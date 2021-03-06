package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model._

import cats.effect.Sync
import cats.syntax.all._
import fs2.io.unsafeReadInputStream
import org.typelevel.log4cats.slf4j.Slf4jLogger
import io.circe.Decoder
import io.circe.refined._
import io.circe.fs2._

final case class InitialData(companies: List[NonEmptyString], computers: List[UnsavedComputer])

trait DataLoader[F[_]] {
  def loadInitialData: F[InitialData]
}

object DataLoader {

  private val chunkSize = 8192

  def apply[F[_]: Sync]: DataLoader[F] =
    new DataLoader[F] {
      private val logger = Slf4jLogger.getLogger[F]

      override def loadInitialData: F[InitialData] =
        for {
          companies <- readJsonFromClasspathResource[NonEmptyString]("data/companies.json")
          computers <- readJsonFromClasspathResource[UnsavedComputer]("data/computers.json")
          _         <- logger.info(s"Loaded ${companies.size} companies from reference data.")
          _         <- logger.info(s"Loaded ${computers.size} computers from reference data.")
        } yield InitialData(companies, computers)

      private def readJsonFromClasspathResource[T: Decoder](resourceName: String): F[List[T]] = {
        val inputStream = Sync[F].delay(getClass.getClassLoader.getResourceAsStream(resourceName))

        unsafeReadInputStream(inputStream, chunkSize)
          .through(byteArrayParser)
          .through(decoder[F, T])
          .compile
          .toList
      }
    }
}
