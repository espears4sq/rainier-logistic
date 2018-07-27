ThisBuild / scalaVersion := "2.12.6"
ThisBuild / organization := "com.foursquare.espears"

lazy val ranier_logistic = (project in file("."))
  .settings(
    name := "ranier-logistic",
    resolvers += Resolver.bintrayRepo("cibotech", "public"),
    libraryDependencies += "com.cibo" %% "evilplot" % "0.2.0",
    libraryDependencies += "com.stripe" %% "rainier-core" % "0.1.1",
    libraryDependencies += "com.stripe" %% "rainier-plot" % "0.1.1"
  )
