name := """wooster"""

version := "1.0-SNAPSHOT"

organization := "com.github.enpassant"

scalaVersion := "2.11.8"

resolvers += "spray repo" at "http://repo.spray.io"

val sprayVersion = "1.3.1"

val akkaVersion = "2.4.2"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

Revolver.settings

enablePlugins(JavaAppPackaging)

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= Seq(
  "com.github.scopt"       %% "scopt"                   % "3.3.0",
  "com.typesafe.akka"      %% "akka-actor"              % akkaVersion,
  "com.typesafe.akka"      %% "akka-http-experimental"  % akkaVersion,
  "com.github.nscala-time" %% "nscala-time"             % "1.8.0",
  "org.json4s"             %% "json4s-jackson"          % "3.2.10",
  "org.json4s"             %% "json4s-ext"              % "3.2.10",
  "org.webjars"             % "requirejs"               % "2.1.22",
  "org.webjars"             % "Semantic-UI"             % "2.1.8",
  "org.webjars"             % "mithril"                 % "0.2.3",
  "org.webjars"             % "jquery"                  % "2.2.1",
  "org.webjars"             % "immutable"               % "3.7.3",
  "com.github.rjeschke"     % "txtmark"                 % "0.13",
  "io.spray"               %% "spray-testkit"           % sprayVersion   % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

lazy val dev = config("dev") describedAs("dev environment settings")
lazy val prod = config("prod") describedAs("prod environment settings")

import com.typesafe.sbt.web.pipeline.Pipeline

lazy val prodSettings = Seq(
    RjsKeys.optimize := "ugilfy2"
)

lazy val devSettings = Seq(
    RjsKeys.optimize := "none"
)

lazy val root = (project.in(file("."))).enablePlugins(SbtWeb).configs(dev, prod).
  settings(inConfig(prod)(prodSettings): _*).
  settings(inConfig(dev)(devSettings): _*)

(managedClasspath in Runtime) += (packageBin in Assets).value

WebKeys.packagePrefix in Assets := "public/"

//unmanagedResourceDirectories in Compile += (WebKeys.public in Assets).value

//RjsKeys.mainModule := "build"

pipelineStages := Seq(rjs)

//pipelineStages := Seq(rjs, digest, gzip)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://github.com/enpassant/wooster</url>
  <licenses>
    <license>
      <name>Apache-style</name>
      <url>http://opensource.org/licenses/Apache-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:enpassant/wooster.git</url>
    <connection>scm:git:git@github.com:enpassant/wooster.git</connection>
  </scm>
  <developers>
    <developer>
      <id>fkalman</id>
      <name>Enpassant</name>
      <url>http://github.com/enpassant</url>
    </developer>
  </developers>)
