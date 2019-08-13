package org.mulesoft.als.common

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject}
import amf.core.parser.FieldEntry
import org.mulesoft.als.common.dtoTypes.{Position, PositionRange}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object AmfSonElementFinder {

  implicit class AlsAmfObject(obj: AmfObject) {

    private def minor(left: FieldEntry, right: FieldEntry): FieldEntry = {
      right.value.value
        .position()
        .orElse(right.value.annotations.find(classOf[LexicalInformation])) match {
        case Some(LexicalInformation(rightRange)) =>
          left.value.value
            .position()
            .orElse(left.value.annotations.find(classOf[LexicalInformation])) match {
            case Some(LexicalInformation(leftRange)) =>
              if (leftRange.contains(rightRange)) right
              else left
            case _ => right
          }
        case None => left
      }
    }

    private def findMinor(fields: Seq[FieldEntry]): Option[FieldEntry] = {
      fields match {
        case Nil          => None
        case head :: Nil  => Some(head)
        case head :: tail => findMinor(tail)
      }
    }

    private def positionFinderFN(amfPosition: Position)(): FieldEntry => Boolean = (f: FieldEntry) => {
      f.value.value match {
        case arr: AmfArray =>
          arr
            .position()
            .map(
              p =>
                p.contains(amfPosition) && f.value.annotations
                  .find(classOf[LexicalInformation])
                  .forall(_.containsCompletely(amfPosition)))
            .getOrElse(
              arrayContainsPosition(arr,
                                    amfPosition,
                                    f.value.annotations
                                      .find(classOf[LexicalInformation])))

        case v =>
          v.position() match {
            case Some(p) =>
              p.contains(amfPosition) && f.value.value.annotations
                .find(classOf[LexicalInformation])
                .forall(_.containsCompletely(amfPosition))

            case _ => f.value.annotations.contains(classOf[SynthesizedField])
          }
      }
    }

    def findSon(amfPosition: Position, filterFns: Seq[FieldEntry => Boolean]): AmfObject =
      findSonWithStack(amfPosition, filterFns)._1

    def findSonWithStack(amfPosition: Position, filterFns: Seq[FieldEntry => Boolean]): (AmfObject, Seq[AmfObject]) = {
      val posFilter = positionFinderFN(amfPosition)
      def innerNode(amfObject: AmfObject): Option[FieldEntry] =
        amfObject.fields
          .fields()
          .filter(f => {
            filterFns.forall(fn => fn(f)) && posFilter(f)
          }) match {
          case Nil  => None
          case list => findMinor(list.toSeq)
//          case head :: tail => findMinor(tail).orElse(Some(head))
        }

      var a: Iterable[AmfObject]        = None // todo: recursive instead of tail recursive?
      val stack: ArrayBuffer[AmfObject] = ArrayBuffer()
      var result                        = obj
      do {
        a = innerNode(result).flatMap(entry =>
          entry.value.value match {
            case e: AmfArray =>
              e.findSon(amfPosition, filterFns: Seq[FieldEntry => Boolean])
                .flatMap {
                  case o: AmfObject
                      if entry.value.annotations
                        .find(classOf[LexicalInformation])
                        .forall(_.containsCompletely(amfPosition)) =>
                    Some(o)
                  case _ => None
                }
            case e: AmfObject => Some(e)
            case _            => None
        })
        a.headOption.foreach(head => {
          stack.prepend(result)
          result = head
        })
      } while (a.nonEmpty)
      (result, stack)
    }
  }

  implicit class AlsAmfArray(array: AmfArray) {
    private def minor(left: AmfElement, right: AmfElement) = {
      right
        .position() match {
        case Some(LexicalInformation(rightRange)) =>
          left.position() match {
            case Some(LexicalInformation(leftRange)) =>
              if (leftRange.contains(rightRange)) right
              else left
            case _ => right
          }
        case None => left
      }
    }

    private def findMinor(elements: List[AmfElement]): Option[AmfElement] = {
      elements match {
        case Nil         => None
        case head :: Nil => Some(head)
        case list =>
          val m = minor(list.head, list.tail.head)
          findMinor(m +: list.tail.tail)
      }
    }

    def findSon(amfPosition: Position, filterFns: Seq[FieldEntry => Boolean]): Option[AmfElement] = {
      val sons: Seq[AmfElement] = array.values.filter(v =>
        v.position() match {
          case Some(p) => p.contains(amfPosition)
          case _       => false
      })
      findMinor(sons.toList)
    }
  }

  implicit class AlsAmfElement(element: AmfElement) {

    def findSon(position: Position, filterFns: Seq[FieldEntry => Boolean]): Option[AmfElement] = { // todo: recursive with cycle control?
      element match {
        case obj: AmfObject  => Some(obj.findSon(position, filterFns))
        case array: AmfArray => array.findSon(position, filterFns)
        case _               => None
      }
    }
  }

  implicit class AlsLexicalInformation(li: LexicalInformation) {

    def contains(pos: Position): Boolean =
      Range(li.range.start.line, li.range.end.line + 1)
        .contains(pos.line) && !isLastLine(pos)

    def isLastLine(pos: Position): Boolean =
      li.range.end.column == 0 && pos.line == li.range.end.line

    def containsCompletely(pos: Position): Boolean =
      PositionRange(Position(li.range.start.line, li.range.start.column),
                    Position(li.range.end.line, li.range.end.column))
        .contains(pos) && !isLastLine(pos)
  }

  private def arrayContainsPosition(amfArray: AmfArray,
                                    amfPosition: Position,
                                    fieldLi: Option[LexicalInformation]): Boolean =
    amfArray.values.exists(_.position() match {
      case Some(p) =>
        p.contains(amfPosition) && fieldLi.forall(_.containsCompletely(amfPosition))
      case _ => false
    })
}
