package org.mulesoft.als.server.modules.diagnostic

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSImport

@JSImport("../../src/main/resources/wasm_exec.js", "GoEnvLoader")
class GoEnvLoader extends js.Any {
  def init(): Go               = js.native
  val exited: UndefOr[Boolean] = js.native // whether the Go program has exited
}
