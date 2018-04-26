name := "facial-recog"
organization := "org.self"
version := "1.0.0"
scalaVersion := "2.11.8"
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-optimize",
  "-Xlint"
)

//javaCppPresetLibs ++= Seq("opencv" -> "3.2.0", "ffmpeg" -> "3.2.1")

val javacppVersion = "1.3"

lazy val platform: String = {
  sys.props.get("platform") match {
    case Some(s) =>
      s match {
        case "android-arm" | "android-x86" | "linux-armhf" | "linux-ppc64le" |
            "linux-x86" | "linux-x86_64" | "macosx-x86_64" | "windows-x86" |
            "windows-x86_64" =>
          s
        case _ => sys.error("unsupported platform")
      }
    case _ => org.bytedeco.javacpp.Loader.getPlatform
  }
}

// Libraries with native dependencies
val bytedecoPresetLibs =
  Seq("opencv" -> "3.2.0", "ffmpeg" -> "3.2.1").flatMap {
    case (lib, ver) =>
      Seq(
        // Add both: dependency and its native binaries for the current `platform`
        "org.bytedeco.javacpp-presets" % lib % (ver + s"-$javacppVersion") withSources () withJavadoc (),
        "org.bytedeco.javacpp-presets" % lib % (ver + s"-$javacppVersion") classifier platform
      )
  }

val akkaVersion = "2.4.20"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"  % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.bytedeco" % "javacpp" % javacppVersion withSources () withJavadoc (),
  "org.bytedeco" % "javacv" % javacppVersion withSources () withJavadoc (),
  "org.scala-lang.modules" %% "scala-swing" % "2.0.3",
  "net.imagej" % "ij" % "1.52a"
) ++ bytedecoPresetLibs

autoCompilerPlugins := true

fork := true

javaOptions += "-Xmx1G"

shellPrompt in ThisBuild := { state =>
  "sbt:" + Project.extract(state).currentRef.project + "> "
}
