val acyclic = "com.lihaoyi" %% "acyclic" % "0.2.1"

libraryDependencies += acyclic % "provided"
autoCompilerPlugins := true
addCompilerPlugin(acyclic)

scalacOptions += "-P:acyclic:force"
