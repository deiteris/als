package org.mulesoft.als.suggestions

import org.mulesoft.als.suggestions.implementation.LocationKindDetectTool
import org.mulesoft.als.suggestions.interfaces.LocationKind.{
  ANNOTATION_COMPLETION,
  KEY_COMPLETION,
  SEQUENCE_KEY_COPLETION
}

object ContentPatcher {

  def prepareYamlContent(text: String, offset: Int): String = {
    val completionKind =
      LocationKindDetectTool.determineCompletionKind(text, offset)
    val result = completionKind match {
      case KEY_COMPLETION | ANNOTATION_COMPLETION | SEQUENCE_KEY_COPLETION => {
        val newLineIndex = text.indexOf("\n", offset)
        val rightPart =
          if (newLineIndex < 0) text.substring(offset)
          else text.substring(offset, newLineIndex)
        val colonIndex = rightPart.indexOf(":")
        if (colonIndex < 0)
          text.substring(0, offset) + "k: " + text.substring(offset)
        else if (colonIndex == 0) {
          val leftPart = text.substring(0, offset)
          val leftOfSentence =
            leftPart.substring(Math.max(leftPart.lastIndexOf('\n'), 0), offset)
          val rightPart = text.substring(offset)
          val rightOfSentence =
            rightPart.substring(0, Math.min(Math.max(rightPart.indexOf('\n'), 0), rightPart.length))

          val openBrackets = { leftOfSentence + rightOfSentence }
            .count(_ == '[') - {
            leftOfSentence + rightOfSentence
          }.count(_ == '[')
          text + "k" + " ]" * openBrackets + rightPart
        } else text
      }
      case _ =>
        if (offset == text.length) text + "\n"
        else text
    }
    result
  }

  def prepareJsonContent(textRaw: String, offsetRaw: Int): String = {
    val EOL       = textRaw.find(_ == '\r').map(_ => "\r\n").getOrElse("\n")
    val text      = textRaw.replace(EOL, "\n")
    val offset    = offsetRaw - textRaw.substring(0, offsetRaw).count(_ == '\r')
    val lineStart = 0.max(text.lastIndexOf("\n", 0.max(offset - 1)) + 1)

    var lineEnd = text.indexOf("\n", offset)
    if (lineEnd < 0) lineEnd = text.length
    val line       = text.substring(lineStart, lineEnd)
    val off        = offset - lineStart
    val lineTrim   = line.trim
    val textEnding = text.substring(lineEnd + 1).trim
    val hasComplexValueStartSameLine = lineTrim.endsWith("{") || lineTrim
      .endsWith("[")
    val hasComplexValueSameLine = hasComplexValueStartSameLine || lineTrim
      .endsWith("}") || lineTrim.endsWith("]")
    val hasComplexValueStartNextLine = !lineTrim.endsWith(",") && (textEnding
      .startsWith("{") || textEnding.startsWith("["))
    val hasComplexValueNextLine = !lineTrim.endsWith(",") & (hasComplexValueStartNextLine || textEnding
      .startsWith("}") || textEnding
      .startsWith("]"))
    val hasComplexValueStart = hasComplexValueStartNextLine || hasComplexValueStartSameLine
    var needComa =
      !(lineTrim.endsWith(",") || hasComplexValueNextLine || hasComplexValueSameLine)
    if (needComa) {
      val textEnding = text.substring(lineEnd).trim
      needComa = textEnding.nonEmpty && !(textEnding.startsWith(",") || textEnding
        .startsWith("{") || textEnding
        .startsWith("}") || textEnding.startsWith("[") || textEnding.startsWith("]"))
    }
    var colonIndex = line.indexOf(":")
    var newLine    = line
    if (colonIndex < 0) {
      if (lineTrim.startsWith("\"")) {
        newLine = line.substring(0, off) + "x\" : "
        if (!hasComplexValueStart)
          newLine += "\"\""
        if (!(hasComplexValueSameLine || hasComplexValueNextLine))
          newLine += ","
      }
    } else if (colonIndex <= off) {
      colonIndex = line.lastIndexOf(":", off)
      var substr               = line.substring(colonIndex + 1).trim
      val hasOpenCurlyBracket  = substr.startsWith("{")
      val hasOpenSquareBracket = substr.startsWith("[")
      newLine = line.substring(0, off)
      if (hasOpenCurlyBracket || hasOpenSquareBracket)
        substr = substr.substring(1)
      var hasOpenValueQuote = substr.startsWith("\"")
      if (!hasOpenValueQuote && !(hasOpenCurlyBracket || hasOpenSquareBracket)) {
        newLine += "\""
        hasOpenValueQuote = true
      }
      if (hasOpenValueQuote)
        newLine += "\""
      if (hasComplexValueSameLine)
        newLine += lineTrim.charAt(lineTrim.length - 1)
      if (lineTrim.endsWith(","))
        newLine += ","
    } else {
      if (line.substring(colonIndex + 1).trim.startsWith("\"")) {
        val openQuoteInd = line.indexOf("\"", colonIndex)
        if (off > openQuoteInd)
          if (!lineTrim.endsWith("\""))
            newLine += "\""
      }
      if (needComa)
        newLine += ","
    }
    val result = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
    result.replace("\n", EOL)
  }
}