package org.mulesoft.als.server.modules.workspace

import org.mulesoft.lexer.SourceLocation
import org.mulesoft.lsp.feature.common.Location
import org.mulesoft.lsp.feature.link.DocumentLink

trait FileTree {
  def parsedUnits: Map[String, ParsedUnit]
  def nodeRelationships: Seq[(Location, Location)]
  def documentLinks: Map[String, Seq[DocumentLink]]
  def aliases: Seq[(SourceLocation, SourceLocation)]
}
