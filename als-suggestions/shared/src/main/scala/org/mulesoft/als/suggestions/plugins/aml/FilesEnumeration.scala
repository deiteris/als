package org.mulesoft.als.suggestions.plugins.aml

import amf.core.remote.Platform
import org.mulesoft.als.common.DirectoryResolver
import org.mulesoft.als.common.URIImplicits._
import org.mulesoft.als.suggestions.RawSuggestion

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class FilesEnumeration(directoryResolver: DirectoryResolver,
                            override implicit val platform: Platform,
                            actual: String,
                            relativePath: String)
    extends PathCompletion {

  def filesIn(fullURI: String): Future[Seq[RawSuggestion]] =
    directoryResolver.isDirectory(platform.resolvePath(fullURI)).flatMap { isDir =>
      if (isDir) listDirectory(fullURI)
      else Future.successful(Nil)
    }

  private def listDirectory(fullURI: String): Future[Seq[RawSuggestion]] =
    directoryResolver
      .readDir(platform.resolvePath(fullURI))
      .flatMap(withIsDir(_, fullURI))
      .map(s => {
        s.filter(tuple => s"${fullURI.toPath}${tuple._1}" != actual && (tuple._2 || supportedExtension(tuple._1)))
          .map(t => if (t._2) s"${t._1}/" else t._1)
          .map(toRawSuggestion)
      })

  private def withIsDir(files: Seq[String], fullUri: String): Future[Seq[(String, Boolean)]] =
    Future.sequence {
      files.map(
        file =>
          directoryResolver
            .isDirectory(platform.resolvePath(s"${fullUri.toPath}$file".toAmfUri))
            .map(isDir => (file, isDir)))
    }

  private def toRawSuggestion(file: String) =
    RawSuggestion(s"$relativePath$file", s"$relativePath$file", "Path suggestion", Nil)
}
