package org.mulesoft.language.server.js

import org.mulesoft.language.common.logger.{ILoggerSettings, IPrintlnLogger}
import org.mulesoft.language.common.dtoTypes._
import org.mulesoft.language.entryPoints.common.ProtocolSeqMessage
import org.mulesoft.language.server.core.connectionsImpl.AbstractServerConnection

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSON

class MSLSPServerConnection() extends MSLSPMessageDispatcher with AbstractServerConnection with IPrintlnLogger {

  var lastStructureReport: Option[StructureReport] = None

  private val validationReportListeners: scala.collection.mutable.MutableList[Function1[ValidationReport, Unit]] =
    scala.collection.mutable.MutableList[Function1[ValidationReport, Unit]]();

  initialize()

  protected def initialize(): Unit = {

    this.newVoidHandler("OPEN_DOCUMENT",
                        this.handleOpenDocument _,
                        Option(NodeMsgTypeMeta("org.mulesoft.language.client.js.OpenedDocument")))

    this.newVoidHandler("CHANGE_DOCUMENT",
                        this.handleChangedDocument _,
                        Option(NodeMsgTypeMeta("org.mulesoft.language.client.js.ChangedDocument")))

    this.newFutureHandler("GET_STRUCTURE",
                          this.handleGetStructure _,
                          Option(NodeMsgTypeMeta("org.mulesoft.language.client.js.GetStructure", true, true)))

    this.newVoidHandler("SET_LOGGER_CONFIGURATION",
                        this.handleSetLoggerConfiguration _,
                        Option(NodeMsgTypeMeta("org.mulesoft.language.client.js.LoggerSettings")))
  }

  def onValidationReport(listener: Function1[ValidationReport, Unit]) {
    validationReportListeners += listener;
  }

  protected def internalSendJSONMessage(message: js.Any): Unit = {}

  def internalSendSeqMessage(message: ProtocolSeqMessage[ProtocolMessagePayload]) {}

  def getStructure(uri: String): Future[GetStructureResponse] = {
    var request = new GetStructureRequest(uri);

    handleGetStructure(request);
  }

  def handleGetStructure(getStructure: GetStructureRequest): Future[GetStructureResponse] = {
    val firstOpt = this.documentStructureListeners.find(_ => true)
    firstOpt match {
      case Some(listener) =>
        listener(getStructure.wrapped).map(resultMap => {
          GetStructureResponse(resultMap.map { case (key, value) => (key, value.asInstanceOf[StructureNode]) })
        })
      case _ => Future.failed(new Exception("No structure providers found"))
    }
  }

  def handleOpenDocument(document: OpenedDocument): Unit = {
    val firstOpt = this.openDocumentListeners.find(_ => true)
    firstOpt match {
      case Some(listener) =>
        listener(document)
      case _ => Future.failed(new Exception("No open document providers found"))
    }
  }

  def handleChangedDocument(document: ChangedDocument): Unit = {
    val firstOpt = this.changeDocumentListeners.find(_ => true)
    firstOpt match {
      case Some(listener) =>
        listener(ChangedDocument.transportToShared(document))
      case _ => Future.failed(new Exception("No change document providers found"))
    }
  }

  def handleSetLoggerConfiguration(loggerSettings: LoggerSettings): Unit = {
    this.setLoggerConfiguration(LoggerSettings.transportToShared(loggerSettings))

  }

  /**
	  * Reports new calculated structure when available.
	  * @param report - structure report.
	  */
  def structureAvailable(report: IStructureReport): Unit = {
    this.send("STRUCTURE_REPORT", report.asInstanceOf[StructureReport])
  }

  /**
	  * Reports latest validation results
	  *
	  * @param report
	  */
  override def validated(report: IValidationReport): Unit = {
    var transportReport = ValidationReport.sharedToTransport(report);

    validationReportListeners.foreach(_(transportReport));

    //this.send("VALIDATION_REPORT", ValidationReport.sharedToTransport(report));
  }

  /**
	  * Returns whether path/url exists.
	  *
	  * @param path
	  */
  override def exists(path: String): Future[Boolean] = ???

  /**
	  * Returns directory content list.
	  *
	  * @param path
	  */
  override def readDir(path: String): Future[Seq[String]] = ???

  /**
	  * Returns whether path/url represents a directory
	  *
	  * @param path
	  */
  override def isDirectory(path: String): Future[Boolean] = ???

  /**
	  * File contents by full path/url.
	  *
	  * @param fullPath
	  */
  override def content(fullPath: String): Future[String] = ???

  /**
	  * Adds a listener to document details request. Must notify listeners in order of registration.
	  *
	  * @param listener    (uri: String, position: Int) => Future[DetailsItemJSON]
	  * @param unsubscribe - if true, existing listener will be removed. False by default.
	  */
  override def onDocumentDetails(listener: (String, Int) => Future[IDetailsItem], unsubscribe: Boolean): Unit = ???

  /**
	  * Reports new calculated details when available.
	  *
	  * @param report - details report.
	  */
  override def detailsAvailable(report: IDetailsReport): Unit = ???

  /**
	  * Adds a listener to display action UI.
	  *
	  * @param uiDisplayRequest - display request
	  * @return final UI state.
	  */
  override def displayActionUI(uiDisplayRequest: IUIDisplayRequest): Future[Any] = ???
}
