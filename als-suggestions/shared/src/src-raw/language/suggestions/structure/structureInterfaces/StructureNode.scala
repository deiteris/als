package org.mulesoft.als.suggestions.structure.structureInterfaces

import org.mulesoft.als.suggestions.structure.raml_1_parser.Raml1ParserIndex
import org.mulesoft.als.suggestions.structure.structureInterfaces.StructureNodeJSON;
import org.mulesoft.als.suggestions.structure.structureInterfaces.StructureNode;
import org.mulesoft.als.suggestions.structure.structureInterfaces.ContentProvider;

trait StructureNode extends StructureNodeJSON {
  var children: Array[StructureNode]
  def getSource(): IParseResult
  def toJSON(): StructureNodeJSON
}
