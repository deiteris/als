package org.mulesoft.als.server.modules.diagnostic

import org.scalatest.FunSuite

class OPAValidatorReportLoaderTest extends FunSuite {

  val example =
    """{
      |   "@type": "http://www.w3.org/ns/shacl#ValidationReport",
      |   "http://www.w3.org/ns/shacl#conforms": false,
      |   "http://www.w3.org/ns/shacl#result": [
      |     {
      |       "@type": [
      |         "http://www.w3.org/ns/shacl#ValidationResult"
      |       ],
      |       "http://a.ml/vocabularies/validation#trace": [
      |         {
      |           "@type": [
      |             "http://a.ml/vocabularies/validation#TraceMessage"
      |           ],
      |           "http://a.ml/vocabularies/validation#component": "minCount",
      |           "http://www.w3.org/ns/shacl#focusNode": "amf://id#14",
      |           "http://www.w3.org/ns/shacl#resultPath": "shapes.schema / shacl.minLength",
      |           "http://www.w3.org/ns/shacl#traceValue": {
      |             "actual": 0,
      |             "condition": ">=",
      |             "expected": 1,
      |             "negated": false
      |           }
      |         }
      |       ],
      |       "http://www.w3.org/ns/shacl#focusNode": {
      |         "@id": "amf://id#14"
      |       },
      |       "http://www.w3.org/ns/shacl#resultMessage": "Scalars in parameters must have minLength defined",
      |       "http://www.w3.org/ns/shacl#resultSeverity": {
      |         "@id": "http://www.w3.org/ns/shacl#Violation"
      |       },
      |       "http://www.w3.org/ns/shacl#sourceShape": {
      |         "@id": "scalar-parameters"
      |       }
      |     },
      |     {
      |       "@type": [
      |         "http://www.w3.org/ns/shacl#ValidationResult"
      |       ],
      |       "http://a.ml/vocabularies/validation#trace": [
      |         {
      |           "@type": [
      |             "http://a.ml/vocabularies/validation#TraceMessage"
      |           ],
      |           "http://a.ml/vocabularies/validation#component": "minCount",
      |           "http://www.w3.org/ns/shacl#focusNode": "amf://id#5",
      |           "http://www.w3.org/ns/shacl#resultPath": "shapes.schema / shacl.minLength",
      |           "http://www.w3.org/ns/shacl#traceValue": {
      |             "actual": 0,
      |             "condition": ">=",
      |             "expected": 1,
      |             "negated": false
      |           }
      |         }
      |       ],
      |       "http://www.w3.org/ns/shacl#focusNode": {
      |         "@id": "amf://id#5"
      |       },
      |       "http://www.w3.org/ns/shacl#resultMessage": "Scalars in parameters must have minLength defined",
      |       "http://www.w3.org/ns/shacl#resultSeverity": {
      |         "@id": "http://www.w3.org/ns/shacl#Violation"
      |       },
      |       "http://www.w3.org/ns/shacl#sourceShape": {
      |         "@id": "scalar-parameters"
      |       }
      |     }
      |   ]
      |   }""".stripMargin
  test("test") {
    val report = new OPAValidatorReportLoader().load(example)
    assert(report.results.nonEmpty)
    assert(
      report.results.head.targetNode == "file://test/data/integration/profile1.negative.data.raml#/web-api/end-points/%2Fendpoint1/get/request/parameter/query/a")
  }

}
