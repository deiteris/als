package org.mulesoft.als.server.modules.diagnostic

import amf.core.model.document.BaseUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PlatformSerializer {
  def serialize(u: BaseUnit): Future[String]
}

object EmptyPlatformSerializer extends PlatformSerializer {
  override def serialize(u: BaseUnit): Future[String] = Future {
    "ERROR: Called on empty serializer"
  }
}
