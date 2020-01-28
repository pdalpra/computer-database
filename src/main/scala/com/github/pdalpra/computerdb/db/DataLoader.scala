package com.github.pdalpra.computerdb.db

import com.github.pdalpra.computerdb.model._

import cats.effect.{ Blocker, ContextShift, Sync }
import cats.implicits._
import fs2.io.unsafeReadInputStream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.Decoder
import io.circe.fs2.{ byteArrayParser, decoder }
import io.circe.generic.semiauto._
import io.circe.refined._

final case class InitialData(companies: List[NonEmptyString], computers: List[UnsavedComputer])

trait DataLoader[F[_]] {
  def loadInitialData: F[InitialData]
}

object DataLoader {

  private val chunkSize = 8192

  def apply[F[_]: Sync: ContextShift](blocker: Blocker): DataLoader[F] =
    new DefaultDataLoader[F](blocker)

  private class DefaultDataLoader[F[_]: Sync: ContextShift](blocker: Blocker) extends DataLoader[F] {
    private val logger = Slf4jLogger.getLogger[F]

    implicit val companyIdDecoder: Decoder[Company.Id]     = Decoder[UniqueId].map(Company.Id.apply)
    implicit val computerIdDecoder: Decoder[Computer.Id]   = Decoder[UniqueId].map(Computer.Id.apply)
    implicit val computerDecoder: Decoder[UnsavedComputer] = deriveDecoder

    override def loadInitialData: F[InitialData] =
      for {
        companies <- readJsonFromClasspathResource[NonEmptyString]("data/companies.json")
        computers <- readJsonFromClasspathResource[UnsavedComputer]("data/computers.json")
        _         <- logger.info(s"Loaded ${companies.size} companies from reference data.")
        _         <- logger.info(s"Loaded ${computers.size} computers from reference data.")
      } yield InitialData(companies, computers)

    private def readJsonFromClasspathResource[T: Decoder](resourceName: String): F[List[T]] = {
      val inputStream = Sync[F].delay(getClass.getClassLoader.getResourceAsStream(resourceName))

      unsafeReadInputStream(inputStream, chunkSize, blocker)
        .through(byteArrayParser)
        .through(decoder[F, T])
        .compile
        .toList
    }
  }
}
