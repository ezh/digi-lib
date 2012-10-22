//
// Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
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

description := "Base library for digi components"

organization := "org.digimead"

version <<= (baseDirectory) { (b) => scala.io.Source.fromFile(b / "version").mkString.trim }

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.8.2", "2.9.0", "2.9.0-1", "2.9.1", "2.9.2")

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-Xcheckinit") ++
  (if (true || (System getProperty "java.runtime.version" startsWith "1.7")) Seq() else Seq("-optimize")) // -optimize fails with jdk7

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

compileOrder := CompileOrder.JavaThenScala

publishTo <<= baseDirectory { (base) => Some(Resolver.file("file",  base / "publish/releases" )) }

resolvers += ("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= {
  Seq(
    "org.slf4j" % "slf4j-api" % "1.7.1",
    "org.aspectj" % "aspectjrt" % "1.6.12",
    "com.escalatesoft.subcut" %% "subcut" % "2.0-SNAPSHOT"
  )
}

sourceDirectory in Test  <<= baseDirectory / "Testing Infrastructure Is Absent"
