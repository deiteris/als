package org.mulesoft.als.wasmloader

import amf.core.unsafe.PlatformSecrets
import org.mulesoft.als.wasmloader.Global._
import org.scalajs.dom.experimental.{Fetch, HeadersInit, Response, ResponseInit}

import scala.scalajs.js.typedarray._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Object.entries
import scala.scalajs.js.{Dynamic, Promise, isUndefined}
import scala.scalajs.js.annotation.JSImport

object GoWasmLoader extends PlatformSecrets {
  private def isDefined(a: js.Any) = !isUndefined(a)

  /* Return true if js is running on node. */
  private def isNode: Boolean =
    if (isDefined(Dynamic.global.process) && isDefined(Dynamic.global.process.versions))
      isDefined(Dynamic.global.process.versions.node)
    else false

  def load(): Future[Unit] = {
    if (isNode) {
      println("Node")
      new NodeGoWasmLoader().load()
    } else {
      println("Browser")
      new BrowserGoWasmLoader().load()
    }
  }
}

sealed trait GoWasmLoader {
  def resolveModule(go: Go): Future[Instance]
  def load(): Future[Unit] = {
    val go = new GoEnvLoader().init()
    resolveModule(go).map(instance => {
      go.run(instance)
      println("Go env loaded")
    })
  }
}

sealed class NodeGoWasmLoader() extends GoWasmLoader {
  override def resolveModule(go: Go): Future[Instance] = {
    val file = "als-server/js/node-package/main.wasm"
    for {
      content  <- JsFs.promises.readFile(file).toFuture
      module   <- WebAssembly.compile(content).toFuture
      instance <- WebAssembly.instantiate(module, go.importObject).toFuture
    } yield {
      instance
    }
  }
}

sealed class BrowserGoWasmLoader() extends GoWasmLoader {
  override def resolveModule(go: Go): Future[Instance] = {
    for {
      response <- Fetch.fetch("main.wasm").toFuture
      body     <- response.blob().toFuture
      r <- Future {
        val hdrs = js.Dictionary.empty[js.Any]
        entries(response.headers).foreach(b => hdrs(b._1) = b._2.asInstanceOf[js.Any])
        hdrs("Content-Type") = "application/wasm"
        new Response(body, ResponseInit(response.status, response.statusText, hdrs.asInstanceOf[HeadersInit]))
      }
      result <- WebAssembly.instantiateStreaming(r, go.importObject).toFuture
    } yield {
      result.instance
    }
  }
}

@js.native
sealed trait JsFsPromises extends js.Object {
  def readFile(file: String): Promise[ArrayBuffer] = js.native
}

@js.native
sealed trait JsFs extends js.Object {
  val promises: JsFsPromises = js.native
}

@js.native
@JSImport("fs", JSImport.Namespace)
private object JsFs extends JsFs
