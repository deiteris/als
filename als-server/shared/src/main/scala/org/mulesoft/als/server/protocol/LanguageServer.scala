package org.mulesoft.als.server.protocol

import org.mulesoft.als.server.protocol.configuration.{AlsInitializeParams, AlsInitializeResult}
import org.mulesoft.lsp.feature.{RequestHandler, RequestType}
import org.mulesoft.als.server.protocol.textsync.AlsTextDocumentSyncConsumer
import org.mulesoft.lsp.workspace.WorkspaceService

import scala.concurrent.Future

trait LanguageServer {
  def initialize(params: AlsInitializeParams): Future[AlsInitializeResult]

  def initialized(): Unit

  def shutdown(): Unit

  def exit(): Unit

  def textDocumentSyncConsumer: AlsTextDocumentSyncConsumer

  def workspaceService: WorkspaceService

  def resolveHandler[P, R](requestType: RequestType[P, R]): Option[RequestHandler[P, R]]

}
