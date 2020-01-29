wartremoverErrors in (Compile, compile) := Warts.allBut(
  Wart.Any,
  Wart.ImplicitParameter,
  Wart.Nothing,
  Wart.StringPlusAny // Buggy with Scala 2.13
)

wartremoverErrors in (Test, compile) := (wartremoverErrors in (Compile, compile)).value.diff(
  Seq(
    Wart.DefaultArguments,
    Wart.NonUnitStatements
  )
)

wartremoverExcluded += sourceManaged.value // Exclude generated code

scalacOptions in (Compile, console) := (scalacOptions in (Compile, console)).value.filterNot(_.contains("wartremover"))
