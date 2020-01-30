package org.mulesoft.language.outline.test

import amf.client.remote.Content
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.als.CompilerEnvironment
import org.mulesoft.als.common.diff.FileAssertionTest
import org.mulesoft.amfintegration.AmfInstance
import org.mulesoft.amfmanager.{AmfParseResult, DialectInitializer, InitOptions}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

object File {
  val FILE_PROTOCOL = "file://"

  def unapply(url: String): Option[String] = {
    url match {
      case s if s.startsWith(FILE_PROTOCOL) =>
        val path = s.stripPrefix(FILE_PROTOCOL)
        Some(path)
      case _ => None
    }
  }
}

trait OutlineTest[T] extends AsyncFunSuite with FileAssertionTest with PlatformSecrets {

  implicit override def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def readDataFromAST(unit: BaseUnit, position: Int): T

  def writeDataToString(data: T): String

  def emptyData(): T

  def runTest(path: String, jsonPath: String): Future[Assertion] = {

    val fullFilePath = filePath(platform.encodeURI(path)) // filePath(path)
    val fullJsonPath = filePath(jsonPath)
    val amfConfig    = AmfInstance.default
    for {
      _             <- DialectInitializer.init(InitOptions.AllProfiles, amfConfig)
      actualOutline <- this.getActualOutline(fullFilePath, platform, amfConfig)
      tmp           <- writeTemporaryFile(jsonPath)(writeDataToString(actualOutline))
      r             <- assertDifferences(tmp, fullJsonPath)

    } yield r
  }

  def rootPath: String

  def bulbLoaders(path: String, content: String): Seq[ResourceLoader] = {
    var loaders: Seq[ResourceLoader] = List(new ResourceLoader {
      override def accepts(resource: String): Boolean = resource == path

      override def fetch(resource: String): Future[Content] = Future.successful(new Content(content, path))
    })
    Environment().loaders
  }

  def getExpectedOutline(url: String): Future[String] =
    this.platform.resolve(url, Environment(this.platform.loaders)).map(_.stream.toString)

  def getActualOutline(url: String,
                       platform: Platform,
                       compilerEnvironment: CompilerEnvironment[AmfParseResult, Environment]): Future[T] = {

    var position = 0

    var contentOpt: Option[String] = None
    platform
      .resolve(url)
      .map(content => {

        val fileContentsStr = content.stream.toString
        val markerInfo      = this.findMarker(fileContentsStr)

        position = markerInfo.position
        contentOpt = Some(markerInfo.content)
        var env = this.buildEnvironment(url, markerInfo.content, position, content.mime)
        env
      })
      .flatMap(env => {
        compilerEnvironment.modelBuiler().parse(url, env).map(_.baseUnit)
      })
      .map {
        case amfUnit: BaseUnit =>
          readDataFromAST(amfUnit, position)
        case _ =>
          emptyData()
      } recoverWith {
      case e: Throwable =>
        println(e)
        Future.successful(emptyData())
      case _ => Future.successful(emptyData())
    }
  }

  def buildEnvironment(fileUrl: String, content: String, position: Int, mime: Option[String]): Environment = {

    var loaders: Seq[ResourceLoader] = List(new ResourceLoader {
      override def accepts(resource: String): Boolean = resource == fileUrl

      override def fetch(resource: String): Future[Content] = Future.successful(new Content(content, fileUrl))
    })
    var env: Environment = Environment()
    env
  }

  def filePath(path: String): String = {
    s"file://als-structure/shared/src/test/resources/$rootPath/$path".replace('\\', '/').replace("null/", "")
  }

  def findMarker(str: String, label: String = "*", cut: Boolean = true): MarkerInfo = {

    var position = str.indexOf(label)

    if (position < 0) {
      new MarkerInfo(str, str.length)
    } else {
      var rawContent = str.substring(0, position) + str.substring(position + 1)
      new MarkerInfo(rawContent, position)
    }

  }

}

class MarkerInfo(val content: String, val position: Int) {}
