package org.mulesoft.als.wasmloader

import org.mulesoft.als.wasmloader.Global._
import org.scalajs.dom.experimental.{Fetch, HeadersInit, Response, ResponseInit}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Object.{entries, keys}
object WasmLoader {
  def load(): Future[Unit] = {
    val go = new GoEnvLoader().init()
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
      go.run(result.instance)
      println("Go env loaded")
    }
  }
}
