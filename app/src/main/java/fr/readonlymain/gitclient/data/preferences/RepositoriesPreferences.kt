package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.Repository
import kotlinx.coroutines.flow.Flow

interface RepositoriesPreferences {
    val repositoriesFlow: Flow<List<Repository>>
    val selectedRepoFlow: Flow<String?>
    val selectedBranchFlow: Flow<String?>

    suspend fun addRepository(repository: Repository)
    suspend fun updateRepository(repository: Repository)
    suspend fun deleteRepository(id: String)
    suspend fun saveSelectedRepo(path: String)
    suspend fun saveSelectedBranch(branchName: String)
    suspend fun resetSelectedRepo()
    suspend fun resetSelectedBranch()
}