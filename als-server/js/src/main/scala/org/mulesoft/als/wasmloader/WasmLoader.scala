package org.mulesoft.als.wasmloader

import org.mulesoft.als.wasmloader.Global._
import org.scalajs.dom.experimental.Fetch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
object WasmLoader {
  def load(): Future[Unit] = {
    val go = new GoEnvLoader().init()
    for {
      result <- WebAssembly.instantiateStreaming(Fetch.fetch("main.wasm"), go.importObject).toFuture
    } yield {
      println(result.instance)
      go.run(result.instance)
      println("Go env loaded")
    }
  }
}
