name := """playing-with-java-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  evolutions,
  "io.javaslang" % "javaslang" % "2.0.4",
  "org.postgresql" % "postgresql" % "9.4.1211",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.revinate" % "assertj-json" % "1.0.1" % "test"
)


fork in run := true