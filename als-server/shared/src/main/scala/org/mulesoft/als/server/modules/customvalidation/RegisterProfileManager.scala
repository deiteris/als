package org.mulesoft.als.server.modules.customvalidation

import amf.ProfileName
import amf.core.services.RuntimeValidator
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.features.validation.custom.AMFValidatorPlugin
import org.mulesoft.als.server.RequestModule
import org.mulesoft.als.server.feature.customvalidation.{RegisterProfileNotificationType, RegisterProfileParams}
import org.mulesoft.als.server.modules.ast.{BaseUnitListener, BaseUnitListenerParams}
import org.mulesoft.als.server.workspace.WorkspaceManager
import org.mulesoft.amfintegration.AmfInstance
import org.mulesoft.lsp.ConfigType
import org.mulesoft.lsp.feature.TelemeteredRequestHandler
import org.mulesoft.lsp.feature.telemetry.MessageTypes.MessageTypes
import org.mulesoft.lsp.feature.telemetry.{MessageTypes, TelemetryProvider}

import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegisterProfileManager(telemetryProvider: TelemetryProvider, amfInstance: AmfInstance)
    extends RequestModule[Unit, Unit]
    with BaseUnitListener {

  val profiles: ListBuffer[ProfileName] = ListBuffer.empty
  override def getRequestHandlers: Seq[TelemeteredRequestHandler[_, _]] = Seq(
    new TelemeteredRequestHandler[RegisterProfileParams, Unit] {
      override def `type`: RegisterProfileNotificationType.type = RegisterProfileNotificationType

      override def task(params: RegisterProfileParams): Future[Unit] =
        processRequest(params.textDocument.uri)

      override protected def telemetry: TelemetryProvider = telemetryProvider

      override protected def code(params: RegisterProfileParams): String = "RegisterProfileManager"

      override protected def beginType(params: RegisterProfileParams): MessageTypes = MessageTypes.BEGIN_SERIALIZATION

      override protected def endType(params: RegisterProfileParams): MessageTypes = MessageTypes.END_SERIALIZATION

      override protected def msg(params: RegisterProfileParams): String =
        s"Requested serialization for ${params.textDocument.uri}"

      override protected def uri(params: RegisterProfileParams): String = params.textDocument.uri

      /**
        * If Some(_), this will be sent as a response as a default for a managed exception
        */
      override protected val empty: Option[Unit] = None
    }
  )

  override val `type`: ConfigType[Unit, Unit] =
    new ConfigType[Unit, Unit] {}
  def processRequest(str: String) = {
    val uuid = UUID.randomUUID().toString

    val r = for {
      profile <- unitAccessor.get.getUnit(str, uuid)
    } yield {
      profile.unit match {
        case d: DialectInstance =>
          val name = AMFValidatorPlugin.loadValidationProfileInstance(AMFValidatorPlugin.parseProfile(d))
          if (!profiles.contains(name)) profiles += name
        case _ => // ignore

      }
    }
    r.map(_ => {})
  }

  override def initialize(): Future[Unit] = Future.unit

  override def applyConfig(config: Option[Unit]): Unit = {}

  /**
    * Called on new AST available
    *
    * @param ast  - AST
    * @param uuid - telemetry UUID
    */
  override def onNewAst(ast: BaseUnitListenerParams, uuid: String): Unit = {
    ast.parseResult.baseUnit match {
      case d: DialectInstance if AMFValidatorPlugin.isValProfile(d) =>
        val profile = AMFValidatorPlugin.parseProfile(d)
        if (profiles.contains(profile.name)) AMFValidatorPlugin.loadValidationProfileInstance(profile)
      case _ => // ignore
    }
  }

  override def onRemoveFile(uri: String): Unit = {}
}
