package org.mulesoft.high.level.test.OAS20.AST.editing

import org.mulesoft.high.level.test.OAS20.OAS20ASTEditingTest

class ResponseObject extends OAS20ASTEditingTest{

    test("Response Definition Object 'key' editing. YAML"){
        runAttributeEditingTest("ResponseObject/ResponseObject.yml", project => {
            project.rootASTUnit.rootNode.elements("responses").head.attribute("key")
        }, "updatedResponseKeyValue")
    }

    test("Response Definition Object 'key' editing. JSON"){
        runAttributeEditingTest("ResponseObject/ResponseObject.json", project => {
            project.rootASTUnit.rootNode.elements("responses").head.attribute("key")
        }, "updatedResponseKeyValue")
    }

//    test("Response Object 'code' editing. YAML"){
//        runAttributeEditingTest("ResponseObject/ResponseObject.yml", project => {
//            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.element("responses").get.elements("responses").head.attribute("code")
//        }, "201")
//    }

    test("Response Object 'code' editing. JSON"){
        runAttributeEditingTest("ResponseObject/ResponseObject.json", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.element("responses").get.elements("responses").head.attribute("code")
        }, "201")
    }

    test("Parameter Object '$ref' editing for refering parameter. YAML"){
        runAttributeEditingTest("ResponseObject/ResponseObjectRef.json", project => {
            project.rootASTUnit.rootNode.elements("paths").head.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.attribute("$ref")
        }, "#/responses/r2")
    }

    test("Parameter Object '$ref' editing for refering parameter. JSON"){
        runAttributeEditingTest("ResponseObject/ResponseObjectRef.json", project => {
            project.rootASTUnit.rootNode.elements("paths").head.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.attribute("$ref")
        }, "#/responses/r2")
    }

    //    test("Parameter Object '$ref' editing for parameter expressed as AMF Parameter. YAML") {
    //
    //        runAttributeCreationTest("ResponseObject/ResponseObjectRef.yml", project => {
    //            Some(project.rootASTUnit.rootNode.elements("paths").head.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses")(1))
    //        }, "$ref", "#/responses/r2")
    //    }

    test("Parameter Object '$ref' editing for parameter expressed as AMF Parameter. JSON") {
        runAttributeCreationTest("ResponseObject/ResponseObjectRef.json", project => {
            Some(project.rootASTUnit.rootNode.elements("paths").head.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses")(1))
        }, "$ref", "#/responses/r2")
    }

    test("ResponseObject description editing YAML"){
        runAttributeEditingTest( "OperationObject/OperationObject2.yml", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.attribute("description")
        }, "text")
    }

    test("ResponseObject schema editing YAML"){
        runAttributeEditingTest( "OperationObject/OperationObject2.yml", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("schema").head.attribute("$ref")
        }, "#/definitions/T1")
    }

    test("ResponseObject headers editing YAML"){
        runAttributeEditingTest( "OperationObject/OperationObject2.yml", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("headers").head.attribute("name")
        }, "X-Rate")
    }

//    test("ResponseObject example editing YAML"){
//        runAttributeEditingTest( "OperationObject/OperationObject2.yml", project => {
//            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("example").head.attribute("mimeType")
//        }, "application/xml")
//    }

    test("ResponseObject description editing JSON"){
        runAttributeEditingTest( "OperationObject/OperationObject2.json", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.attribute("description")
        }, "text")
    }

    test("ResponseObject schema editing JSON"){
        runAttributeEditingTest( "OperationObject/OperationObject2.json", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("schema").head.attribute("$ref")
        }, "#/definitions/T1")
    }

    test("ResponseObject headers editing JSON"){
        runAttributeEditingTest( "OperationObject/OperationObject2.json", project => {
            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("headers").head.attribute("name")
        }, "X-Rate")
    }

//    test("ResponseObject example editing JSON"){
//        runAttributeEditingTest( "OperationObject/OperationObject2.json", project => {
//            project.rootASTUnit.rootNode.element("paths").get.elements("paths").head.elements("operations").head.elements("responses").head.elements("responses").head.elements("example").head.attribute("mimeType")
//        }, "application/xml")
//    }
}