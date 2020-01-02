package org.mulesoft.als.client.lsp.textsync

import org.mulesoft.lsp.textsync.IndexDialectParams

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr

@js.native
trait ClientIndexDialectParams extends js.Object {
  def uri: String              = js.native
  def content: UndefOr[String] = js.native
}

object ClientIndexDialectParams {
  def apply(internal: IndexDialectParams): ClientIndexDialectParams =
    js.Dynamic
      .literal(
        uri = internal.uri,
        content = internal.content.orUndefined
      )
      .asInstanceOf[ClientIndexDialectParams]
}
