package org.mulesoft.amfmanager

import amf.core.model.document.BaseUnit

import scala.collection.mutable

object BaseUnitImplicits {
  implicit class BaseUnitImp(bu: BaseUnit) {
    def flatRefs: Seq[BaseUnit] = {
      val set: mutable.Set[BaseUnit] = mutable.Set.empty

      def innerRefs(refs: Seq[BaseUnit]): Unit =
        refs.foreach { bu =>
          if (set.add(bu)) innerRefs(bu.references)
        }

      innerRefs(bu.references)
      set.toSeq
    }

    def identifier: String = bu.location().getOrElse(bu.id)
  }

}
