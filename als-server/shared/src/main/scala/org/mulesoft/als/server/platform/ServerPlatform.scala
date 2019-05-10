package org.mulesoft.als.server.platform

import amf.client.remote.Content
import amf.core.lexer.CharSequenceStream
import amf.internal.environment.Environment
import amf.internal.resource.ResourceLoader
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.textsync.TextDocumentManager
import org.mulesoft.als.server.util.PathRefine
import org.mulesoft.high.level.implementation.{AlsPlatform, AlsPlatformWrapper}
import org.mulesoft.high.level.interfaces.DirectoryResolver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Http {
  def unapply(uri: String): Option[(String, String, String)] = uri match {
    case url if url.startsWith("http://") || url.startsWith("https://") =>
      val protocol        = url.substring(0, url.indexOf("://") + 3)
      val rightOfProtocol = url.stripPrefix(protocol)
      val host =
        if (rightOfProtocol.contains("/")) rightOfProtocol.substring(0, rightOfProtocol.indexOf("/"))
        else rightOfProtocol
      val path = rightOfProtocol.replace(host, "")
      Some(protocol, host, path)
    case _ => None
  }
}

object File {
  val FILE_PROTOCOL = "file://"

  def unapply(url: String): Option[String] = {
    url match {
      case s if s.startsWith(FILE_PROTOCOL) =>
        val path = s.stripPrefix(FILE_PROTOCOL)
        Some(path)
      case _ => None
    }
  }
}

class DefaultFileLoader(platform: ServerPlatform) extends ResourceLoader {

  override def accepts(resource: String): Boolean = {
    !(resource.startsWith("http") || resource.startsWith("HTTP"))
  }

  override def fetch(resource: String): Future[Content] = {
    platform.fetchFile(resource)
  }
}

/**
  * Platform based on connection.
  * Intended for subclassing to implement fetchHttp method
  */
class ServerPlatform(val logger: Logger,
                     val textDocumentManager: TextDocumentManager,
                     directoryResolver: Option[DirectoryResolver] = None,
                     defaultEnvironment: Environment = Environment())
    extends AlsPlatformWrapper(defaultEnvironment, dirResolver = directoryResolver) { self =>

  val fileLoader = new DefaultFileLoader(this)

  override val loaders: Seq[ResourceLoader] = Seq(fileLoader)

  override def resolvePath(uri: String): String = {
    val refineUri = PathRefine.refinePath(uri, this)

    val result = refineUri match {
      case File(path) => File.FILE_PROTOCOL + path

      case Http(protocol, host, path) => protocol + host + withTrailingSlash(path)

      case _ => File.FILE_PROTOCOL + refineUri
    }

    logger.debugDetail(s"Resolved $refineUri as $result", "ConnectionBasedPlatform", "resolvePath")

    result
  }

  def fetchFile(uri: String): Future[Content] = {
    this.logger.debugDetail("Asked to fetch file " + uri, "ConnectionBasedPlatform", "fetchFile")

    val editorOption = textDocumentManager.getTextDocument(uri)
    logger.debugDetail(s"Result of editor check for uri $uri: ${editorOption.isDefined}",
                       "ConnectionBasedPlatform",
                       "fetchFile")

    val contentFuture =
      if (editorOption.isDefined) {

        Future.successful(editorOption.get.text)
      } else {
        fs.asyncFile(uri).read()
      }

    contentFuture
      .map(content => {

        Content(new CharSequenceStream(uri, content),
                ensureFileAuthority(uri),
                extension(uri).flatMap(mimeFromExtension))
      })
  }

  private def withTrailingSlash(path: String) = {
    (if (!path.startsWith("/")) "/" else "") + path
  }

  override def withDefaultEnvironment(defaultEnvironment: Environment): AlsPlatform =
    new ServerPlatform(logger, textDocumentManager, directoryResolver, defaultEnvironment)
}