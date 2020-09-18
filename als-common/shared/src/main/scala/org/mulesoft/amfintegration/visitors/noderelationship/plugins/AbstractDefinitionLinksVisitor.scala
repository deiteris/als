package org.mulesoft.amfintegration.visitors.noderelationship.plugins

import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.ParametrizedDeclaration
import amf.core.model.domain.{AmfArray, AmfElement}
import amf.core.parser.FieldEntry
import amf.core.vocabulary.Namespace
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import org.mulesoft.amfintegration.AmfImplicits._
import org.mulesoft.amfintegration.relationships.RelationshipLink
import org.mulesoft.amfintegration.visitors.WebApiElementVisitorFactory
import org.mulesoft.amfintegration.visitors.noderelationship.NodeRelationshipVisitorType

class AbstractDefinitionLinksVisitor extends NodeRelationshipVisitorType {
  override protected def innerVisit(element: AmfElement): Seq[RelationshipLink] =
    element match {
      case o: Operation =>
        extractFromEntries(o.fields.fields())
      case e: EndPoint =>
        extractFromEntries(e.fields.fields())
      case _ => Nil
    }

  private def extractFromEntries(entries: Iterable[FieldEntry]): Seq[RelationshipLink] =
    entries
      .find(fe => fe.field.value == Namespace.Document + "extends")
      .map(parametrizedDeclarationTargetsWithPosition)
      .getOrElse(Nil)

  private def parametrizedDeclarationTargetsWithPosition(fe: FieldEntry): Seq[RelationshipLink] =
    fe.value.value match {
      case array: AmfArray =>
        array.values.flatMap {
          case p: ParametrizedDeclaration =>
            p.annotations
              .ast()
              .flatMap(source => Option(p.target).flatMap(t => t.annotations.ast().map(target => (source, target))))
              .map(t => RelationshipLink(t._1, t._2, getName(p)))
          case _ => None
        }
      case _ => Nil
    }
}

object AbstractDefinitionLinksVisitor extends WebApiElementVisitorFactory {
  override def apply(bu: BaseUnit): Option[AbstractDefinitionLinksVisitor] =
    if (applies(bu))
      Some(new AbstractDefinitionLinksVisitor())
    else None
}