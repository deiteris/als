package org.mulesoft.typesystem.json.interfaces

trait Point {

    def line: Int

    def column: Int

    def position: Int

    def resolved: Boolean = position >= 0
}
