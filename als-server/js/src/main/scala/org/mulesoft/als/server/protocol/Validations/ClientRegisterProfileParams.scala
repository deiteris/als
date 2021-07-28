package org.mulesoft.als.server.protocol.Validations

import org.mulesoft.als.server.feature.customvalidation.RegisterProfileParams
import org.mulesoft.als.server.feature.renamefile.RenameFileActionParams
import org.mulesoft.lsp.convert.LspConvertersSharedToClient.ClientTextDocumentIdentifierConverter
import org.mulesoft.lsp.feature.common.ClientTextDocumentIdentifier

import scala.scalajs.js

// $COVERAGE-OFF$ Incompatibility between scoverage and scalaJS

@js.native
trait ClientRegisterProfileParams extends js.Object {
  def textDocument: ClientTextDocumentIdentifier = js.native
}

object ClientRegisterProfileParams {
  def apply(internal: RegisterProfileParams): ClientRegisterProfileParams = {
    js.Dynamic
      .literal(
        oldDocument = internal.textDocument.toClient
      )
      .asInstanceOf[ClientRegisterProfileParams]
  }
}

// $COVERAGE-ON$
