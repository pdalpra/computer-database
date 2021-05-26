wartremoverPluginJarsDir := None

Compile / wartremoverErrors := Warts.allBut(
  Wart.Any,
  Wart.ImplicitParameter,
  Wart.Nothing,
  Wart.StringPlusAny, // Buggy with Scala 2.13
)

Test / wartremoverErrors := (Compile / wartremoverErrors)
  .value
  .diff(
    Seq(
      Wart.DefaultArguments,
      Wart.NonUnitStatements,
    )
  )

wartremoverExcluded += sourceManaged.value // Exclude generated code

Compile / console / scalacOptions := (Compile / scalacOptions).value.filterNot(_.contains("wartremover"))
