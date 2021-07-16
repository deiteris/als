package org.mulesoft.als.server.modules.completion

import org.mulesoft.als.common.MarkerInfo
import org.mulesoft.als.common.diff.FileAssertionTest
import org.mulesoft.als.common.dtoTypes.Position
import org.mulesoft.als.suggestions.test.CompletionItemNode
import org.mulesoft.lsp.feature.completion.CompletionItem
import org.scalatest.{Assertion, AsyncFunSuite}
import upickle.default.write

class SemanticExtensionsServerTest extends ServerSuggestionsTest with FileAssertionTest {
  override def rootPath: String = "/suggestions/semantic-extensions/"

  ignore("Test semantic Extensions") {
    runTest("api.raml")
  }

  ignore("Test semantic Extensions reference") {
    runTest("annotation-ref.raml")
  }

  ignore("Test semantic Extensions second level") {
    runTest("api-anidated.raml")
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
        markerInfo <- {
          this.platform
            .resolve(api)
            .map(c => {
              val fileContentsStr = c.stream.toString
              val markerInfo      = this.findMarker(fileContentsStr, "*")

              openFile(server)("file://api.raml", markerInfo.content)
              markerInfo
            })
        }
        c <- getPureCompletions(server, markerInfo, "file://api.raml")
        tmp <- writeTemporaryFile(api + ".json")(
          writeDataToString(c.sortWith((s1, s2) => s1.label.compareTo(s2.label) < 0).toList))
        r <- assertDifferences(tmp, api + ".json")
      } yield assert(c.nonEmpty)
    }
  }

  def writeDataToString(data: List[CompletionItem]): String =
    write[List[CompletionItemNode]](data.map(CompletionItemNode.sharedToTransport), 2)
}
