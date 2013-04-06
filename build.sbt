//
// Copyright (c) 2012-2013 Alexey Aksenov ezh@ezh.msk.ru
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

name := "Digi-Lib"

description := "Base library for Digi components"

organization := "org.digimead"

version <<= (baseDirectory) { (b) => scala.io.Source.fromFile(b / "version").mkString.trim }

crossScalaVersions := Seq("2.10.1")

scalaVersion := "2.10.1"

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-Xcheckinit", "-feature") ++
  (if (true || (System getProperty "java.runtime.version" startsWith "1.7")) Seq() else Seq("-optimize")) // -optimize fails with jdk7

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

compileOrder := CompileOrder.JavaThenScala

publishTo <<= baseDirectory { (base) => Some(Resolver.file("file",  base / "publish/releases" )) }

resolvers += ("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= {
  Seq(
    "com.escalatesoft.subcut" %% "subcut" % "2.0",
    "com.typesafe.akka" %% "akka-actor" % "2.1.2",
    "org.aspectj" % "aspectjrt" % "1.7.1",
    "org.slf4j" % "slf4j-api" % "1.7.1"
  )
}

sourceDirectory in Test  <<= baseDirectory / "Testing Infrastructure Is Absent"
