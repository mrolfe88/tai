import sbt._

object MicroServiceBuild extends Build with MicroService {

  import play.sbt.routes.RoutesKeys._

  import scala.util.Properties._

  val appName = "tai"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
  override lazy val playSettings: Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.tai.binders._", "uk.gov.hmrc.domain._"))

}

private object AppDependencies {
  import play.sbt.PlayImport._
  private val pegdownVersion = "1.6.0"
  private val scalatestVersion = "2.2.6"

  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.40.0",
    "uk.gov.hmrc" %% "domain" % "5.6.0-play-26",
    "uk.gov.hmrc" %% "json-encryption" % "3.2.0",
    "uk.gov.hmrc" %% "mongo-caching" % "6.1.0-play-26" exclude("uk.gov.hmrc","time_2.11"),
    "com.typesafe.play" %% "play-json-joda" % "2.6.10"
  )

  trait TestDependencies {
    lazy val scope: String = "test,it"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.8.0-play-26" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.7.3" % scope,
        "org.scalacheck" %% "scalacheck" % "1.12.5" % scope,
        "org.mockito" % "mockito-core" % "1.9.5",
        "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()

}


