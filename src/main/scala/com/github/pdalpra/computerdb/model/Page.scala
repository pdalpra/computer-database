package com.github.pdalpra.computerdb.model

import cats.implicits._
import eu.timepit.refined.api._
import eu.timepit.refined.numeric._

object Page {
  type Number = Int Refined NonNegative
  type Size   = Int Refined NonNegative

  object Number extends RefinedTypeOps[Number, Int]
  object Size   extends RefinedTypeOps[Number, Int]

  val DefaultPage: Number = Number.unsafeFrom(0)
}
final case class Page[A](items: List[A], page: Page.Number, offset: Int, total: Int) {

  def prev: Option[Page.Number] =
    Page.Number.from(page.value - 1).toOption

  def next: Option[Page.Number] =
    if (offset + items.size < total)
      Page.Number.unsafeFrom(page.value + 1).some
    else
      None
}
