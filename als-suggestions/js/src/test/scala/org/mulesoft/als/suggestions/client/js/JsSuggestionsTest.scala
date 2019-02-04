package org.mulesoft.als.suggestions.client.js

import amf.client.remote.Content
import amf.client.resource.{ClientResourceLoader, ResourceLoader}
import amf.core.remote.Vendor
import org.mulesoft.high.level.interfaces.{DirectoryResolver => InternalDirectoryResolver}
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class JsSuggestionsTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("Basic suggestion on file") {
    val fileContent =
      """#%RAML 1.0
        |title: Project 2
        |
      """.stripMargin

    val fileLoader = js
      .use(new ResourceLoader {
        override def fetch(resource: String): js.Promise[Content] =
          js.Promise.resolve[Content](new Content(fileContent, resource))

        override def accepts(resource: String): Boolean = resource == "file:///api.raml"
      })
      .as[ClientResourceLoader]

    JsSuggestions
      .init()
      .toFuture
      .flatMap(_ => {
        JsSuggestions
          .suggest(Vendor.RAML.name, "file:///api.raml", 11, js.Array(fileLoader))
          .toFuture
          .map(suggestions => assertResult(15)(suggestions.length))
      })
  }

  test("Basic suggestion on file root") {
    val fileContent =
      """#%RAML 1.0
        |title: Project 2
        |
      """.stripMargin

    val fileLoader = js
      .use(new ResourceLoader {
        override def fetch(resource: String): js.Promise[Content] =
          js.Promise.resolve[Content](new Content(fileContent, resource))

        override def accepts(resource: String): Boolean = resource == "file:///api.raml"
      })
      .as[ClientResourceLoader]

    JsSuggestions
      .init()
      .toFuture
      .flatMap(_ => {
        JsSuggestions
          .suggest(Vendor.RAML.name, "file:///api.raml", 28, js.Array(fileLoader))
          .toFuture
          .map(suggestions => assertResult(15)(suggestions.length))
      })
  }

  test("Custom Directory Resolver") {
    val api = "#%RAML 1.0\ntitle: Project 2\ntraits:\n  t: !include  \ntypes:\n  a: string"

    val fragment =
      """#%RAML 1.0 Trait
        |responses:
        | 200:
      """.stripMargin

    val fileLoader = js
      .use(new ResourceLoader {
        override def fetch(resource: String): js.Promise[Content] = {
          if (resource == "file:///dir/api.raml")
            js.Promise.resolve[Content](new Content(api, resource))
          else
            js.Promise.resolve[Content](new Content(fragment, resource))
        }

        override def accepts(resource: String): Boolean =
          resource == "file:///dir/api.raml" || resource == "file://dir/fragment.raml"
      })
      .as[ClientResourceLoader]

    val clientResolver = js.use(new ClientDirectoryResolver {
      override def exists(path: String): js.Promise[Boolean] =
        Future(Seq("file:///api.raml", "file://fragment.raml", "file://another.raml").contains(path)).toJSPromise

      override def readDir(path: String): js.Promise[js.Array[String]] = {
        Future(Seq("file:///dir/fragment.raml", "file://dir/another.raml")).map(_.toJSArray).toJSPromise
      }

      override def isDirectory(path: String): js.Promise[Boolean] = {
        Future(path endsWith "dir/").toJSPromise
      }
    }).as[ClientDirectoryResolver]

    JsSuggestions
      .init()
      .toFuture
      .flatMap(_ => {
        JsSuggestions
          .suggest(Vendor.RAML.name,
                   "file:///dir/api.raml",
                   51,
                   js.Array(fileLoader),
                   Some(clientResolver).orUndefined)
          .toFuture
          .map(suggestions => {
            val seq = suggestions.toSeq
            seq.size should be(2)
            seq.head.text should be("fragment.raml")
            seq.last.text should be("another.raml")
          })
      })
  }
}
