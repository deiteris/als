package org.mulesoft.als.wasmloader

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSGlobalScope, JSImport}

@js.native
@JSGlobalScope
object Global extends js.Any {
  val WebAssembly: WebAssembly                                                                            = js.native
  def `__AMF__validateCustomProfile`(validationProfile: String, data: String, debugMode: Boolean): String = js.native
  def `__AMF__terminateValidator`(): js.Any                                                               = js.native
}
