package org.mulesoft.als.server

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.plugins.ClientAMFPayloadValidationPlugin
import amf.client.resource.ClientResourceLoader
import org.mulesoft.als.configuration.{
  ClientDirectoryResolver,
  DefaultJsServerSystemConf,
  EmptyJsDirectoryResolver,
  JsServerSystemConf
}
import org.mulesoft.als.server.client.{AlsClientNotifier, ClientNotifier}
import org.mulesoft.als.server.logger.PrintLnLogger
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.modules.diagnostic.DiagnosticNotificationsKind
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.amfintegration.AmfInstance
import org.yaml.builder.DocBuilder.{Entry, Part, Scalar}
import org.yaml.builder.DocBuilder.SType.{Bool, Float, Int, Str}
import org.yaml.builder.{DocBuilder, JsOutputBuilder}

import scala.scalajs.js
import scala.scalajs.js.{Dynamic, JSON, UndefOr}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("LanguageServerFactory")
object LanguageServerFactory {

  def fromLoaders(clientNotifier: ClientNotifier,
                  serializationProps: JsSerializationProps,
                  clientLoaders: js.Array[ClientResourceLoader] = js.Array(),
                  clientDirResolver: ClientDirectoryResolver = EmptyJsDirectoryResolver,
                  logger: js.UndefOr[ClientLogger] = js.undefined,
                  withDiagnostics: Boolean = true,
                  notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined,
                  amfPlugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array.apply()): LanguageServer = {
    fromSystemConfig(clientNotifier,
                     serializationProps,
                     JsServerSystemConf(clientLoaders, clientDirResolver),
                     amfPlugins,
                     logger,
                     withDiagnostics,
                     notificationKind)
  }

  def fromSystemConfig(clientNotifier: ClientNotifier,
                       serialization: JsSerializationProps,
                       jsServerSystemConf: JsServerSystemConf = DefaultJsServerSystemConf,
                       plugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array(),
                       logger: js.UndefOr[ClientLogger] = js.undefined,
                       withDiagnostics: Boolean = true,
                       notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined): LanguageServer = {

    val factory =
      new WorkspaceManagerFactoryBuilder(clientNotifier, sharedLogger(logger), jsServerSystemConf.environment)
        .withAmfConfiguration(
          new AmfInstance(plugins.toSeq.map(ClientPayloadPluginConverter.convert),
                          jsServerSystemConf.platform,
                          jsServerSystemConf.environment))
        .withPlatform(jsServerSystemConf.platform)
        .withDirectoryResolver(jsServerSystemConf.directoryResolver)

    notificationKind.toOption.foreach(factory.withNotificationKind)

    val dm                    = factory.diagnosticManager()
    val sm                    = factory.serializationManager(serialization)
    val filesInProjectManager = factory.filesInProjectManager(serialization.alsClientNotifier)
    val builders              = factory.buildWorkspaceManagerFactory()

    val languageBuilder =
      new LanguageServerBuilder(builders.documentManager,
                                builders.workspaceManager,
                                builders.configurationManager,
                                builders.resolutionTaskManager,
                                sharedLogger(logger))
        .addInitializableModule(sm)
        .addInitializableModule(filesInProjectManager)
        .addInitializable(builders.workspaceManager)
        .addInitializable(builders.resolutionTaskManager)
        .addInitializable(builders.configurationManager)
        .addRequestModule(builders.cleanDiagnosticManager)
        .addRequestModule(builders.conversionManager)
        .addRequestModule(builders.completionManager)
        .addRequestModule(builders.structureManager)
        .addRequestModule(builders.definitionManager)
        .addRequestModule(builders.implementationManager)
        .addRequestModule(builders.typeDefinitionManager)
        .addRequestModule(builders.hoverManager)
        .addRequestModule(builders.referenceManager)
        .addRequestModule(builders.fileUsageManager)
        .addRequestModule(builders.documentLinksManager)
        .addRequestModule(builders.renameManager)
        .addRequestModule(builders.documentHighlightManager)
        .addRequestModule(builders.foldingRangeManager)
        .addRequestModule(builders.selectionRangeManager)
        .addRequestModule(builders.renameFileActionManager)
        .addRequestModule(builders.codeActionManager)
        .addRequestModule(builders.documentFormattingManager)
        .addRequestModule(builders.documentRangeFormattingManager)
        .addInitializable(builders.telemetryManager)
    dm.foreach(languageBuilder.addInitializableModule)
    builders.serializationManager.foreach(languageBuilder.addRequestModule)
    languageBuilder.build()
  }

  private def sharedLogger(logger: UndefOr[ClientLogger]) = {
    logger.toOption.map(l => ClientLoggerAdapter(l)).getOrElse(PrintLnLogger)
  }
}

@JSExportAll
@JSExportTopLevel("JsSerializationProps")
case class JsSerializationProps(override val alsClientNotifier: AlsClientNotifier[js.Any])
    extends JsSerializationProp[js.Any](alsClientNotifier) {
  override def newDocBuilder(): DocBuilder[js.Any]  = JsOutputBuilder()
  override def newDocBuilder2(): DocBuilder[js.Any] = new JsonBuilder()
}

class JsonBuilder extends DocBuilder[js.Any] {

  private var obj: js.Any         = _
  override def isDefined: Boolean = obj eq null

  override def result: js.Any = JSON.stringify(obj)

  override def list(f: Part[js.Any] => Unit): js.Any = {
    obj = createSeq(f)
    obj
  }

  override def obj(f: Entry[js.Any] => Unit): js.Any = {
    obj = createObj(f)
    obj
  }

  override def doc(f: Part[js.Any] => Unit): js.Any = {
    obj = createSeq(f)(0)
    obj
  }

  private def createSeq(f: Part[js.Any] => Unit): js.Array[js.Any] = {
    val result = new js.Array[js.Any]
    val partBuilder: Part[js.Any] = new Part[js.Any] {
      override def +=(element: js.Any): Unit = result.push(element)
      override def +=(scalar: Scalar): Unit  = result.push(fromScalar(scalar))
      override def list(f: Part[js.Any] => Unit): Option[js.Any] = {
        val value = createSeq(f)
        result.push(value)
        Some(value)
      }
      override def obj(f: Entry[js.Any] => Unit): Option[js.Any] = {
        val value: js.Object = createObj(f)
        result.push(value)
        Some(value)
      }
    }
    f(partBuilder)
    result
  }

  private def fromScalar(scalar: Scalar): js.Any = scalar.t match {
    case Str   => scalar.value.toString
    case Bool  => scalar.value.asInstanceOf[Boolean]
    case Float => scalar.value.asInstanceOf[Double]
    case Int   => scalar.value.asInstanceOf[Long]
  }

  private def createObj(f: Entry[js.Any] => Unit): js.Object = {
    val result = js.Object()
    val o      = result.asInstanceOf[Dynamic]

    val b: Entry[js.Any] = new Entry[js.Any] {
      override def entry(key: String, value: Scalar): Unit           = o.updateDynamic(key)(fromScalar(value))
      override def entry(key: String, f: Part[js.Any] => Unit): Unit = o.updateDynamic(key)(createSeq(f)(0))
    }
    f(b)
    result
  }

}
