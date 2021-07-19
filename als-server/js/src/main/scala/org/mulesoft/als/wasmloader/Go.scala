package org.mulesoft.als.wasmloader

import scala.scalajs.js
import scala.scalajs.js.UndefOr

@js.native
class Go extends js.Any {
  val importObject: js.Any     = js.native
  def run(prg: js.Any): Unit   = js.native
  val exited: UndefOr[Boolean] = js.native // whether the Go program has exited
}
