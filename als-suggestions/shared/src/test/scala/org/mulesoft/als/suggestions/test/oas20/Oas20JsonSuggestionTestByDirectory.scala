package org.mulesoft.als.suggestions.test.oas20

import amf.core.remote.{Hint, OasJsonHint}
import org.mulesoft.als.suggestions.test.SuggestionByDirectoryTest

class Oas20JsonSuggestionTestByDirectory extends SuggestionByDirectoryTest {
  override def basePath: String = "als-suggestions/shared/src/test/resources/test/oas20/by-directory/json"

  override def origin: Hint = OasJsonHint

  override def fileExtensions: Seq[String] = Seq(".json")
}
