name := "s3merger-fs2"
version := "0.1"
scalaVersion := "2.13.2"

// https://github.com/functional-streams-for-scala/fs2#getit
libraryDependencies += "co.fs2" %% "fs2-core" % "2.2.1" // For cats 2 and cats-effect 2
libraryDependencies += "co.fs2" %% "fs2-io" % "2.2.1"

// https://github.com/laserdisc-io/fs2-aws#using
libraryDependencies +=  "io.laserdisc" %% "fs2-aws" % "2.28.39"

// https://github.com/typelevel/squants#installation
libraryDependencies +=  "org.typelevel"  %% "squants"  % "1.6.0"

// Gist doesn't support directories :(
scalaSource := baseDirectory.value

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same line
  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
