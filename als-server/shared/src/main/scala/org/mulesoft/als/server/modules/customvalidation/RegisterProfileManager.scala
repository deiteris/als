package org.mulesoft.als.server.modules.customvalidation

import amf.ProfileName
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.features.validation.custom.AMFValidatorPlugin
import org.mulesoft.als.server.RequestModule
import org.mulesoft.als.server.feature.customvalidation.{
  RegisterProfileNotificationType,
  RegisterProfileParams,
  UnregisterProfileNotificationType
}
import org.mulesoft.als.server.modules.ast.{BaseUnitListener, BaseUnitListenerParams}
import org.mulesoft.amfintegration.AmfInstance
import org.mulesoft.lsp.ConfigType
import org.mulesoft.lsp.feature.TelemeteredRequestHandler
import org.mulesoft.lsp.feature.telemetry.MessageTypes.MessageTypes
import org.mulesoft.lsp.feature.telemetry.{MessageTypes, TelemetryProvider}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegisterProfileManager(telemetryProvider: TelemetryProvider)
    extends RequestModule[Unit, Unit]
    with BaseUnitListener {

  val profiles: ListBuffer[String] = ListBuffer.empty
  override def getRequestHandlers: Seq[TelemeteredRequestHandler[_, _]] = {
    val registerHandler: TelemeteredRequestHandler[RegisterProfileParams, Unit] =
      new TelemeteredRequestHandler[RegisterProfileParams, Unit] {
        override def `type`: RegisterProfileNotificationType.type = RegisterProfileNotificationType

        override def task(params: RegisterProfileParams): Future[Unit] = Future {
          processRequestRegister(params.textDocument.uri)
        }

        override protected def telemetry: TelemetryProvider = telemetryProvider

        override protected def code(params: RegisterProfileParams): String = "RegisterProfileManager"

        override protected def beginType(params: RegisterProfileParams): MessageTypes =
          MessageTypes.BEGIN_SERIALIZATION

        override protected def endType(params: RegisterProfileParams): MessageTypes = MessageTypes.END_SERIALIZATION

        override protected def msg(params: RegisterProfileParams): String =
          s"Requested RegisterProfileManager for ${params.textDocument.uri}"

        override protected def uri(params: RegisterProfileParams): String = params.textDocument.uri

        /**
          * If Some(_), this will be sent as a response as a default for a managed exception
          */
        override protected val empty: Option[Unit] = None
      }
    val unregisterHandler: TelemeteredRequestHandler[RegisterProfileParams, Unit] =
      new TelemeteredRequestHandler[RegisterProfileParams, Unit] {
        override def `type`: UnregisterProfileNotificationType.type = UnregisterProfileNotificationType

        override def task(params: RegisterProfileParams): Future[Unit] = Future {
          processRequestUnregister(params.textDocument.uri)
        }

        override protected def telemetry: TelemetryProvider = telemetryProvider

        override protected def code(params: RegisterProfileParams): String = "RegisterProfileManager"

        override protected def beginType(params: RegisterProfileParams): MessageTypes =
          MessageTypes.BEGIN_SERIALIZATION

        override protected def endType(params: RegisterProfileParams): MessageTypes = MessageTypes.END_SERIALIZATION

        override protected def msg(params: RegisterProfileParams): String =
          s"Requested UnregisterProfileNotificationType for ${params.textDocument.uri}"

        override protected def uri(params: RegisterProfileParams): String = params.textDocument.uri

        /**
          * If Some(_), this will be sent as a response as a default for a managed exception
          */
        override protected val empty: Option[Unit] = None
      }
    Seq(
      registerHandler,
      unregisterHandler
    )
  }

  override val `type`: ConfigType[Unit, Unit] =
    new ConfigType[Unit, Unit] {}

  def processRequestRegister(str: String): Unit = {
    println("Registering: " + str)
    if (!profiles.contains(str)) profiles += str
    listener.foreach(_.newProfile())
  }

  def processRequestUnregister(str: String): Unit = {
    println("Unregistering: " + str)
    if (profiles.contains(str)) profiles -= str
    listener.foreach(_.newProfile())
  }

  override def initialize(): Future[Unit] = Future.unit

  override def applyConfig(config: Option[Unit]): Unit = {}

  /**
    * Called on new AST available
    *
    * @param ast  - AST
    * @param uuid - telemetry UUID
    */
  override def onNewAst(ast: BaseUnitListenerParams, uuid: String): Unit = {}

  override def onRemoveFile(uri: String): Unit = {}

  var listener: Option[ProfileListener] = None
  def withListener(a: ProfileListener) {
    listener = Some(a)
  }
}

trait ProfileListener {
  def newProfile(): Unit
}
