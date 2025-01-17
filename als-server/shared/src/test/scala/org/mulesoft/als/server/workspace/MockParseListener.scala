package org.mulesoft.als.server.workspace

import amf.core.benchmark.ExecutionLog.executionContext
import org.mulesoft.als.server.modules.ast.{BaseUnitListener, BaseUnitListenerParams}
import org.mulesoft.als.server.{AbstractTestClientNotifier, TimeoutFuture}

import scala.concurrent.Future

class MockParseListener(val timeoutMillis: Int = 1000)
    extends AbstractTestClientNotifier[BaseUnitListenerParams]
    with TimeoutFuture
    with BaseUnitListener {

  /**
    * Called on new AST available
    *
    * @param ast  - AST
    * @param uuid - telemetry UUID
    */
  override def onNewAst(ast: BaseUnitListenerParams, uuid: String): Unit = this.notify(ast)

  override def nextCall: Future[BaseUnitListenerParams] = timeoutFuture(super.nextCall, timeoutMillis)

  override def onRemoveFile(uri: String): Unit = {}
}
