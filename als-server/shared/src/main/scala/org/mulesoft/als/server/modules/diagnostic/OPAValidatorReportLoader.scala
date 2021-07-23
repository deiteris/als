package org.mulesoft.als.server.modules.diagnostic

import amf.{ProfileName, ProfileNames}
import org.yaml.model.YMap
import org.yaml.parser.JsonParser
import amf.core.parser.YMapOps
import amf.core.validation.{AMFValidationReport, AMFValidationResult}
import org.yaml.model.YNodeLike
import org.yaml.model.YNodeLike.toBoolean
class OPAValidatorReportLoader {

  implicit class URI(value: String) {
    val SHACL_ALIAS = "http://www.w3.org/ns/shacl#"
    def expanded    = SHACL_ALIAS + value
  }

  def load(report: String): AMFValidationReport = {
    val doc       = JsonParser.apply(report).document()
    val map: YMap = doc.node.as[YMap]
    val conforms  = map.key("conforms".expanded).forall(n => toBoolean(n.value))
    val results   = map.key("result".expanded).map(n => n.value.as[Seq[YMap]]).getOrElse(Nil).map(loadResult)
    AMFValidationReport(conforms, "", ProfileNames.AMF, results)
  }

  def loadResult(map: YMap) = {
    val node    = map.key("focusNode".expanded).flatMap(_.value.asScalar).map(_.text).getOrElse("")
    val message = map.key("resultMessage".expanded).flatMap(_.value.asScalar).map(_.text).getOrElse("")
    val level   = map.key("resultSeverity".expanded).flatMap(_.value.asScalar).map(_.text).getOrElse("Violation")
    AMFValidationResult(message, level, node, None, "", None, None, null)

  }

}
