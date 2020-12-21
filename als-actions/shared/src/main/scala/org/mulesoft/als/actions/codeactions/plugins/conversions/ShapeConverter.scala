package org.mulesoft.als.actions.codeactions.plugins.conversions

import amf.ProfileNames
import amf.core.model.domain.{AmfObject, Shape}
import amf.plugins.document.webapi.annotations.{ParsedJSONSchema, SchemaIsJsonSchema}
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.shapes.resolution.stages.elements.CompleteShapeTransformationPipeline
import org.mulesoft.als.actions.codeactions.plugins.declarations.common.{
  BaseElementDeclarableExtractors,
  FileExtractor
}
import org.mulesoft.als.common.dtoTypes.{Position, PositionRange}
import org.mulesoft.amfintegration.AmfImplicits.AmfAnnotationsImp
import org.mulesoft.amfintegration.LocalIgnoreErrorHandler

trait ShapeConverter extends BaseElementDeclarableExtractors {

  protected def resolveShape(anyShape: AnyShape): Option[AnyShape] =
    new CompleteShapeTransformationPipeline(anyShape, LocalIgnoreErrorHandler, ProfileNames.RAML).resolve() match {
      case a: AnyShape => Some(a)
      case _           => None
    }

  // We wouldn't want to override amfObject as a whole as it's used for range comparisons and such
  protected lazy val resolvedAmfObject: Option[AmfObject] = amfObject match {
    case Some(shape: AnyShape) => resolveShape(shape.copyShape())
    case e                     => e
  }

  lazy val maybeAnyShape: Option[AnyShape] = extractShapeFromAmfObject(resolvedAmfObject)

  def extractShapeFromAmfObject(obj: Option[AmfObject]): Option[AnyShape] = {
    obj.flatMap {
      case s: AnyShape => Some(s)
      case _           => None
    }
  }

  def isJsonSchemaShape(obj: AmfObject): Boolean = {
    obj match {
      case s: AnyShape if isInlinedJsonSchema(s) => true
      case _                                     => false
    }
  }

  def containsPosition(obj: AmfObject, position: Option[Position]): Boolean =
    obj.annotations
      .lexicalInformation()
      .map(l => PositionRange(l.range))
      .exists(r => position.exists(r.contains))

  def isInlinedJsonSchema(shape: Shape): Boolean =
    shape.annotations.find(ann => ann.isInstanceOf[ParsedJSONSchema] || ann.isInstanceOf[SchemaIsJsonSchema]).isDefined

}