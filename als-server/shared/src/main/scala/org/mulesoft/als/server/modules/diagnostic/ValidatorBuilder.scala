package org.mulesoft.als.server.modules.diagnostic
import scala.concurrent.Future

trait ValidatorBuilder {

  def apply(): Future[Validator]
}

trait Validator {

  /**
    * profileString := args[0].String()
    * dataString := args[1].String()
    * debug := args[2].Bool()
    */
  def validate(profileString: String, dataString: String, debug: Boolean): String
}
