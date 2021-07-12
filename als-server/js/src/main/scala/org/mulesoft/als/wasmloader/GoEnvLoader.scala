package org.mulesoft.als.wasmloader

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("../../src/main/resources/wasm_exec.js", "GoEnvLoader")
class GoEnvLoader extends js.Any {
  def init(): Go = js.native
}
