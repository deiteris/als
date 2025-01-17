package org.mulesoft.als.server.workspace

import amf.core.remote.Platform
import org.mulesoft.als.common.URIImplicits._
import org.mulesoft.als.configuration.{DefaultProjectConfigurationStyle, ProjectConfigurationStyle}
import org.mulesoft.als.server.AlsWorkspaceService
import org.mulesoft.als.server.logger.Logger
import org.mulesoft.als.server.modules.ast._
import org.mulesoft.als.server.modules.workspace.{CompilableUnit, WorkspaceContentManager}
import org.mulesoft.als.server.textsync.EnvironmentProvider
import org.mulesoft.als.server.workspace.command._
import org.mulesoft.als.server.workspace.extract._
import org.mulesoft.amfintegration.relationships.{AliasInfo, RelationshipLink}
import org.mulesoft.lsp.configuration.WorkspaceFolder
import org.mulesoft.lsp.feature.link.DocumentLink
import org.mulesoft.lsp.feature.telemetry.TelemetryProvider
import org.mulesoft.lsp.workspace.{DidChangeWorkspaceFoldersParams, ExecuteCommandParams}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WorkspaceManager(environmentProvider: EnvironmentProvider,
                       telemetryProvider: TelemetryProvider,
                       val allSubscribers: List[BaseUnitListener],
                       override val dependencies: List[AccessUnits[CompilableUnit]],
                       logger: Logger)
    extends TextListener
    with UnitWorkspaceManager
    with UnitsManager[CompilableUnit, BaseUnitListenerParams]
    with AlsWorkspaceService {
  implicit val platform: Platform                  = environmentProvider.platform
  override def subscribers: List[BaseUnitListener] = allSubscribers.filter(_.isActive)
  private val workspaces                           = new WorkspaceList(environmentProvider, telemetryProvider, allSubscribers, logger)

  def getWorkspace(uri: String): WorkspaceContentManager =
    workspaces.findWorkspace(uri)

  override def getUnit(uri: String, uuid: String): Future[CompilableUnit] =
    getWorkspace(uri.toAmfUri).getUnit(uri.toAmfUri)

  override def getLastUnit(uri: String, uuid: String): Future[CompilableUnit] =
    getUnit(uri.toAmfUri, uuid).flatMap(cu => if (cu.isDirty) getLastCU(cu, uri, uuid) else Future.successful(cu))

  private def getLastCU(cu: CompilableUnit, uri: String, uuid: String) =
    cu.getLast.flatMap {
      case newCu if newCu.isDirty => getLastUnit(uri, uuid)
      case newCu                  => Future.successful(newCu)
    }

  override def notify(uri: String, kind: NotificationKind): Unit = {
    val manager: WorkspaceContentManager = getWorkspace(uri.toAmfUri)
    if (manager.configFile
          .map(_.toAmfUri)
          .contains(uri.toAmfUri)) {
      manager.withConfiguration(ReaderWorkspaceConfigurationProvider(manager))
      manager.stage(uri.toAmfUri, CHANGE_CONFIG)
    } else manager.stage(uri.toAmfUri, kind)
  }

  def contentManagerConfiguration(manager: WorkspaceContentManager,
                                  mainSubUri: String,
                                  dependencies: Set[String],
                                  reader: Option[ConfigReader]): Unit =
    manager
      .withConfiguration(DefaultWorkspaceConfigurationProvider(manager, mainSubUri, dependencies, reader))
      .stage(mainSubUri, CHANGE_CONFIG)

  override def executeCommand(params: ExecuteCommandParams): Future[AnyRef] =
    commandExecutors.get(params.command) match {
      case Some(exe) =>
        exe.runCommand(params)
      case _ =>
        logger.error(s"Command [${params.command}] not recognized", "WorkspaceManager", "executeCommand")
        Future.successful(Unit) // future failed?
    }

  private val commandExecutors: Map[String, CommandExecutor[_, _]] = Map(
    Commands.DID_FOCUS_CHANGE_COMMAND -> new DidFocusCommandExecutor(logger, this),
    Commands.DID_CHANGE_CONFIGURATION -> new DidChangeConfigurationCommandExecutor(logger, this),
    Commands.INDEX_DIALECT            -> new IndexDialectCommandExecutor(logger, environmentProvider.amfConfiguration)
  )

  override def getProjectRootOf(uri: String): Future[Option[String]] =
    getWorkspace(uri).getRootFolderFor(uri)

  override def initialize(workspaceFolders: List[WorkspaceFolder],
                          projectConfigurationStyle: ProjectConfigurationStyle): Future[Unit] = Future {
    // Drop all old workspaces
    workspaces.reset(projectConfigurationStyle)
    val newWorkspaces = extractCleanURIs(workspaceFolders)
    dependencies.foreach(d => d.withUnitAccessor(this))
    workspaces.changeWorkspaces(newWorkspaces, List())
  }

  private def extractCleanURIs(workspaceFolders: List[WorkspaceFolder]) =
    workspaceFolders.flatMap(_.uri).sortBy(_.length).distinct

  override def didChangeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams): Unit =
    changeWorkspaceFolders(params)

  def changeWorkspaceFolders(params: DidChangeWorkspaceFoldersParams): Unit =
    workspaces.changeWorkspaces(params.event.added.flatMap(_.uri), params.event.deleted.flatMap(_.uri))

  def getWorkspaceFolders: Seq[String] = workspaces.allWorkspaces().map(_.folderUri)

  dependencies.foreach(d => d.withUnitAccessor(this))

  override def getDocumentLinks(uri: String, uuid: String): Future[Seq[DocumentLink]] =
    getLastUnit(uri.toAmfUri, uuid).flatMap(_ =>
      getWorkspace(uri.toAmfUri).getRelationships(uri.toAmfUri).getDocumentLinks(uri.toAmfUri))

  override def getAllDocumentLinks(uri: String, uuid: String): Future[Map[String, Seq[DocumentLink]]] = {
    val workspace = getWorkspace(uri)
    workspace.mainFileUri.flatMap {
      case Some(mf) =>
        getLastUnit(mf, uuid)
          .flatMap(_ => workspace.getRelationships(mf).getAllDocumentLinks)
      case _ =>
        Future.successful(Map.empty)
    }
  }

  override def getAliases(uri: String, uuid: String): Future[Seq[AliasInfo]] =
    getLastUnit(uri, uuid).flatMap(_ => getWorkspace(uri).getRelationships(uri).getAliases(uri))

  private def filterDuplicates(links: Seq[RelationshipLink]): Seq[RelationshipLink] = {
    val res = mutable.ListBuffer[RelationshipLink]()
    links.foreach { l =>
      if (!res.exists(_.relationshipIsEqual(l)))
        res += l
    }
    res
  }

  // tepm until have class for all relationships from visitors result associated to CU.
  override def getRelationships(uri: String, uuid: String): Future[(CompilableUnit, Seq[RelationshipLink])] =
    getLastUnit(uri, uuid)
      .flatMap(
        cu =>
          getWorkspace(uri)
            .getRelationships(uri)
            .getRelationships(uri)
            .map(rl => (cu, filterDuplicates(rl))))

  override def isInMainTree(uri: String): Boolean =
    workspaces.findWorkspace(uri.toAmfUri).isInMainTree(uri)

}

class WorkspaceList(environmentProvider: EnvironmentProvider,
                    telemetryProvider: TelemetryProvider,
                    val allSubscribers: List[BaseUnitListener],
                    logger: Logger) {

  def subscribers: List[BaseUnitListener] = allSubscribers.filter(_.isActive)

  private val workspaces: mutable.Set[WorkspaceContentManager] = new mutable.HashSet()

  private var projectConfigurationStyle: Option[ProjectConfigurationStyle] = None

  private def configStyle: ProjectConfigurationStyle =
    projectConfigurationStyle.getOrElse(DefaultProjectConfigurationStyle)

  private val defaultWorkspace: WorkspaceContentManager = {
    logger.debug(s"created default WorkspaceContentManager", "WorkspaceList", "buildWorkspaceAt")
    new WorkspaceContentManager("", environmentProvider, telemetryProvider, logger, subscribers, configStyle)
  }

  def addWorkspace(uri: String): Unit =
    changeWorkspaces(List(uri), List.empty)

  def removeWorkspace(uri: String): Unit =
    changeWorkspaces(List.empty, List(uri))

  // todo: should return future
  def changeWorkspaces(added: List[String], deleted: List[String]): Unit = synchronized {
    logger.debug(s"Changing workspaces, added: $added, deleted: $deleted", "WorkspaceList", "changeWorkspace")
    val newWorkspaces = added.filterNot(uri => workspaces.exists(_.folderUri == uri)).map(getOrCreateWorkspaceAt)
    val oldWorkspaces = workspaces.filter(wcm => deleted.contains(wcm.folderUri)) ++
      workspaces.collect({
        case wcm: WorkspaceContentManager if added.exists(uri => wcm.folderUri.startsWith(uri)) => wcm
      })
    oldWorkspaces.foreach(_.shutdown())

    workspaces --= oldWorkspaces
    workspaces ++= newWorkspaces
  }

  private def getOrCreateWorkspaceAt(uri: String): WorkspaceContentManager =
    workspaces
      .find(w => uri.startsWith(w.folderUri)) // if there is an existing WS containing the new one, do not create it
      .getOrElse(buildWorkspaceAt(uri))

  // todo: should return future
  private def buildWorkspaceAt(uri: String): WorkspaceContentManager = {
    // todo: instead of `new WorkspaceContentManager` there should be a `WorkspaceContentManager.init(...)` which returns a future
    val wcm =
      new WorkspaceContentManager(uri, environmentProvider, telemetryProvider, logger, subscribers, configStyle)
    val applicableFiles = environmentProvider.openedFiles.filter(_.startsWith(uri))
    applicableFiles.foreach(wcm.stage(_, OPEN_FILE))
    logger.debug(s"created WorkspaceContentManager for $uri", "WorkspaceList", "buildWorkspaceAt")
    wcm
  }

  def findWorkspace(uri: String): WorkspaceContentManager =
    workspaces.find(ws => ws.containsFile(uri)).getOrElse {
      logger.debug(s"getting default workspace", "WorkspaceList", "findWorkspace")
      defaultWorkspace
    }

  private def clear(): Unit = workspaces.foreach(w => removeWorkspace(w.folderUri))

  def reset(configuration: ProjectConfigurationStyle): Unit = {
    projectConfigurationStyle = Some(configuration)
    this.clear()
  }

  def allWorkspaces(): Seq[WorkspaceContentManager] = workspaces.toSeq
}
