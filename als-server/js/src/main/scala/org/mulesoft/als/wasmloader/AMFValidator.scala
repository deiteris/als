package org.mulesoft.als.wasmloader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFValidator {

  /**
    * profileString := args[0].String()
		* dataString := args[1].String()
		* debug := args[2].Bool()
    */
  def validate(profileString: String, dataString: String, debug: Boolean): String =
    Global.`__AMF__validateCustomProfile`(profileString, dataString, debug)
}

object AMFValidator {
  def apply(): Future[AMFValidator] = {
    GoWasmLoader.load().map(_ => new AMFValidator())
  }
}
