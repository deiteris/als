package org.mulesoft.als.server.modules.diagnostic

import amf.core.annotations.LexicalInformation
import amf.{ProfileName, ProfileNames}
import org.yaml.model.{YMap, YMapEntry, YNode, YNodeLike}
import org.yaml.parser.JsonParser
import amf.core.parser.{Position, ScalarNode, YMapOps}
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import org.yaml.model.YNodeLike.toBoolean
class OPAValidatorReportLoader {

  implicit class URI(value: String) {
    val SHACL_ALIAS = "http://www.w3.org/ns/shacl#"
    val TRACE_ALIAS = "http://a.ml/vocabularies/validation#"
    val AML_ALIAS   = "http://a.ml/vocabularies/amf/parser#"
    def shacl       = SHACL_ALIAS + value
    def trace       = TRACE_ALIAS + value
    def aml         = AML_ALIAS + value
    def unshacl     = value.replace(SHACL_ALIAS, "")
  }

  def load(report: String): AMFValidationReport = {
    val doc       = JsonParser.apply(report).document()
    val map: YMap = doc.node.as[YMap]
    val conforms  = map.key("conforms".shacl).forall(n => toBoolean(n.value))
    val results   = map.key("result".shacl).map(n => n.value.as[Seq[YMap]]).getOrElse(Nil).map(loadResult)
    AMFValidationReport(conforms, "", ProfileNames.AMF, results)
  }

  def loadResult(map: YMap) = {
    val node    = map.key("focusNode".shacl).flatMap(readIdValue).getOrElse("")
    val message = map.key("resultMessage".shacl).flatMap(_.value.asScalar).map(_.text).getOrElse("")
    val level   = map.key("resultSeverity".shacl).flatMap(readIdValue).map(_.unshacl).getOrElse("Violation")
    val lexical = map.key("trace".trace).flatMap(readTrace)
    AMFValidationResult(message, level, node, None, "", lexical, None, null)

  }

  def readTrace(e: YMapEntry) = {
    val head = e.value.as[Seq[YNode]].map(_.as[YMap]).head
    head.key("lexicalPosition".aml).map(e => parseLexical(e.value.as[YMap])).map(LexicalInformation(_))
  }

  def parseLexical(m: YMap) = {
    amf.core.parser.Range(
      m.key("end".aml).map(e => parsePosition(e.value.as[YMap])).getOrElse(Position.ZERO),
      m.key("start".aml).map(e => parsePosition(e.value.as[YMap])).getOrElse(Position.ZERO)
    )
  }

  def parsePosition(m: YMap) = {
    new Position(parseNumber(m, "line").getOrElse(1), parseNumber(m, "column").getOrElse(0))
  }

  def parseNumber(m: YMap, field: String): Option[Int] = m.key(field.aml).map(e => e.value.as[Int])

  def readIdValue(e: YMapEntry) = {
    e.value.as[YMap].key("@id").flatMap(_.value.asScalar).map(_.text)
  }

}
