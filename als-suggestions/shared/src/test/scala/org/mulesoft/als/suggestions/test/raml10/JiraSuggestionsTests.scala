package org.mulesoft.als.suggestions.test.raml10

class JiraSuggestionsTests extends RAML10Test {
  test("ALS-707 Single") {
    this.runTest("jira-tests/als-707/als-707a.raml",
                 Set("application/json", "application/xml", "application/x-www-form-urlencoded"))
  }

  test("ALS-707 Sequence") {
    this.runTest("jira-tests/als-707/als-707b.raml", Set("application/json", "application/x-www-form-urlencoded"))
  }

  test("ALS-707 Open Sequence") {
    this.runTest("jira-tests/als-707/als-707c.raml",
                 Set("application/json", "application/x-www-form-urlencoded", "application/xml"))
  }

  test("ALS-719 Library Suggestions") {
    this.runTest("jira-tests/als-719/test01.raml", Set("library01.raml"))
  }

  test("ALS-718 Library Suggestions") {
    this.runTest("jira-tests/als-718/api.raml", Set("Employee", "Person", "Manager"))
  }

  ignore("ALS-721 Multiple Library Suggestions 01") {
    this.runTest("jira-tests/als-721/test01.raml", Set("Employee", "Person", "Manager"))
  }

  ignore("ALS-721 Multiple Library Suggestions 02") {
    this.runTest("jira-tests/als-721/test02.raml", Set("Employee2", "Person2", "Manager2"))
  }
}
