package org.mulesoft.als.server

import org.mulesoft.als.server.protocol.LanguageServer
import org.mulesoft.als.server.protocol.configuration.{AlsInitializeParams, AlsInitializeResult}
import org.mulesoft.als.server.protocol.textsync.AlsTextDocumentSyncConsumer
import org.mulesoft.lsp.configuration.WorkspaceFolder
import org.mulesoft.lsp.feature.{RequestHandler, RequestType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LanguageServerImpl(val textDocumentSyncConsumer: AlsTextDocumentSyncConsumer,
                         val workspaceService: AlsWorkspaceService,
                         private val languageServerInitializer: LanguageServerInitializer,
                         private val requestHandlerMap: RequestMap)
    extends LanguageServer {

  override def initialize(params: AlsInitializeParams): Future[AlsInitializeResult] =
    languageServerInitializer.initialize(params).flatMap { p =>
      val root: Option[String]                   = params.rootUri.orElse(params.rootPath)
      val workspaceFolders: Seq[WorkspaceFolder] = params.workspaceFolders.getOrElse(List())
      workspaceService
        .initialize((workspaceFolders :+ WorkspaceFolder(root, None)).toList)
        .map(_ => p)
    }

  override def initialized(): Unit = {
    // no further actions
  }

  override def shutdown(): Unit = {
    // no further actions at the moment, maybe shutdown managers?
  }

  override def exit(): Unit = {
    // no further actions at the moment, maybe shutdown managers?
  }

  override def resolveHandler[P, R](requestType: RequestType[P, R]): Option[RequestHandler[P, R]] =
    requestHandlerMap(requestType)
}
