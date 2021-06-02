package com.github.pdalpra.computerdb

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

import com.github.pdalpra.computerdb.db._
import com.github.pdalpra.computerdb.http.Routes
import com.github.pdalpra.computerdb.service.ComputerService

import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import doobie._
import doobie.h2.H2Transactor
import doobie.h2.syntax.h2transactor._
import doobie.util.ExecutionContexts
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

class ComputerDatabase[F[_]: Async] {

  private val logger       = Slf4jLogger.getLogger[F]
  private val configSource = ConfigSource.default.at("app")

  def program: F[Unit] =
    for {
      config <- configSource.loadF[F, Config]()
      _      <- appResources(config).use(setup(_, config))
    } yield ()

  private def setup(appResources: AppResources, config: Config): F[Unit] = {
    val initSchema         = SchemaInitializer[F](appResources.transactor)
    val dataLoader         = DataLoader[F]
    val companyRepository  = CompanyRepository[F](appResources.transactor)
    val computerRepository = ComputerRepository[F](appResources.transactor)

    for {
      _           <- initSchema.initSchema
      initialData <- dataLoader.loadInitialData
      computerService =
        ComputerService(computerRepository, companyRepository, config.db.readOnlyComputers, initialData.computers)
      routes  = Routes[F](computerService)
      _      <- companyRepository.loadAll(initialData.companies)
      _      <- computerRepository.loadAll(initialData.computers)
      _      <- logger.info("Loaded all reference data into the database.")
      _      <- scheduleDataReset(computerService, config.db.restoreInitial)
      server <- server(routes, config.server, appResources.serverExecutionContext).start
      _      <- logger.info(s"Computer database started on port ${config.server.port}")
      _      <- server.join
    } yield ()
  }

  private def scheduleDataReset(computerService: ComputerService[F], config: Config.Database.RestoreInitial): F[Unit] =
    Sync[F].whenA(config.enabled) {
      val resetData = computerService.loadDefaultComputers *> logger.info("Computer data reset to reference data.")
      schedule(resetData, config.frequency, "Failed to reset computer data")
    }

  private def schedule(action: F[Unit], interval: FiniteDuration, errorMessage: String): F[Unit] =
    (Temporal[F].sleep(interval) >> action
      .handleErrorWith(ex => logger.error(s"$errorMessage: $ex"))).foreverM.start.void

  private def server(httpApp: HttpApp[F], config: Config.Server, serverExecutionContext: ExecutionContext): F[Unit] =
    BlazeServerBuilder[F](serverExecutionContext)
      .withTcpNoDelay(true)
      .withResponseHeaderTimeout(config.responseHeaderTimeout)
      .withIdleTimeout(config.idleTimeout)
      .withHttpApp(httpApp)
      .bindHttp(config.port.value, "0.0.0.0")
      .serve
      .compile
      .drain

  private def appResources(config: Config): Resource[F, AppResources] =
    for {
      serverEC     <- ExecutionContexts.fixedThreadPool(config.server.threadPoolSize.value)
      connectionEC <- ExecutionContexts.cachedThreadPool
      rawTransactor =
        H2Transactor.newH2Transactor[F](config.db.url, config.db.username, config.db.password, connectionEC)
      transactor <- rawTransactor.evalTap(_.setMaxConnections(config.db.maxConnections.value))
    } yield AppResources(transactor, serverEC)

  private case class AppResources(transactor: Transactor[F], serverExecutionContext: ExecutionContext)
}
