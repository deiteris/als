package org.mulesoft.als.server.modules.reference

import org.mulesoft.als.common.dtoTypes.Position
import org.mulesoft.als.convert.LspRangeConverter
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.{
  LanguageServerBaseTest,
  LanguageServerBuilder,
  MockDiagnosticClientNotifier,
  ServerWithMarkerTest
}
import org.mulesoft.als.suggestions.interfaces.Syntax.YAML
import org.mulesoft.als.suggestions.patcher.{ContentPatcher, PatchedContent}
import org.mulesoft.lsp.feature.common.{Location, TextDocumentIdentifier}
import org.mulesoft.lsp.feature.implementation.{ImplementationParams, ImplementationRequestType}
import org.mulesoft.lsp.feature.reference.{ReferenceContext, ReferenceParams, ReferenceRequestType}
import org.scalatest.Assertion

import scala.concurrent.{ExecutionContext, Future}

trait ServerReferencesTest extends ServerWithMarkerTest[Seq[Location]] {

  override implicit val executionContext: ExecutionContext =
    ExecutionContext.Implicits.global

  override def rootPath: String = "actions/reference"

  def buildServer(): LanguageServer = {

    val factory =
      new WorkspaceManagerFactoryBuilder(new MockDiagnosticClientNotifier, logger).buildWorkspaceManagerFactory()
    new LanguageServerBuilder(factory.documentManager,
                              factory.workspaceManager,
                              factory.configurationManager,
                              factory.resolutionTaskManager)
      .addRequestModule(factory.referenceManager)
      .addRequestModule(factory.implementationManager)
      .build()
  }

  def runTest(path: String, expectedDefinitions: Set[Location]): Future[Assertion] =
    withServer[Assertion](buildServer()) { server =>
      val resolved = filePath(platform.encodeURI(path))
      for {
        content <- this.platform.resolve(resolved)
        definitions <- {
          val fileContentsStr = content.stream.toString
          val markerInfo      = this.findMarker(fileContentsStr)

          getAction(resolved, server, markerInfo)
        }
      } yield {
        assert(definitions.toSet == expectedDefinitions)
      }
    }

  def runTestImplementations(path: String, expectedDefinitions: Set[Location]): Future[Assertion] =
    withServer[Assertion](buildServer()) { server =>
      val resolved = filePath(platform.encodeURI(path))
      for {
        content <- this.platform.resolve(resolved)
        definitions <- {
          val fileContentsStr = content.stream.toString
          val markerInfo      = this.findMarker(fileContentsStr)

          getServerImplementations(resolved, server, markerInfo)
        }
      } yield {
        assert(definitions.toSet == expectedDefinitions)
      }
    }

  def getServerImplementations(filePath: String,
                               server: LanguageServer,
                               markerInfo: MarkerInfo): Future[Seq[Location]] = {

    openFile(server)(filePath, markerInfo.patchedContent.original)

    val implementationsHandler = server.resolveHandler(ImplementationRequestType).value

    implementationsHandler(
      ImplementationParams(TextDocumentIdentifier(filePath), LspRangeConverter.toLspPosition(markerInfo.position)))
      .map(implementations => {
        closeFile(server)(filePath)
        implementations
      })
      .map(_.left.getOrElse(Nil))
  }
}

class MarkerInfo(val patchedContent: PatchedContent, val position: Position) {}
