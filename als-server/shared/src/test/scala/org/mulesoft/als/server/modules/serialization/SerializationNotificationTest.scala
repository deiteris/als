package org.mulesoft.als.server.modules.serialization

import java.io.StringWriter

import amf.client.remote.Content
import amf.core.CompilerContextBuilder
import amf.core.model.document.Document
import amf.core.parser.UnspecifiedReference
import amf.core.remote.{Amf, Mimes}
import amf.core.services.RuntimeCompiler
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.als.server._
import org.mulesoft.als.server.modules.{WorkspaceManagerFactory, WorkspaceManagerFactoryBuilder}
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.protocol.configuration.{AlsClientCapabilities, AlsInitializeParams}
import org.mulesoft.lsp.configuration.TraceKind
import org.mulesoft.lsp.feature.serialization.SerializationClientCapabilities

import scala.concurrent.{ExecutionContext, Future}

class SerializationNotificationTest extends LanguageServerBaseTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val alsClient = new MockAlsClientNotifier
  protected val factoryBuilder: WorkspaceManagerFactoryBuilder =
    new WorkspaceManagerFactoryBuilder(new MockDiagnosticClientNotifier, logger)
  protected val serializationManager: SerializationManager[StringWriter] =
    factoryBuilder.serializationManager(JvmSerializationProps(alsClient))
  protected val factory: WorkspaceManagerFactory = factoryBuilder.buildWorkspaceManagerFactory()

  override val initializeParams: AlsInitializeParams = AlsInitializeParams(
    Some(AlsClientCapabilities(serialization = Some(SerializationClientCapabilities(true)))),
    Some(TraceKind.Off))

  test("Parse Model and check serialized json ld") {
    withServer { server =>
      val content =
        """#%RAML 1.0
          |title: test
          |description: missing title
          |""".stripMargin

      val api = "file://api.raml"
      openFile(server)(api, content)

      for {
        s <- alsClient.nextCall.map(_.model.toString)
        parsed <- {
          val env = Environment(new ResourceLoader {

            /** Fetch specified resource and return associated content. Resource should have benn previously accepted. */
            override def fetch(resource: String): Future[Content] = Future.successful(new Content(s, api))

            /** Accepts specified resource. */
            override def accepts(resource: String): Boolean = resource == api
          })
          RuntimeCompiler
            .forContext(
              new CompilerContextBuilder(api, platform).withEnvironment(env).build(),
              Some(Mimes.`APPLICATION/LD+JSONLD`),
              Some(Amf.name),
              UnspecifiedReference
            )
        }
      } yield {
        parsed.asInstanceOf[Document].encodes.id should be("amf://id#/web-api")
      }
    }
  }

  override def buildServer(): LanguageServer = {
    val builder = new LanguageServerBuilder(factory.documentManager, factory.workspaceManager)
    builder.addInitializableModule(serializationManager)
    builder.build()
  }

  override def rootPath: String = ""
}
