package org.mulesoft.als.client.lsp.common

import org.mulesoft.lsp.common.TextDocumentIdentifier

import scala.scalajs.js
// $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

@js.native
trait ClientTextDocumentIdentifier extends js.Object {
  def uri: String = js.native
}

object ClientTextDocumentIdentifier {
  def apply(internal: TextDocumentIdentifier): ClientTextDocumentIdentifier =
    js.Dynamic
      .literal(uri = internal.uri)
      .asInstanceOf[ClientTextDocumentIdentifier]
}
// $COVERAGE-ON$