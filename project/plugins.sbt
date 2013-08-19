// scalikejdbc mapper generator
libraryDependencies += "com.h2database" % "h2" % "1.3.173"

addSbtPlugin("com.github.seratch" %% "scalikejdbc-mapper-generator" % "1.6.7")

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.3")

