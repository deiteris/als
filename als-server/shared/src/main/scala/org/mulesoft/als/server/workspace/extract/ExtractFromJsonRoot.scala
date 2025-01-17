package org.mulesoft.als.server.workspace.extract

import amf.core.remote.Platform
import amf.internal.environment.Environment
import org.mulesoft.als.common.URIImplicits._
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.common.io.SyncFile
import org.yaml.model.{YDocument, YMap, YMapEntry}
import org.yaml.parser.JsonParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExtractFromJsonRoot(content: String) {

  val rootMap: Iterable[YMapEntry] = {
    JsonParser(content).parse(false).headOption match {
      case Some(d: YDocument) =>
        d.node.value match {
          case y: YMap => y.entries
          case _       => Nil
        }
      case _ => Nil
    }
  }

  lazy val getMain: Option[String] =
    rootMap.find(_.key.asScalar.exists(_.text == "main")).flatMap(_.value.asScalar.map(_.text))
}

object ExchangeConfigReader extends ConfigReader {
  override val configFileName: String = "exchange.json"

  override protected def buildConfig(content: String,
                                     path: String,
                                     platform: Platform,
                                     environment: Environment,
                                     logger: Logger): Option[Future[WorkspaceConfig]] =
    new ExtractFromJsonRoot(content).getMain.map { m =>
      val encodedUri = platform.encodeURI(m)
      try {
        logger.debug(s"path: $path", "ExtractFromJsonRoot", "buildConfig")
        logger.debug(s"encodedUri: $encodedUri", "ExtractFromJsonRoot", "buildConfig")
        // todo: change platform.fs file reads for resolve
        getSubList(platform.fs.syncFile(path), platform, environment, logger).map { dependencies =>
          // How to know if a config file already encoded the main file?
          // the encoding should be handled by each config reader plugin? or in general?
          logger.debug(s"dependencies: ${dependencies.fold("")((a, b) => s"$a\n$b")}",
                       "ExtractFromJsonRoot",
                       "buildConfig")
          WorkspaceConfig(path, encodedUri, dependencies, Some(this))
        }
      } catch {
        case e: Exception =>
          logger.error(Option(e.getMessage).getOrElse("Error while reading dependencies"),
                       "ExtractFromJsonRoot",
                       "buildConfig")
          Future.successful(WorkspaceConfig(path, encodedUri, Set.empty, Some(this)))
      }
    }

  private def getSubList(dir: SyncFile,
                         platform: Platform,
                         environment: Environment,
                         logger: Logger): Future[Set[String]] =
    if (dir.list != null && dir.list.nonEmpty)
      findDependencies(
        dir.list.map(l => platform.fs.syncFile(s"${dir.path}${platform.fs.separatorChar}$l")).filter(_.isDirectory),
        platform,
        environment,
        logger)
    else
      Future.successful(Set.empty)

  private def findDependencies(subDirs: Array[SyncFile],
                               platform: Platform,
                               environment: Environment,
                               logger: Logger): Future[Set[String]] =
    if (subDirs.nonEmpty) {
      val (dependencies, others) = subDirs.partition(_.list.exists(_.contains(configFileName)))
      val mains: Future[Seq[String]] =
        Future.sequence {
          dependencies.toSeq.map(
            d =>
              readFile(s"${d.path}${platform.fs.separatorChar}$configFileName".toAmfUri(platform),
                       platform,
                       environment,
                       logger)
                .map(_.flatMap { c =>
                  new ExtractFromJsonRoot(c).getMain.map(m => s"${d.path}${platform.fs.separatorChar}$m")
                })) map (_.collect { case Some(c) => c })
        }

      mains.flatMap(
        innerMains =>
          findDependencies(
            others
              .flatMap(o => o.list.map(so => platform.fs.syncFile(s"${o.path}${platform.fs.separatorChar}$so")))
              .filter(_.isDirectory),
            platform,
            environment,
            logger
          ).map(_ ++ innerMains.toSet))
    } else Future.successful(Set.empty)
}
