package com.github.pdalpra.computerdb.service

import com.github.pdalpra.computerdb.db.{ CompanyRepository, ComputerRepository }
import com.github.pdalpra.computerdb.model._

import cats.data.OptionT
import cats.effect.Sync

trait ComputerService[F[_]] {
  def fetchCompanies: F[List[Company]]

  def fetchComputer(id: Computer.Id): F[Option[Computer]]
  def fetchComputers(parameters: ComputerListParameters): F[Page[Computer]]

  def insertComputer(computer: UnsavedComputer): F[Computer]
  def updateComputer(id: Computer.Id, computer: UnsavedComputer): F[Unit]
  def deleteComputer(id: Computer.Id): F[Option[Computer]]

  def loadDefaultComputers: F[Unit]
}
object ComputerService {

  def apply[F[_]: Sync](
      computerRepository: ComputerRepository[F],
      companyRepository: CompanyRepository[F],
      readOnlyComputers: Set[Computer.Id],
      initialComputers: List[UnsavedComputer]
  ): ComputerService[F] =
    new ComputerService[F] {
      override def fetchCompanies: F[List[Company]] =
        companyRepository.fetchAll

      override def fetchComputer(id: Computer.Id): F[Option[Computer]] =
        computerRepository.fetchOne(id)

      override def fetchComputers(parameters: ComputerListParameters): F[Page[Computer]] =
        computerRepository.fetchPaged(parameters.number, parameters.size, parameters.sort, parameters.order, parameters.nameFilter)

      override def insertComputer(computer: UnsavedComputer): F[Computer] =
        computerRepository.insert(computer)

      override def updateComputer(id: Computer.Id, computer: UnsavedComputer): F[Unit] =
        Sync[F].whenA(!readOnlyComputers.contains(id)) {
          computerRepository.update(id, computer)
        }

      override def deleteComputer(id: Computer.Id): F[Option[Computer]] =
        (for {
          computer <- OptionT(computerRepository.fetchOne(id))
          _        <- OptionT.liftF(Sync[F].whenA(!readOnlyComputers.contains(id))(computerRepository.deleteOne(id)))
        } yield computer).value

      override def loadDefaultComputers: F[Unit] =
        computerRepository.loadAll(initialComputers)
    }
}
