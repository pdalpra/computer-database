val acyclic = "com.lihaoyi" %% "acyclic" % "0.2.0"

libraryDependencies += acyclic % "provided"
autoCompilerPlugins := true
addCompilerPlugin(acyclic)

scalacOptions += "-P:acyclic:force"
