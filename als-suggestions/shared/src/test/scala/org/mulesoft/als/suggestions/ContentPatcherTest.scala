package org.mulesoft.als.suggestions

import org.mulesoft.als.common.diff.{FileAssertionTest, ListAssertions}
import org.mulesoft.als.suggestions.interfaces.Syntax
import org.mulesoft.als.suggestions.patcher.{ColonToken, CommaToken, ContentPatcher, PatchToken, QuoteToken}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class ContentPatcherTest extends AsyncFunSuite with FileAssertionTest with ListAssertions {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://als-suggestions/shared/src/test/resources/test/patcher"

  test("Test in json key patch") {
    assertJson("in-key", List(QuoteToken, QuoteToken))
  }

  test("Test in json 2 key patch") {
    assertJson("in-key2", List(ColonToken, QuoteToken, QuoteToken, CommaToken))
  }

  test("unquoted key without comma") {
    assertJson("unquoted-key", List(QuoteToken, QuoteToken, ColonToken, QuoteToken, QuoteToken))
  }

  test("unquoted key without comma before entry") {
    assertJson("unquoted-key-before-entry",
               List(QuoteToken, QuoteToken, ColonToken, QuoteToken, QuoteToken, CommaToken))
  }

  test("unquoted key with comma") {
    assertJson("unquoted-key", List(QuoteToken, QuoteToken, ColonToken, QuoteToken, QuoteToken))
  }

  test("empty key without value") {
    assertJson("empty-key-without-value", List(ColonToken, QuoteToken, QuoteToken))
  }

  test("Only one quote at the beginning") {
    assertJson("only-one-quote", List(QuoteToken, ColonToken, QuoteToken, QuoteToken, CommaToken))
  }

  test("empty value") {
    assertJson("empty-value", List(QuoteToken, QuoteToken))
  }

  test("empty value open quote") {
    assertJson("empty-value-open-quote", List(QuoteToken))
  }

  test("empty value quoted") {
    assertJson("empty-value-quoted", List(QuoteToken))
  }

  private def assertJson(name: String, tokenList: List[PatchToken]): Future[Assertion] =
    assert(name, "json", tokenList)

  private def assert(name: String, syntax: String, tokenList: List[PatchToken]): Future[Assertion] = {
    val url      = basePath + s"/$name.$syntax"
    val expected = basePath + s"/$name.result.$syntax"
    for {
      c <- platform.resolve(url)
      patched <- Future {

        val content    = c.stream.toString
        val offset     = content.indexOf("*")
        val rawContent = content.substring(0, offset) + content.substring(offset + 1)
        ContentPatcher(rawContent, offset, Syntax.JSON).prepareContent()
      }
      tmp <- writeTemporaryFile(expected)(patched.content)
      r   <- assertDifferences(tmp, expected)
      finalAssertion <- if (r == succeed) {
        assert(patched.addedTokens, tokenList)
      } else r
    } yield finalAssertion
  }
}
