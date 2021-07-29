package org.mulesoft.als.server.modules.diagnostic

import scala.scalajs.js
import scala.scalajs.js.{Promise, UndefOr}

@js.native
class Go extends js.Any {
  val importObject: js.Any              = js.native
  def run(prg: js.Any): Promise[js.Any] = js.native
  val exited: UndefOr[Boolean]          = js.native // whether the Go program has exited
}
