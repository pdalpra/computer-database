package com.github.pdalpra.computerdb.http.html

import java.time.LocalDate

import com.github.pdalpra.computerdb.http.forms.FieldError
import com.github.pdalpra.computerdb.model._

import cats.data.NonEmptyChain
import cats.implicits._
import org.http4s.UrlForm
import scalatags.Text._
import scalatags.Text.all._

object Forms {
  final case class InvalidFormState(form: UrlForm, errors: NonEmptyChain[FieldError])

  final case class FormContext(
      name: Option[NonEmptyString],
      introduced: Option[LocalDate],
      discontinued: Option[LocalDate],
      companyId: Option[Company.Id],
      companies: List[Company],
      invalidFormState: Option[InvalidFormState]
  )

  def creationForm(companies: List[Company], formState: Option[InvalidFormState]): TypedTag[String] = {
    val context = FormContext(None, None, None, None, companies, formState: Option[InvalidFormState])

    PageTemplate.pageTemplate(
      h1("Add a computer"),
      computerForm(s"/computers", "Create this computer", context)
    )
  }

  def editionForm(computer: Computer, companies: List[Company], formState: Option[InvalidFormState]): TypedTag[String] = {
    val context = FormContext(
      computer.name.some,
      computer.introduced,
      computer.discontinued,
      computer.company.map(_.id),
      companies,
      formState
    )

    PageTemplate.pageTemplate(
      h1("Edit computer"),
      computerForm(s"/computers/${computer.id}", "Save this computer", context),
      deleteComputerButton(computer)
    )
  }

  private def computerForm(targetUrl: String, saveMessage: String, context: FormContext) = {
    val name         = context.name.map(_.value).getOrElse("")
    val introduced   = context.introduced.map(_.toString()).getOrElse("")
    val discontinued = context.discontinued.map(_.toString()).getOrElse("")

    form(action := targetUrl, method := "POST")(
      fieldset(
        inputField("name", "Computer name", "Required", name, context.invalidFormState),
        inputField("introduced", "Introduced", "Date ('yyyy-MM-dd')", introduced, context.invalidFormState),
        inputField("discontinued", "Discontinued", "Date ('yyyy-MM-dd')", discontinued, context.invalidFormState),
        companiesSelect(context.companies, context.companyId)
      ),
      div(`class` := "actions")(
        input(`type` := "submit", value := saveMessage, `class` := "btn primary"),
        " or ",
        a(href := "/computers", `class` := "btn")("Cancel")
      )
    )
  }

  private def deleteComputerButton(computer: Computer) =
    form(`class` := "topRight", action := s"/computers/${computer.id}/delete", method := "POST")(
      input(`type` := "submit", value := "Delete this computer", `class` := "btn danger")
    )

  private def inputField(fieldName: String, fieldLabel: String, hint: String, defaultValue: String, formState: Option[InvalidFormState]) = {
    val enteredValue = formState.flatMap(_.form.getFirst(fieldName))
    val fieldError   = formState.flatMap(_.errors.find(_.fieldName === fieldName))
    val errorClass   = if (fieldError.isDefined) "error" else ""
    div(`class` := s"clearfix $errorClass")(
      label(`for` := fieldName)(fieldLabel),
      div(`class` := "input")(
        input(`type` := "text", id := fieldName, name := fieldName, value := enteredValue.getOrElse(defaultValue)),
        " ",
        span(`class` := "help-inline")(fieldError.fold(hint)(_.error.getMessage))
      )
    )
  }

  private def companiesSelect(companies: List[Company], current: Option[Company.Id]) =
    div(`class` := "clearfix")(
      label(`for` := "company")("Company"),
      div(`class` := "input")(
        select(id := "company", name := "company")(
          option(`class` := "blank", value := "")("-- Choose a company --"),
          companies.map { company =>
            val isSelected = current.collect { case id if id === company.id => selected := "" }
            option(value := company.id.toString(), isSelected)(company.name.toString())
          }
        )
      )
    )
}
