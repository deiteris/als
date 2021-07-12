package org.mulesoft.als.wasmloader

import org.scalajs.dom.experimental.Response

import scala.scalajs.js
import scala.scalajs.js.Promise

@js.native
trait WebAssembly extends js.Object {
  def instantiateStreaming(source: Promise[Response], importObject: js.Any): Promise[ResultObject] = js.native
}

@js.native
trait ResultObject extends js.Object {
  val module: js.Any   = js.native
  val instance: js.Any = js.native
}
