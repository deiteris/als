package org.mulesoft.lsp.feature.diagnostic

case object DiagnosticSeverity extends Enumeration {
  type DiagnosticSeverity = Value

  /**
    * Reports an error.
    */
  val Error: Value = Value(0)

  /**
    * Reports a warning.
    */
  val Warning: Value = Value(1)

  /**
    * Reports an information.
    */
  val Information: Value = Value(2)

  /**
    * Reports a hint.
    */
  val Hint: Value = Value(3)
}
