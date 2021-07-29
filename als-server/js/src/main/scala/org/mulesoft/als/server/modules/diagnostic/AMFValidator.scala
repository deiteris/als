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
  private var goInstance: Option[Go] = None
  override def apply(): Future[Validator] = {
    if (goInstance.isEmpty || goInstance.exists(_.exited.exists(b => b))) {
      GoWasmLoader
        .load()
        .map(go => {
          goInstance = Some(go)
          new AMFValidator()
        })
    } else {
      Future {
        new AMFValidator()
      }
    }

  }
}
