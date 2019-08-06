package org.mulesoft.language.outline.structure.structureDefault

import org.mulesoft.high.level.interfaces.IParseResult
import org.mulesoft.language.outline.common.commonInterfaces.{LabelProvider, VisibilityFilter}

class DefaultVisibilityFilter extends VisibilityFilter {

  /**
    * Allows blocking some nodes from being added to the structure tree, on top of what
    * StructureBuilder returns.
    *
    * @param node
    */
  def apply(node: IParseResult): Boolean = {
    true
  }
}

object NonEmptyNameVisibilityFilter {
  def apply(labelProvider: LabelProvider): VisibilityFilter =
    (node: IParseResult) => labelProvider.getLabelText(node).nonEmpty
}