package org.mulesoft.als.suggestions.test.core

import amf.client.remote.Content
import amf.core.unsafe.PlatformSecrets
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.als.common.{MarkerFinderTest, PlatformDirectoryResolver}
import org.mulesoft.als.configuration.AlsConfiguration
import org.mulesoft.als.suggestions.client.Suggestions
import org.mulesoft.als.suggestions.interfaces.Syntax.YAML
import org.mulesoft.als.suggestions.patcher.{ContentPatcher, PatchedContent}
import org.mulesoft.amfintegration.AmfInstance
import org.mulesoft.lsp.feature.completion.CompletionItem
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

trait CoreTest extends AsyncFunSuite with PlatformSecrets with MarkerFinderTest {

  implicit override def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def rootPath: String

  def suggest(path: String, amfInstance: AmfInstance): Future[Seq[CompletionItem]] = {

    val url = filePath(path)
    for {
      content <- platform.resolve(url)
      (env, position) <- Future.successful {
        val fileContentsStr = content.stream.toString
        val markerInfo      = this.findMarker(fileContentsStr, "*")

        (this.buildEnvironment(url, markerInfo.content, content.mime), markerInfo.offset)
      }
      suggestions <- {
        new Suggestions(platform, env, AlsConfiguration(), new PlatformDirectoryResolver(platform), amfInstance)
          .initialized()
          .suggest(url, position, snippetsSupport = true, None)
      }
    } yield suggestions
  }

  def filePath(path: String): String =
    s"file://als-suggestions/shared/src/test/resources/test/$rootPath/$path"
      .replace('\\', '/')
      .replace("/null", "")

  def buildEnvironment(fileUrl: String, content: String, mime: Option[String]): Environment = {
    var loaders: Seq[ResourceLoader] = List(new ResourceLoader {
      override def accepts(resource: String): Boolean = resource == fileUrl

      override def fetch(resource: String): Future[Content] =
        Future.successful(new Content(content, fileUrl))
    })

    loaders ++= platform.loaders()

    Environment(loaders)
  }

  def runTestForCustomDialect(path: String, dialectPath: String, originalSuggestions: Set[String]): Future[Assertion] = {
    val p             = filePath(dialectPath)
    val configuration = AmfInstance.default
    configuration.init().flatMap { _ =>
      configuration
        .parse(p)
        .flatMap(_ =>
          suggest(path, amfInstance = configuration).map(suggestions => {
            assert(suggestions.map(_.label).size == originalSuggestions.size)
            assert(suggestions.map(_.label).forall(s => originalSuggestions.contains(s)))
            assert(originalSuggestions.forall(s => suggestions.map(_.label).contains(s)))
          }))
    }
  }
}
