name := """playing-with-java-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava).disablePlugins(PlayLayoutPlugin)
PlayKeys.playMonitoredFiles ++= (sourceDirectories in(Compile, TwirlKeys.compileTemplates)).value

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)


fork in run := true