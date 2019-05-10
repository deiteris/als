package org.mulesoft.als.server

import org.mulesoft.lsp.configuration.{InitializeParams, InitializeResult}
import org.mulesoft.lsp.feature.{RequestHandler, RequestType}
import org.mulesoft.lsp.server.LanguageServer
import org.mulesoft.lsp.textsync.TextDocumentSyncConsumer

import scala.concurrent.Future

class LanguageServerImpl(val textDocumentSyncConsumer: TextDocumentSyncConsumer,
                         private val languageServerInitializer: LanguageServerInitializer,
                         private val requestHandlerMap: RequestMap)
  extends LanguageServer {

  override def initialize(params: InitializeParams): Future[InitializeResult] =
    languageServerInitializer.initialize(params)

  override def initialized(): Unit = {}

  override def shutdown(): Unit = {}

  override def exit(): Unit = {}

  override def resolveHandler[P, R](requestType: RequestType[P, R]): Option[RequestHandler[P, R]] =
    requestHandlerMap(requestType)
}