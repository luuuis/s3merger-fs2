name := "s3merger-fs2"
version := "0.1"
scalaVersion := "2.13.2"

// https://github.com/functional-streams-for-scala/fs2#getit
libraryDependencies += "co.fs2" %% "fs2-core" % "2.2.1" // For cats 2 and cats-effect 2
libraryDependencies += "co.fs2" %% "fs2-io" % "2.2.1"

// https://github.com/lendup/fs2-blobstore#installing
libraryDependencies ++= Seq(
  "com.github.fs2-blobstore" %% "core"  % "0.7.2",
  "com.github.fs2-blobstore" %% "s3"    % "0.7.2",
)

// https://github.com/aws/aws-sdk-java#importing-the-bom
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-bom" % "1.11.786"
)

// log to stdout
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.28" % "runtime"
)

// https://github.com/typelevel/squants#installation
libraryDependencies +=  "org.typelevel"  %% "squants"  % "1.6.0"

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
