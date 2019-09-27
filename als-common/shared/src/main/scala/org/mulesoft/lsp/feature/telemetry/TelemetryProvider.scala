package org.mulesoft.lsp.feature.telemetry

import org.mulesoft.lsp.feature.telemetry.MessageTypes.MessageTypes

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val BEGIN_PARSE, END_PARSE, BEGIN_PARSE_PATCHED, END_PARSE_PATCHED, BEGIN_REPORT, END_REPORT, BEGIN_PATCHING,
  END_PATCHING, GOT_DIAGNOSTICS, BEGIN_COMPLETION, END_COMPLETION, BEGIN_STRUCTURE, END_STRUCTURE, BEGIN_DIAGNOSTIC,
  END_DIAGNOSTIC, CHANGE_DOCUMENT, BEGIN_AMF_INIT, END_AMF_INIT = Value
}

trait TelemetryProvider {

  def addTimedMessage(code: String, messageType: MessageTypes, msg: String, uri: String, uuid: String): Unit

  private def extractNames(className: String): (String, Option[String]) = {
    val pattern = """.*\.(.*)\$\$anonfun\$([^\$]*).*""".r
    pattern.findAllIn(className).matchData match {
      case md if md.hasNext => {
        val tmp = md.next()
        (tmp.group(1), Some(tmp.group(2)))
      }
      case _ => (className, None)
    }
  }

  def addTimedMessage(msg: String, messageType: MessageTypes, uri: String, uuid: String): Unit = {
    val element: StackTraceElement = Thread.currentThread().getStackTrace.toList(2)
    extractNames(element.getClassName) match {
      case (c, Some(m)) => addTimedMessage(s"$c : $m", messageType, msg, uri, uuid)
      case _ =>
        addTimedMessage(s"${element.getClassName.split('.').last} : ${element.getMethodName}",
                        messageType,
                        msg,
                        uri,
                        uuid)
    }
  }
}