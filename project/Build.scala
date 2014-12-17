import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbtfilter.Plugin.FilterKeys._
import scoverage.ScoverageSbtPlugin._

object Build extends sbt.Build {

  lazy val avpath = Project("wandou-avpath", file("."))
    .settings(basicSettings: _*)
    .settings(formatSettings: _*)
    .settings(releaseSettings: _*)
    .settings(sbtrelease.ReleasePlugin.releaseSettings: _*)
    .settings(libraryDependencies ++= Dependencies.avro ++ Dependencies.test)
    .settings(XitrumPackage.skip: _*)
    .settings(sbtavro.SbtAvro.avroSettings ++ avroSettingsTest: _*)
    .settings(instrumentSettings: _*)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

  lazy val basicSettings = Seq(
    organization := "com.wandoulabs.avro",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.4",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers ++= Seq(
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")) 

  lazy val avroSettings = Seq(
    sbtavro.SbtAvro.stringType in sbtavro.SbtAvro.avroConfig := "String",
    sourceDirectory in sbtavro.SbtAvro.avroConfig <<= (resourceDirectory in Compile)(_ / "avsc"),
    version in sbtavro.SbtAvro.avroConfig := "1.7.5"
  )

  // Todo rewrite sbt-avro to compile in Test phase.
  lazy val avroSettingsTest = Seq(
    sbtavro.SbtAvro.stringType in sbtavro.SbtAvro.avroConfig := "String",
    sourceDirectory in sbtavro.SbtAvro.avroConfig <<= (resourceDirectory in Test)(_ / "avsc"),
    javaSource in sbtavro.SbtAvro.avroConfig <<= (sourceManaged in Test)(_ / "java" / "compiled_avro"),
    version in sbtavro.SbtAvro.avroConfig := "1.7.5"
  )

  lazy val releaseSettings = Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { (repo: MavenRepository) => false },
    pomExtra := pomXml)

  lazy val pomXml = (<url>https://github.com/wandoulabs/wandou-math</url>
      <licenses>
        <license>
          <name>Apache License 2.0</name>
          <url>http://www.apache.org/licenses/</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:wandoulabs/wandou-avpath.git</url>
        <connection>scm:git:git@github.com:wandoulabs/wandou-avpath.git</connection>
      </scm>)

  lazy val noPublishing = Seq(
    publish :=(),
    publishLocal :=(),
    // required until these tickets are closed https://github.com/sbt/sbt-pgp/issues/42,
    // https://github.com/sbt/sbt-pgp/issues/36
    publishTo := None
  )

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test := formattingPreferences)

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(RewriteArrowSymbols, false)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(IndentSpaces, 2)
  }
}

object Dependencies {
  val SLF4J_VERSION = "1.7.7"

  val log = Seq(
    "org.slf4j" % "slf4j-api" % SLF4J_VERSION,
    "org.slf4j" % "jcl-over-slf4j" % SLF4J_VERSION,
    "org.slf4j" % "log4j-over-slf4j" % SLF4J_VERSION,
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )

  val test = Seq(
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2-RC1" % "test",
    "org.scalatest" %% "scalatest" % "2.1.3" % "test"
  )

  val avro = Seq(
    "org.apache.avro" % "avro" % "1.7.6"
  )

  val basic: Seq[ModuleID] = log ++ test ++ avro

  val all = basic
}

