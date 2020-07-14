package com.github.pdalpra.computerdb.http.html

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.github.pdalpra.computerdb.db.ComputerSort
import com.github.pdalpra.computerdb.model._
import com.github.pdalpra.computerdb.service.ComputerListParameters

import cats.implicits._
import cats.Show
import eu.timepit.refined.cats._
import scalatags.Text._
import scalatags.Text.all._

private[html] object ComputersListView {

  private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

  implicit val localDateShow: Show[LocalDate] = Show.show(dateFormatter.format)

  final case class Context(page: Page[Computer], parameters: ComputerListParameters)

  def computersList(context: Context, flashData: Option[String]): TypedTag[String] =
    PageTemplate.pageTemplate(
      h1(title(context.page)),
      flashData.map(div(`class` := "alert-message warning")(strong("Done !  "), _)),
      computerActions(context.parameters.nameFilter),
      computerTable(context),
      pageNavigation(context)
    )

  private def computerActions(searchQuery: Option[NonEmptyString]) = {
    val currentFilter = searchQuery.map(_.value).getOrElse("")

    div(id := "actions")(
      form(action := "/computers", method := "GET")(
        input(`type` := "search", id := "searchbox", name := "f", value := currentFilter, placeholder := "Filter by computer name..."),
        "  ",
        input(`type` := "submit", id := "searchsubmit", value := "Filter by name", `class` := "btn primary"),
        a(`class` := "btn success", id := "add", href := "/computers/new")("Add a new computer")
      )
    )
  }

  private def title(computers: Page[Computer]) =
    computers.total match {
      case 0 => "No computer"
      case 1 => "One computer found"
      case n => s"$n computers found"
    }

  private def computerTable(context: Context) =
    if (context.page.items.isEmpty)
      div(`class` := "well")(em("Nothing to display"))
    else
      table(`class` := "computers zebra-striped")(
        thead(
          header("Computer name", "name", context, ComputerSort.Name),
          header("Introduced", "introduced", context, ComputerSort.Introduced),
          header("Discontinued", "discontinued", context, ComputerSort.Discontinued),
          header("Company", "company", context, ComputerSort.CompanyName)
        ),
        tbody(context.page.items.map { computer =>
          tr(
            td(a(href := s"/computers/${computer.id.show}")(computer.name.show)),
            td(computer.introduced.fold("-")(_.show)),
            td(computer.discontinued.fold("-")(_.show)),
            td(computer.company.fold("-")(_.name.show))
          )
        })
      )

  private def pageNavigation(context: Context) =
    div(id := "pagination", `class` := "pagination")(
      ul(
        pageNavButton(context.page.previous, context, "← Previous", "prev"),
        li(`class` := "current")(
          a(s"Displaying ${context.page.offset + 1} to ${context.page.offset + context.page.items.size} of ${context.page.total}")
        ),
        pageNavButton(context.page.next, context, "Next →", "next")
      )
    )

  private def pageNavButton(pagePointer: Option[Page.Number], context: Context, message: String, cssClass: String) =
    pagePointer
      .map(page => li(`class` := cssClass)(a(href := pageLink(page, context, None, None))(message)))
      .getOrElse(li(`class` := s"$cssClass disabled")(a(message)))

  private def header(name: String, columnClass: String, context: Context, sort: ComputerSort) = {
    val (newSort, newOrder, orderClass) = context.parameters.sort match {
      case `sort` => (None, context.parameters.order.inverse.some, orderCssClass(context.parameters.order))
      case _      => (sort.some, None, "")
    }

    th(`class` := s"col-$columnClass header $orderClass")(
      a(href := pageLink(Page.DefaultPage, context, newSort, newOrder))(name)
    )
  }

  private def pageLink(targetPage: Page.Number, context: Context, newSort: Option[ComputerSort], newOrder: Option[Order]) = {
    val sort    = newSort.getOrElse(context.parameters.sort)
    val order   = newOrder.getOrElse(context.parameters.order)
    val baseUrl = s"/computers?p=$targetPage&n=${context.parameters.size}&s=${sort.show}&d=${order.show}"
    context.parameters.nameFilter.map(filter => s"$baseUrl&f=$filter").getOrElse(baseUrl)
  }

  private def orderCssClass(order: Order) =
    order match {
      case Order.Asc  => "headerSortUp"
      case Order.Desc => "headerSortDown"
    }
}
