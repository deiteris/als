package org.mulesoft.als.server.protocol.textsync

import org.eclipse.lsp4j.TextDocumentIdentifier
import org.mulesoft.lsp.textsync.TextDocumentSyncConsumer
trait AlsTextDocumentSyncConsumer extends TextDocumentSyncConsumer {

  def didFocus(params: DidFocusParams): Unit

}
