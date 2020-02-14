package org.mulesoft.lsp.edit

import org.mulesoft.lsp.common.Range

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * A textual edit applicable to a text document.
  *
  * @param range   The range of the text document to be manipulated. To insert
  *                text into a document create a range where start === end.
  * @param newText The string to be inserted. For delete operations use an
  *                empty string.
  */

@JSExportAll
@JSExportTopLevel("TextEditorg.mulesoft.als.client.lsp.Command")
case class TextEdit(range: Range, newText: String)