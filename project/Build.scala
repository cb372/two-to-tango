import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "two-to-tango"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.github.seratch" %% "scalikejdbc"             % "[1.6,)",
    "com.github.seratch" %% "scalikejdbc-interpolation" % "[1.6,)",
    "com.github.seratch" %% "scalikejdbc-play-plugin" % "[1.6,)",
    "com.h2database"     %  "h2"                      % "1.3.173"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
