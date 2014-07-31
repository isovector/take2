package models

import java.io.File

object RepoModel {
    val remote = "git@github.com:Paamayim/take2.git"
    val local = "repo"

    def getFile(localPath: String) = new File(RepoModel.local + File.separator + localPath)
}

