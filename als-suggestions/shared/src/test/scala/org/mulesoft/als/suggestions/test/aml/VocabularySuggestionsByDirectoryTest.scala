package org.mulesoft.als.suggestions.test.aml

import amf.core.remote.{Hint, VocabularyYamlHint}
import org.mulesoft.als.suggestions.test.SuggestionByDirectoryTest

class VocabularySuggestionsByDirectoryTest extends SuggestionByDirectoryTest {
  override def basePath: String = "als-suggestions/shared/src/test/resources/test/AML/vocabulary"

  override def origin: Hint = VocabularyYamlHint

  override def fileExtensions: Seq[String] = Seq(".yaml")
}