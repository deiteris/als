package org.mulesoft.als.server.modules.diagnostic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFValidator extends Validator {

  /**
    * profileString := args[0].String()
    * dataString := args[1].String()
    * debug := args[2].Bool()
    */
  override def validate(profileString: String, dataString: String, debug: Boolean): String =
    Global.`__AMF__validateCustomProfile`(profileString, dataString, debug)
}

object AMFValidator extends ValidatorBuilder {
  override def apply(): Future[Validator] = {
    GoWasmLoader.load().map(_ => new AMFValidator())
  }
}
