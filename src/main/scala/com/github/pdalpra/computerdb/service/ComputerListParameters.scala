package com.github.pdalpra.computerdb.service

import com.github.pdalpra.computerdb.db.ComputerSort
import com.github.pdalpra.computerdb.model.{ NonEmptyString, Order, Page }

object ComputerListParameters {

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(
      number: Option[Page.Number],
      size: Option[Page.Size],
      sort: Option[ComputerSort],
      order: Option[Order],
      nameFilter: Option[NonEmptyString]
  ): ComputerListParameters =
    new ComputerListParameters(
      number.getOrElse(Page.DefaultPage),
      size.getOrElse(Page.DefaultPageSize),
      sort.getOrElse(ComputerSort.Name),
      order.getOrElse(Order.Asc),
      nameFilter
    )
}
final case class ComputerListParameters private (
    number: Page.Number,
    size: Page.Size,
    sort: ComputerSort,
    order: Order,
    nameFilter: Option[NonEmptyString]
)
