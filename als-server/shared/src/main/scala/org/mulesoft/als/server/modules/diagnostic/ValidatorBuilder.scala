package org.mulesoft.als.server.modules.diagnostic
import scala.concurrent.ExecutionContext.Implicits.global
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

object EmptyValidatorBuilder extends ValidatorBuilder {
  override def apply(): Future[Validator] = Future {
    new Validator {

      /**
        * profileString := args[0].String()
        * dataString := args[1].String()
        * debug := args[2].Bool()
        */
      override def validate(profileString: String, dataString: String, debug: Boolean): String =
        "ERROR: Called on empty validator builder"
    }
  }
}
