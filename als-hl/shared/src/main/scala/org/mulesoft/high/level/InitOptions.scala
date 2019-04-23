package org.mulesoft.high.level

import amf.{Oas20Profile, ProfileName, Raml08Profile, Raml10Profile}

import scala.collection.mutable.ListBuffer
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class InitOptions(val vendors: Set[ProfileName], val customDialects: Seq[CustomDialects] = Seq()) {
  def filterClone(initialized: Set[ProfileName]) =
    new InitOptions(vendors.filter(!initialized.contains(_)),
                    customDialects.filter(c => !initialized.contains(c.name)))

  def contains(profile: ProfileName): Boolean = vendors.contains(profile)

  def withCustomDialects(dialects: Seq[CustomDialects]) = new InitOptions(vendors, dialects)

}
// todo: vendors instead of profiles?

@JSExportAll
object InitOptions {
  val AllProfiles: InitOptions = new InitOptions(Set(Raml10Profile, Raml08Profile, Oas20Profile, AsyncAPIProfile))

  val WebApiProfiles: InitOptions = new InitOptions(Set(Raml10Profile, Raml08Profile, Oas20Profile))

  val RamlProfiles: InitOptions = new InitOptions(Set(Raml10Profile, Raml08Profile))

  val OasProfile: InitOptions = new InitOptions(Set(Oas20Profile))

  val AsyncProfile: InitOptions = new InitOptions(Set(AsyncAPIProfile))
}

@JSExportAll
object AsyncAPIProfile extends ProfileName("AsyncAPI")
