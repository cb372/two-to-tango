import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "two-to-tango"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.github.seratch"   %% "scalikejdbc"               % "[1.6,)",
    "com.github.seratch"   %% "scalikejdbc-config"        % "[1.6,)",
    "com.github.seratch"   %% "scalikejdbc-interpolation" % "[1.6,)",
    "com.github.seratch"   %% "scalikejdbc-play-plugin"   % "[1.6,)",
    "com.github.seratch"   %% "scalikejdbc-test"          % "[1.6,)"       % "test",
    "com.typesafe"         %% "play-plugins-mailer"       % "2.1.0",
    "com.h2database"       %  "h2"                        % "1.3.173",
    "com.github.tototoshi" %% "play-flyway"               % "0.1.4",
    "org.specs2"           %% "specs2"                    % "2.1.1"        % "test",
    "com.newrelic.agent.java" % "newrelic-agent"          % "2.21.4"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )

}
