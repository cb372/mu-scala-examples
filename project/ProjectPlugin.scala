import sbt.Keys._
import sbt._
import sbtorgpolicies.OrgPoliciesPlugin
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model._
import sbtorgpolicies.templates._
import sbtorgpolicies.templates.badges._
import sbtorgpolicies.runnable.syntax._

import scala.language.reflectiveCalls

object ProjectPlugin extends AutoPlugin {

  override def requires: Plugins = OrgPoliciesPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val V = new {
      val catsEffect: String          = "2.1.2"
      val circe: String               = "0.13.0"
      val frees: String               = "0.8.2"
      val fs2: String                 = "2.2.2"
      val kindProjector: String       = "0.10.3"
      val log4cats: String            = "1.0.1"
      val log4s: String               = "1.8.2"
      val logback: String             = "1.2.3"
      val monix: String               = "3.1.0"
      val mu                          = "0.20.1"
      val paradise: String            = "2.1.1"
      val pureconfig: String          = "0.12.3"
      val scala212: String            = "2.12.10"
      val scopt: String               = "3.7.1"
      val scalatest: String           = "3.1.1"
      val scalatestplusScheck: String = "3.1.0.0-RC2"
      val slf4j: String               = "1.7.30"
    }

    def mu(module: String) = "io.higherkindness" %% module % V.mu

    lazy val macroSettings: Seq[Setting[_]] = {

      def paradiseDependency(sv: String): Seq[ModuleID] =
        if (isOlderScalaVersion(sv)) {
          Seq(
            compilerPlugin(
              ("org.scalamacros" % "paradise" % V.paradise).cross(CrossVersion.patch)
            )
          )
        } else Seq.empty

      def macroAnnotationScalacOption(sv: String): Seq[String] =
        if (isOlderScalaVersion(sv)) Seq.empty
        else Seq("-Ymacro-annotations")

      Seq(
        libraryDependencies ++= Seq(
          scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided
        ) ++ paradiseDependency(scalaVersion.value),
        scalacOptions ++= macroAnnotationScalacOption(scalaVersion.value)
      )
    }

    lazy val healthCheckSettingsFS2: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.chrisdavenport" %% "log4cats-core"  % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
        %%("fs2-core", V.fs2),
        %%("cats-effect", V.catsEffect)
      )
    )

    lazy val healthCheckSettingsMonix: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.chrisdavenport" %% "log4cats-core"  % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
        %%("monix", V.monix),
        %%("cats-effect", V.catsEffect)
      )
    )

    lazy val exampleRouteguideRuntimeSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        %%("monix", V.monix)
      )
    )

    lazy val exampleRouteguideCommonSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        %%("circe-core", V.circe),
        %%("circe-generic", V.circe),
        %%("circe-parser", V.circe),
        %%("log4s", V.log4s),
        %("logback-classic", V.logback)
      )
    )

    lazy val exampleSeedLogSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        %("logback-classic", V.logback),
        "io.chrisdavenport" %% "log4cats-core"  % V.log4cats,
        "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats
      )
    )

    lazy val exampleSeedConfigSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "org.typelevel"         %% "cats-effect" % V.catsEffect,
        "com.github.pureconfig" %% "pureconfig"  % V.pureconfig
      )
    )

    lazy val exampleSeedClientAppSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % V.scopt
      )
    )

    lazy val exampleTodolistCommonSettings: Seq[Def.Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        "io.frees" %% "frees-todolist-lib" % V.frees,
        %%("log4s", V.log4s),
        %("logback-classic", V.logback)
      )
    )

    lazy val noCrossCompilationLastScala: Seq[Def.Setting[_]] = Seq(
      scalaVersion := V.scala212,
      crossScalaVersions := Seq(V.scala212)
    )

    def isOlderScalaVersion(sv: String): Boolean =
      CrossVersion.partialVersion(sv) match {
        case Some((2, minor)) if minor < 13 => true
        case _                              => false
      }

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      description := "mu-scala-examples https://github.com/higherkindness/mu-scala",
      startYear := Some(2020),
      orgProjectName := "mu-scala-examples",
      orgGithubSetting := GitHubSettings(
        organization = "higherkindness",
        project = (name in LocalRootProject).value,
        organizationName = "47 Degrees",
        groupId = "io.higherkindness",
        organizationHomePage = url("http://47deg.com"),
        organizationEmail = "hello@47deg.com"
      ),
      scalaVersion := V.scala212,
      crossScalaVersions := Seq(V.scala212), // , V.scala213), until next mu release
      scalacOptions --= Seq("-Xfuture", "-Xfatal-warnings"),
      Test / fork := true,
      addCompilerPlugin(%%("kind-projector", V.kindProjector) cross CrossVersion.binary),
      libraryDependencies ++= Seq(
        %%("scalatest", V.scalatest) % Test,
        %("slf4j-nop", V.slf4j)      % Test
      )
    ) ++ Seq(
      orgMaintainersSetting := List(
        Dev("developer47deg", Some("47 Degrees (twitter: @47deg)"), Some("hello@47deg.com"))
      )
    ) ++ macroSettings
}
