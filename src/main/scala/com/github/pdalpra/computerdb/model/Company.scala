package com.github.pdalpra.computerdb.model

import cats.implicits._
import cats.{ derived, Eq }
import eu.timepit.refined.cats._
import io.circe.Decoder
import io.circe.refined._

object Company {
  object Id {
    implicit val eq: Eq[Id]           = derived.semi.eq
    implicit val decoder: Decoder[Id] = Decoder[UniqueId].map(Id.apply)
  }
  final case class Id(value: UniqueId) {
    override def toString: String = value.toString()
  }
}
final case class Company(
    id: Company.Id,
    name: NonEmptyString
)
