package org.mulesoft.als.server.modules.completion

import org.mulesoft.als.common.diff.FileAssertionTest
import org.mulesoft.als.common.dtoTypes.PositionRange
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.{LanguageServerBaseTest, LanguageServerBuilder, MockDiagnosticClientNotifier}
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.suggestions.test.CompletionItemNode
import org.mulesoft.language.outline.structure.structureImpl.DocumentSymbol
import org.mulesoft.language.outline.structure.structureImpl.SymbolKind.SymbolKind
import org.mulesoft.language.outline.test.DocumentSymbolNode
import org.mulesoft.lsp.feature.common.TextDocumentIdentifier
import org.mulesoft.lsp.feature.completion.CompletionItem
import org.mulesoft.lsp.feature.documentsymbol.{DocumentSymbolParams, DocumentSymbolRequestType}
import org.scalatest.Assertion
import upickle.default.write
import org.mulesoft.lsp.feature.documentsymbol.{DocumentSymbol => LspSymbol}

import scala.concurrent.Future

class DocumentSymbolExtensionsServerTest extends LanguageServerBaseTest with FileAssertionTest {
  override def rootPath: String = "/outline/semantic-extensions/"

  def buildServer(): LanguageServer = {

    val factory =
      new WorkspaceManagerFactoryBuilder(new MockDiagnosticClientNotifier, logger).buildWorkspaceManagerFactory()
    new LanguageServerBuilder(factory.documentManager,
                              factory.workspaceManager,
                              factory.configurationManager,
                              factory.resolutionTaskManager)
      .addRequestModule(factory.structureManager)
      .build()
  }

  test("Test semantic Extensions outline") {
    runTest("api.raml")
  }

  def runTest(path: String) = {
    val api = filePath(platform.encodeURI(path))
    withServer[Assertion](buildServer()) { server =>
      for {
        _ <- {
          this.platform
            .resolve(filePath(platform.encodeURI("dialect.yaml")))
            .map(c => {
              openFile(server)("file://dialect.yaml", c.stream.toString)
            })
        }
        _ <- {
          this.platform
            .resolve(api)
            .map(c => {

              openFile(server)("file://api.raml", c.stream.toString)
            })
        }
        c   <- getOutline(server, "file://api.raml")
        tmp <- writeTemporaryFile(api + ".json")(writeDataToString(c.toList))
        r   <- assertDifferences(tmp, api + ".json")
      } yield assert(c.nonEmpty)
    }
  }

  def getOutline(server: LanguageServer, filePath: String): Future[Seq[DocumentSymbol]] = {
    val handler = server.resolveHandler(DocumentSymbolRequestType).value

    handler(DocumentSymbolParams(TextDocumentIdentifier(filePath)))
      .collect { case Right(symbols) => symbols.map(buildInternalSymbol).toList }
  }

  private def buildInternalSymbol(documentSymbol: LspSymbol): DocumentSymbol = {
    DocumentSymbol(documentSymbol.name,
                   SymbolKind(2),
                   PositionRange(documentSymbol.range),
                   documentSymbol.children.map(buildInternalSymbol).toList)
  }

  def writeDataToString(data: List[DocumentSymbol]): String =
    write[List[DocumentSymbolNode]](data.map(DocumentSymbolNode.sharedToTransport), 2)
}
