name := """playing-with-java-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice,
  "io.vavr" % "vavr" % "0.9.0",
  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.opencsv" % "opencsv" % "4.0",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.revinate" % "assertj-json" % "1.0.1" % "test",
  "com.h2database" % "h2" % "1.4.192" % "test"
)

javaOptions in Test += "-Dconfig.file=conf/application-test.conf"

playEbeanDebugLevel := 4

fork in run := true
