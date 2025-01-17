package org.mulesoft.als.server.workspace.highlights

import amf.client.remote.Content
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.als.actions.rename.FindRenameLocations
import org.mulesoft.als.common.dtoTypes.{Position => DtoPosition}
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.protocol.configuration.AlsInitializeParams
import org.mulesoft.als.server.workspace.WorkspaceManager
import org.mulesoft.als.server.{LanguageServerBaseTest, LanguageServerBuilder, MockDiagnosticClientNotifier}
import org.mulesoft.lsp.configuration.TraceKind
import org.mulesoft.lsp.edit.{TextDocumentEdit, TextEdit, WorkspaceEdit}
import org.mulesoft.lsp.feature.RequestHandler
import org.mulesoft.lsp.feature.common.{
  Position,
  Range,
  TextDocumentIdentifier,
  TextDocumentItem,
  VersionedTextDocumentIdentifier
}
import org.mulesoft.lsp.feature.highlight.{
  DocumentHighlight,
  DocumentHighlightConfigType,
  DocumentHighlightKind,
  DocumentHighlightParams,
  DocumentHighlightRequestType
}
import org.mulesoft.lsp.textsync.DidOpenTextDocumentParams

import scala.concurrent.{ExecutionContext, Future}

class DocumentHighlightTest extends LanguageServerBaseTest {

  override implicit val executionContext: ExecutionContext =
    ExecutionContext.Implicits.global

  private val ws1 = Map(
    "file:///root/exchange.json" -> """{"main": "api.raml"}""",
    "file:///root/api.raml" ->
      """#%RAML 1.0
        |uses:
        |  lib: lib.raml
        |
        |/links:
        |  is:
        |    - lib.tr""".stripMargin,
    "file:///root/lib.raml" ->
      """#%RAML 1.0 Library
        |traits:
        |  tr:
        |    description: example trait
        |types:
        |  A: string
        |  C: A
        |  D: A""".stripMargin
  )

  private val ws2 = Map(
    "file:///root/exchange.json" -> """{"main": "api.json"}""",
    "file:///root/api.json" ->
      """{
        |  "swagger": "2.0",
        |  "definitions": {
        |      "User": {
        |        "$ref": "test/properties.json"
        |      }
        |  },
        |  "paths": {
        |    "/get": {
        |        "get": {
        |            "parameters": [
        |                {
        |                  "in": "body",
        |                  "name": "user",
        |                  "schema": {
        |                      "$ref": "#/definitions/User"
        |                  }
        |                }
        |            ]
        |          }
        |        }
        |    }
        |}""".stripMargin,
    "file:///root/test/properties.json" ->
      """{
        |    "properties": {
        |            "username": {
        |              "type": "string"
        |            }
        |    }
        |}""".stripMargin
  )

  private val ws3 = Map(
    "file:///root/exchange.json" -> """{"main": "api.raml"}""",
    "file:///root/api.raml" ->
      """#%RAML 1.0
        |title: api
        |resourceTypes:
        |  details: !include resourceTypes/details.raml
        |  export: !include resourceTypes/export.raml
        |
        |/path:
        |  /details:
        |    type: details
        |
        |  /export:
        |    type: export""".stripMargin,
    "file:///root/resourceTypes/details.raml" ->
      """#%RAML 1.0 ResourceType
        |  responses:
        |    200:
        |      body:
        |        application/json:
        |          type: array
        |          items:
        |            type: string""".stripMargin,
    "file:///root/resourceTypes/export.raml" ->
      """get:
        |  responses:
        |    200:
        |      body:
        |        application/json:
        |          type: array
        |          items:
        |            type: string""".stripMargin,
  )
  private val ws4 = Map(
    "file:///root/exchange.json" -> """{"main": "api.raml"}""",
    "file:///root/api.raml" ->
      """#%RAML 1.0
        |title: test
        |securitySchemes:
        |  oauth_2_0: !include securitySchemes/oauth_2_0.raml
        |  tokenSchema: !include securitySchemes/tokenSchema.raml
        |
        |/get:
        |  description: |
        |    This endpoint retrieves all updated and newly created Project Assignments.
        |  securedBy: tokenSchema
        |              """.stripMargin,
    "file:///root/securitySchemes/oauth_2_0.raml" ->
      """#%RAML 1.0 SecurityScheme
        |description: ""
        |type: OAuth 2.0
        |describedBy:
        |  headers:
        |    Authorization:
        |      description: |
        |        Used to send a valid OAuth 2 access token. The token must be preceeded by the word "Bearer".
        |      example: Bearer _token_
        |settings:
        |  authorizationUri: https://login.salesforce.com/services/oauth2/authorize
        |  accessTokenUri: https://login.salesforce.com/services/oauth2/token
        |  authorizationGrants: [ authorization_code ]""".stripMargin,
    "file:///root/securitySchemes/tokenSchema.raml" ->
      """#%RAML 1.0 SecurityScheme
        |type: x-custom
        |describedBy:
        |  headers:
        |    Authorization:
        |      description: |
        |        Used to send a valid OAuth 2 access token. The token must be preceeded by the word "Bearer".
        |      example: Bearer _token_""".stripMargin,
  )

  val testSets: Set[TestEntry] = Set(
    TestEntry(
      "file:///root/lib.raml",
      Position(5, 3),
      ws1,
      Set(
        DocumentHighlight(Range(Position(6, 5), Position(6, 6)), DocumentHighlightKind.Text),
        DocumentHighlight(Range(Position(7, 5), Position(7, 6)), DocumentHighlightKind.Text)
      )
    ),
    TestEntry(
      "file:///root/api.json",
      Position(3, 9),
      ws2,
      Set(
        DocumentHighlight(Range(Position(15, 30), Position(15, 50)), DocumentHighlightKind.Text)
      )
    ),
    TestEntry(
      "file:///root/api.raml",
      Position(4, 5),
      ws3,
      Set(
        DocumentHighlight(Range(Position(11, 10), Position(11, 16)), DocumentHighlightKind.Text)
      )
    ),
    TestEntry(
      "file:///root/api.raml",
      Position(4,5),
      ws4,
      Set(
        DocumentHighlight(Range(Position(9, 13), Position(9, 24)), DocumentHighlightKind.Text)
      )
    ),
    TestEntry(
      "file:///root/api.raml",
      Position(3,5),
      ws4,
      Set()
    )
  )

  test("Document Highlight tests") {
    Future.sequence(testSets.toSeq.map { test =>
      for {
        (server, _) <- buildServer(test.root, test.ws)
        highlights <- {
          server.textDocumentSyncConsumer.didOpen(
            DidOpenTextDocumentParams(
              TextDocumentItem(
                test.targetUri,
                "",
                0,
                test.ws(test.targetUri)
              )))
          val dhHandler: RequestHandler[DocumentHighlightParams, Seq[DocumentHighlight]] =
            server.resolveHandler(DocumentHighlightRequestType).get
          dhHandler(DocumentHighlightParams(TextDocumentIdentifier(test.targetUri), test.targetPosition))
        }
      } yield {
        highlights.toSet == test.result
      }
    }).map(set => {
      assert(!set.contains(false) && set.size == testSets.size)
    })
  }

  case class TestEntry(targetUri: String,
                       targetPosition: Position,
                       ws: Map[String, String],
                       result: Set[DocumentHighlight],
                       root: String = "file:///root")

  def buildServer(root: String, ws: Map[String, String]): Future[(LanguageServer, WorkspaceManager)] = {
    val rs = new ResourceLoader {
      override def fetch(resource: String): Future[Content] =
        ws.get(resource)
          .map(c => new Content(c, resource))
          .map(Future.successful)
          .getOrElse(Future.failed(new Exception("File not found on custom ResourceLoader")))
      override def accepts(resource: String): Boolean =
        ws.keySet.contains(resource)
    }

    val env = Environment().withLoaders(Seq(rs))

    val factory =
      new WorkspaceManagerFactoryBuilder(new MockDiagnosticClientNotifier, logger, env)
        .buildWorkspaceManagerFactory()
    val workspaceManager: WorkspaceManager = factory.workspaceManager
    val server =
      new LanguageServerBuilder(factory.documentManager,
                                workspaceManager,
                                factory.configurationManager,
                                factory.resolutionTaskManager)
        .addRequestModule(factory.documentHighlightManager)
        .build()

    server
      .initialize(AlsInitializeParams(None, Some(TraceKind.Off), rootUri = Some(root)))
      .andThen { case _ => server.initialized() }
      .map(_ => (server, workspaceManager))
  }

  override def rootPath: String = ???
}
