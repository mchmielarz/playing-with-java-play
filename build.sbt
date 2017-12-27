resolvers += "jcenter" at "https://jcenter.bintray.com"

name := """playing-with-java-play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean, ProtobufPlugin)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice,
  "io.vavr" % "vavr" % "0.9.1",
  "org.postgresql" % "postgresql" % "9.4.1211",
  "com.opencsv" % "opencsv" % "4.0",
  "com.google.protobuf" % "protobuf-java-util" % "3.4.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % "test",
  "org.assertj" % "assertj-core" % "3.5.2" % "test",
  "com.revinate" % "assertj-json" % "1.0.1" % "test",
  "org.xmlunit" % "xmlunit-core" % "2.5.1" % "test",
  "org.xmlunit" % "xmlunit-matchers" % "2.5.1" % "test",
  "org.hamcrest" % "hamcrest-library" % "1.3" % "test",
  "org.mockito" % "mockito-core" % "2.12.0" % "test",
  "com.h2database" % "h2" % "1.4.192" % "test"
)

javaOptions in Test += "-Dconfig.file=conf/application-test.conf"

playEbeanDebugLevel := 4

fork in run := true

sourceDirectory in ProtobufConfig := baseDirectory.value / "protobuf"
javaSource in ProtobufConfig := baseDirectory.value / "generated"
