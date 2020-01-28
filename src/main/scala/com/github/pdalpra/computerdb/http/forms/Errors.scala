package com.github.pdalpra.computerdb.http.forms

final case class FieldError(fieldName: String, error: Error)

class Error(message: String) extends Exception(message)

final class MissingField(field: String)         extends Error(s"Field $field is missing from form.")
final class NumberParseException(ex: Exception) extends Error(s"Failed to decode number : $ex")
final class DateParseException(ex: Exception)   extends Error(s"Failed to decode date : $ex")
final class RefinedException(message: String)   extends Error(s"Failed to refine type : $message")
