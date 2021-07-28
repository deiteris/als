package org.mulesoft.als.server

import amf.client.convert.ClientPayloadPluginConverter
import amf.client.plugins.ClientAMFPayloadValidationPlugin
import amf.client.resource.ClientResourceLoader
import amf.core.AMFSerializer
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Mimes}
import org.mulesoft.als.configuration.{
  ClientDirectoryResolver,
  DefaultJsServerSystemConf,
  EmptyJsDirectoryResolver,
  JsServerSystemConf
}
import org.mulesoft.als.server.client.{AlsClientNotifier, ClientConnection, ClientNotifier}
import org.mulesoft.als.server.logger.PrintLnLogger
import org.mulesoft.als.server.modules.WorkspaceManagerFactoryBuilder
import org.mulesoft.als.server.modules.diagnostic.{
  AMFValidator,
  DiagnosticNotificationsKind,
  PlatformSerializer,
  ValidatorBuilder
}
import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.amfintegration.AmfInstance
import org.yaml.builder.DocBuilder.{Entry, Part, Scalar}
import org.yaml.builder.DocBuilder.SType.{Bool, Float, Int, Str}
import org.yaml.builder.{DocBuilder, JsOutputBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.{Dynamic, JSON, UndefOr}
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("LanguageServerFactory")
object LanguageServerFactory {

  def fromLoaders[A](clientNotifier: ClientConnection[A],
                     serializationProps: SerializationProps[A],
                     clientLoaders: js.Array[ClientResourceLoader] = js.Array(),
                     clientDirResolver: ClientDirectoryResolver = EmptyJsDirectoryResolver,
                     logger: js.UndefOr[ClientLogger] = js.undefined,
                     notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined,
                     amfPlugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array.apply()): LanguageServer = {
    fromSystemConfig(clientNotifier,
                     serializationProps,
                     JsServerSystemConf(clientLoaders, clientDirResolver),
                     amfPlugins,
                     logger,
                     notificationKind)
  }

  def fromSystemConfig[A](clientNotifier: ClientConnection[A],
                          serialization: SerializationProps[A],
                          jsServerSystemConf: JsServerSystemConf = DefaultJsServerSystemConf,
                          plugins: js.Array[ClientAMFPayloadValidationPlugin] = js.Array(),
                          logger: js.UndefOr[ClientLogger] = js.undefined,
                          notificationKind: js.UndefOr[DiagnosticNotificationsKind] = js.undefined): LanguageServer = {

    val factory =
      new WorkspaceManagerFactoryBuilder(clientNotifier,
                                         sharedLogger(logger),
                                         jsServerSystemConf.environment,
                                         new JsPlatformSerializer(),
                                         AMFValidator)
        .withAmfConfiguration(
          new AmfInstance(plugins.toSeq.map(ClientPayloadPluginConverter.convert),
                          jsServerSystemConf.platform,
                          jsServerSystemConf.environment))
        .withPlatform(jsServerSystemConf.platform)
        .withDirectoryResolver(jsServerSystemConf.directoryResolver)

    notificationKind.toOption.foreach(factory.withNotificationKind)

    val dm                    = factory.diagnosticManager()
    val sm                    = factory.serializationManager(serialization, clientNotifier)
    val filesInProjectManager = factory.filesInProjectManager(clientNotifier)
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
        .addRequestModule(factory.validationProfileRegister)
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
case class JsSerializationProps() extends SerializationProps[js.Any]() {
  override def newDocBuilder(): DocBuilder[js.Any] = JsOutputBuilder()
}

class JsPlatformSerializer() extends PlatformSerializer {
  override def serialize(u: BaseUnit): Future[String] = {
    val builder = JsOutputBuilder()
    new AMFSerializer(u, Mimes.`APPLICATION/LD+JSONLD`, Amf.name, RenderOptions().withCompactUris.withSourceMaps)
      .renderToBuilder(builder)(ExecutionContext.Implicits.global)
      .map(_ => JSON.stringify(builder.result))
  }
}
