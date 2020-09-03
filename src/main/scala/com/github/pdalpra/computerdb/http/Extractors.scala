package com.github.pdalpra.computerdb.http

import com.github.pdalpra.computerdb.db.ComputerSort
import com.github.pdalpra.computerdb.model._

import cats.syntax.all._
import eu.timepit.refined.api._
import mouse.string._
import org.http4s.{ ParseFailure, ParseResult, QueryParamDecoder }
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

private[http] trait Extractors {

  implicit def refinedDecoder[T: QueryParamDecoder, P, F[_, _]: RefType](implicit
      validate: Validate[T, P]
  ): QueryParamDecoder[F[T, P]] =
    QueryParamDecoder[T]
      .emap(RefType[F].refine(_).leftMap(ParseFailure("Failed to decode refined type", _)))

  private implicit val computerSortDecoder: QueryParamDecoder[ComputerSort] =
    QueryParamDecoder[String]
      .emap(
        ComputerSort
          .withValueEither(_)
          .fold(err => ParseResult.fail("sort", err.getMessage()), ParseResult.success)
      )

  private implicit val orderDecoder: QueryParamDecoder[Order] =
    QueryParamDecoder[String]
      .emap(
        Order
          .withNameEither(_)
          .fold(err => ParseResult.fail("order", err.getMessage()), ParseResult.success)
      )

  private val searchQueryDecoder: QueryParamDecoder[Option[NonEmptyString]] =
    QueryParamDecoder[String].map(NonEmptyString.from(_).toOption)

  object PageNumber  extends OptionalQueryParamDecoderMatcher[Page.Number]("p")
  object PageSize    extends OptionalQueryParamDecoderMatcher[Page.Size]("n")
  object Sort        extends OptionalQueryParamDecoderMatcher[ComputerSort]("s")
  object SortOrder   extends OptionalQueryParamDecoderMatcher[Order]("d")
  object SearchQuery extends OptionalQueryParamDecoderMatcher[Option[NonEmptyString]]("f")(searchQueryDecoder)

  object ComputerId {
    def unapply(str: String): Option[Computer.Id] =
      str.parseLongOption.flatMap(UniqueId.from(_).toOption).map(Computer.Id.apply)
  }
}
