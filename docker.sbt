enablePlugins(BuildInfoPlugin, JavaAppPackaging, DockerPlugin)
enablePlugins(DockerSpotifyClientPlugin)

dockerBaseImage := "openjdk:11-jre-slim"
dockerExposedPorts := Seq(8080)

javaOptions in Universal ++= Seq(
  "-J-XX:+PrintCommandLineFlags",
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+EnableJVMCI",
  "-J-XX:+UseJVMCICompiler",
  "-Dgraal.GraalArithmeticStubs=false"
)
