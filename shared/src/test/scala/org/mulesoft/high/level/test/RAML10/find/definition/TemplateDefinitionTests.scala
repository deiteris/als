package org.mulesoft.high.level.test.RAML10.find.definition

import org.mulesoft.high.level.ReferenceSearchResult
import org.mulesoft.high.level.test.RAML10.RamlFindDefinitionTest
import org.scalatest.Assertion

class TemplateDefinitionTests extends RamlFindDefinitionTest{

    test("Resource type definition test 1. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test001/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 2. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test002/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 3. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test003/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 4. Negative") {
        runFindDefinitionTest("resourceTypes/test004/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Resource type definition test 5. Negative") {
        runFindDefinitionTest("resourceTypes/test005/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Resource type definition test 6. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test006/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 7. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test007/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 8. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test008/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 9. Negative") {
        runFindDefinitionTest("resourceTypes/test009/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Resource type definition test 10. Negative") {
        runFindDefinitionTest("resourceTypes/test010/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Resource type definition test 11. Negative") {
        runFindDefinitionTest("resourceTypes/test011/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Resource type definition test 12. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test012/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 13. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test013/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 14. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test014/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Resource type definition test 15. Positive") {
        var templateName = "rt"
        var templateType = "ResourceType"
        runFindDefinitionTest("resourceTypes/test015/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 1. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test001/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 2. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test002/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 3. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test003/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 4. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test004/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 5. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test005/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 6. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test006/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 7. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test007/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 8. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test008/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 9. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test009/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 10. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test010/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    test("Trait definition test 11. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test011/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 12. Negative") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test012/api.raml", x => testFindTemplateDefinitionNegative(x))
    }

    test("Trait definition test 13. Positive") {
        var templateName = "tr"
        var templateType = "Trait"
        runFindDefinitionTest("traits/test013/api.raml", x => testFindTemplateDefinitionPositive(x,templateType,templateName))
    }

    def testFindTemplateDefinitionPositive(opt:Option[ReferenceSearchResult], templateType:String, templateName:String):Assertion = {
        opt match {
            case Some(c) =>
                c.definition.attribute("name") match {
                    case Some(a) => a.value match {
                        case Some(aVal) =>
                            if(aVal != templateName){
                                fail(s"ExpectedName: $templateName, actual:$aVal")
                            }
                            else {
                                val definition = c.definition.definition
                                if (!definition.nameId.contains(templateType)) {
                                    fail(s"$templateType is expected but found  ${definition.nameId.get}")
                                }
                                else if(c.references.isEmpty){
                                    fail("References array is empty")
                                }
                                else {
                                    succeed
                                }
                            }
                        case _ => fail("'name' attribute has no value")
                    }

                    case _ => fail("'name' attribute not found")
                }
            case None => fail("Search result is empty")
        }
    }

    def testFindTemplateDefinitionNegative(opt:Option[ReferenceSearchResult]):Assertion = {
        opt match {
            case Some(c) =>
                fail(s"No definition is expected to be found, but a ${c.definition.definition.nameId.get} is found")
            case None => succeed
        }
    }

}
