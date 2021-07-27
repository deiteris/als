package org.mulesoft.als

import org.mulesoft.als.server.ClientLogger

object DummyClientLogger extends ClientLogger {
  override def error(message: String): Unit = {}

  override def warn(message: String): Unit = {}

  override def info(message: String): Unit = {}

  override def log(message: String): Unit = {}
}
