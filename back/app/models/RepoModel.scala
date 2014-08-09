package models

import java.io.File

trait SourceRepositoryModel {
    def initialize: Unit
    def update(branch: String): Unit
    def getFilePath(localPath: String) =
        RepoModel.local + File.separator + localPath
    def getFile(localPath: String) = new File(getFilePath(localPath))
}

object RepoModel {
    val remote = "git@github.com:Paamayim/take2.git"
    val local = "repo"

    private val impl = GitModel

    // Forward methods to implementation
    val defaultBranch = impl.defaultBranch
    def initialize = impl.initialize
    def update = impl.update _
    def getFilePath = impl.getFilePath _
    def getFile = impl.getFile _
}

