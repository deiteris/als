package org.mulesoft.als.client.lsp.feature.rename

import org.mulesoft.lsp.feature.rename.RenameClientCapabilities

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import js.JSConverters._

@js.native
trait ClientRenameClientCapabilities extends js.Object {
  def dynamicRegistration: UndefOr[Boolean] = js.native
  def prepareSupport: UndefOr[Boolean]      = js.native
}

object ClientRenameClientCapabilities {
  def apply(internal: RenameClientCapabilities): ClientRenameClientCapabilities =
    js.Dynamic
      .literal(
        dynamicRegistration = internal.dynamicRegistration.orUndefined,
        prepareSupport = internal.prepareSupport.orUndefined
      )
      .asInstanceOf[ClientRenameClientCapabilities]
}
