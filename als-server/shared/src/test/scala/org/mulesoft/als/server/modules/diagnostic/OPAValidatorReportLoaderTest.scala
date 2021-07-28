package org.mulesoft.als.server.modules.diagnostic

import org.scalatest.FunSuite

class OPAValidatorReportLoaderTest extends FunSuite {

  val example =
    """{
      |  "@type": "http://www.w3.org/ns/shacl#ValidationReport",
      |  "http://www.w3.org/ns/shacl#conforms": false,
      |  "http://www.w3.org/ns/shacl#result": [
      |    {
      |      "@type": [
      |        "http://www.w3.org/ns/shacl#ValidationResult"
      |      ],
      |      "http://a.ml/vocabularies/validation#trace": [
      |        {
      |          "@type": [
      |            "http://a.ml/vocabularies/validation#TraceMessage"
      |          ],
      |          "http://a.ml/vocabularies/amf/parser#lexicalPosition": {
      |            "@type": [
      |              "http://a.ml/vocabularies/amf/parser#Position"
      |            ],
      |            "http://a.ml/vocabularies/amf/parser#end": {
      |              "@type": "http://a.ml/vocabularies/amf/parser#Location",
      |              "http://a.ml/vocabularies/amf/parser#column": 0,
      |              "http://a.ml/vocabularies/amf/parser#line": 9
      |            },
      |            "http://a.ml/vocabularies/amf/parser#start": {
      |              "@type": "http://a.ml/vocabularies/amf/parser#Location",
      |              "http://a.ml/vocabularies/amf/parser#column": 6,
      |              "http://a.ml/vocabularies/amf/parser#line": 8
      |            }
      |          },
      |          "http://a.ml/vocabularies/validation#component": "minCount",
      |          "http://www.w3.org/ns/shacl#focusNode": "file://test/data/integration/profile1/negative.data.raml#/web-api/end-points/%2Fendpoint1/get/request/parameter/query/a",
      |          "http://www.w3.org/ns/shacl#resultPath": "shapes.schema / shacl.minLength",
      |          "http://www.w3.org/ns/shacl#traceValue": {
      |            "actual": 0,
      |            "condition": ">=",
      |            "expected": 1,
      |            "negated": false
      |          }
      |        }
      |      ],
      |      "http://www.w3.org/ns/shacl#focusNode": {
      |        "@id": "file://test/data/integration/profile1/negative.data.raml#/web-api/end-points/%2Fendpoint1/get/request/parameter/query/a"
      |      },
      |      "http://www.w3.org/ns/shacl#resultMessage": "Scalars in parameters must have minLength defined",
      |      "http://www.w3.org/ns/shacl#resultSeverity": {
      |        "@id": "http://www.w3.org/ns/shacl#Violation"
      |      },
      |      "http://www.w3.org/ns/shacl#sourceShape": {
      |        "@id": "scalar-parameters"
      |      }
      |    },
      |    {
      |      "@type": [
      |        "http://www.w3.org/ns/shacl#ValidationResult"
      |      ],
      |      "http://a.ml/vocabularies/validation#trace": [
      |        {
      |          "@type": [
      |            "http://a.ml/vocabularies/validation#TraceMessage"
      |          ],
      |          "http://a.ml/vocabularies/amf/parser#lexicalPosition": {
      |            "@type": [
      |              "http://a.ml/vocabularies/amf/parser#Position"
      |            ],
      |            "http://a.ml/vocabularies/amf/parser#end": {
      |              "@type": "http://a.ml/vocabularies/amf/parser#Location",
      |              "http://a.ml/vocabularies/amf/parser#column": 8,
      |              "http://a.ml/vocabularies/amf/parser#line": 18
      |            },
      |            "http://a.ml/vocabularies/amf/parser#start": {
      |              "@type": "http://a.ml/vocabularies/amf/parser#Location",
      |              "http://a.ml/vocabularies/amf/parser#column": 6,
      |              "http://a.ml/vocabularies/amf/parser#line": 18
      |            }
      |          },
      |          "http://a.ml/vocabularies/validation#component": "minCount",
      |          "http://www.w3.org/ns/shacl#focusNode": "file://test/data/integration/profile1/negative.data.raml#/web-api/end-points/%2Fendpoint2/get/request/parameter/query/d",
      |          "http://www.w3.org/ns/shacl#resultPath": "shapes.schema / shacl.minLength",
      |          "http://www.w3.org/ns/shacl#traceValue": {
      |            "actual": 0,
      |            "condition": ">=",
      |            "expected": 1,
      |            "negated": false
      |          }
      |        }
      |      ],
      |      "http://www.w3.org/ns/shacl#focusNode": {
      |        "@id": "file://test/data/integration/profile1/negative.data.raml#/web-api/end-points/%2Fendpoint2/get/request/parameter/query/d"
      |      },
      |      "http://www.w3.org/ns/shacl#resultMessage": "Scalars in parameters must have minLength defined",
      |      "http://www.w3.org/ns/shacl#resultSeverity": {
      |        "@id": "http://www.w3.org/ns/shacl#Violation"
      |      },
      |      "http://www.w3.org/ns/shacl#sourceShape": {
      |        "@id": "scalar-parameters"
      |      }
      |    }
      |  ]
      |} """.stripMargin
  test("test") {
    val report = new OPAValidatorReportLoader().load(example)
    assert(report.results.nonEmpty)
    assert(
      report.results.head.targetNode == "file://test/data/integration/profile1/negative.data.raml#/web-api/end-points/%2Fendpoint1/get/request/parameter/query/a")
    assert(report.results.head.position.isDefined)
    assert(report.results.head.position.get.range.start.line == 9)
  }

}
