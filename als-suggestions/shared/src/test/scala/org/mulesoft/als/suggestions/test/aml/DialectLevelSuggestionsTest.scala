package org.mulesoft.als.suggestions.test.aml

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies.model.domain.{DocumentMapping, NodeMapping, PropertyMapping}
import org.mulesoft.als.suggestions.test.SuggestionsTest
import org.scalatest.Assertion

import scala.concurrent.Future

trait DialectLevelSuggestionsTest extends SuggestionsTest {

  /**
    *
    * @param path : path of the dialect instance for parse
    * @param goldenDialectPath : golden path of dialect to check that the defined by field is ok
    * @param nodeName : node father name for suggest properties. Send None (default) if is root node
    * @return
    */
  def runDialectTest(path: String,
                     goldenDialectPath: String,
                     nodeName: Option[String] = None,
                     level: Int = 1,
                     label: String = "*",
                     labels: Array[String] = Array("*")): Future[Assertion] = {
    buildModel(path, label, true, labels).flatMap { r =>
      r.u match {
        case d: DialectInstance =>
          AMLPlugin.registry.dialectFor(d) match {
            case Some(d: Dialect) =>
              assert(d.location().contains(goldenDialectPath))
              buildCompletionProviderFromUnit(r).flatMap(cp =>
                cp.suggest.map(_.map(_.text).toSet).map(assertByDialect(_, d, nodeName, path, level)))
            case _ => fail(s"Cannot find dialect for ${d.definedBy().value()}")
          }
        case other => fail(s"Dialect Instance expected but ${other.meta.getClass.getName} found")
      }
    }
  }

  private def getPropertiesByPath(d: Dialect, nodeName: String): Seq[PropertyMapping] = {
    val de: Option[DomainElement] = d.declares.find(de => de.id endsWith s"/${nodeName}")
    de match {
      case Some(n: NodeMapping) => n.propertiesMapping()
    }
  }

  def assertByDialect(actual: Set[String], d: Dialect, nodeName: Option[String], path: String, level: Int): Assertion = {
    nodeName match {
      case Some(n) => {
        val dialectProps = getPropertiesByPath(d, n).map(addPropTrailingsSpaces(_, level)).toSet
        assert(path, actual, dialectProps)
      }
      case _ => {
        // all root nodes
        val mapping: DocumentMapping = d.documents().root()
        val dialectProps: Set[String] =
          d.declares
            .find(_.id == mapping.encoded().value())
            .collectFirst({ case n: NodeMapping => n })
            .map(e => e.propertiesMapping().map(addPropTrailingsSpaces(_, level)).toSet)
            .getOrElse(Set.empty) ++ mapping
            .declaredNodes()
            .map(_.name().value() + ":\n" + (" " * (level * 2))) // declared cannot be scalars??
        assert(path, actual, dialectProps)
      }
    }
  }

  private def addPropTrailingsSpaces(propertyMapping: PropertyMapping, level: Int): String = {
    if (propertyMapping.literalRange().option().isEmpty) {
      propertyMapping.name().value() + ":\n" + (" " * (level * 2))
    } else propertyMapping.name().value() + ":"
  }

}