package org.mulesoft.high.level.test.OAS20.AST

import org.mulesoft.high.level.test.OAS20.OAS20ASTTest

class SwaggerObjectJson extends OAS20ASTTest{

    test("Swagger ObjectJson info"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 1
            var actualValue = project.rootASTUnit.rootNode.elements("info").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson Host"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = "my.api.com"
            project.rootASTUnit.rootNode.attribute("host") match {
                case Some(a) => a.value match {
                    case Some("my.api.com") => succeed
                    case _ => fail(s"Expected value: $expectedValue, actual: ${a.value}")
                }
                case _ => fail("'host' attribute not found")
            }
        })
    }

    test("Swagger ObjectJson basePath"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = "/v1"
            project.rootASTUnit.rootNode.attribute("basePath") match {
                case Some(a) => a.value match {
                    case Some("/v1") => succeed
                    case _ => fail(s"Expected value: $expectedValue, actual: ${a.value}")
                }
                case _ => fail("'basePath' attribute not found")
            }
        })
    }

    test("Swagger ObjectJson schemes"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.attributes("schemes").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson consumes"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 1
            var actualValue = project.rootASTUnit.rootNode.attributes("consumes").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson produces"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.attributes("produces").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson paths"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 1
            var actualValue = project.rootASTUnit.rootNode.elements("paths").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson definitions"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.elements("definitions").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson parameters"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 1
            var actualValue = project.rootASTUnit.rootNode.elements("parameters").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson responses"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.elements("responses").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson securityDefinitions"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.elements("securityDefinitions").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson security"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 2
            var actualValue = project.rootASTUnit.rootNode.elements("security").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }

    test("Swagger ObjectJson externalDocs"){
        runTest( "ASTTests/SwaggerObject/SwaggerObject.json", project => {
            var expectedValue = 1
            var actualValue = project.rootASTUnit.rootNode.elements("externalDocs").length
            if (actualValue == expectedValue)
                succeed
            else
                fail(s"Expected value: $expectedValue, actual: ${actualValue}")
        })
    }
}