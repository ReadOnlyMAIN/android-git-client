package fr.readonlymain.gitclient.data.preferences

import fr.readonlymain.gitclient.data.model.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeRepositoriesPreferences : RepositoriesPreferences {
    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    override val repositoriesFlow: Flow<List<Repository>> = _repositories.asStateFlow()

    private val _selectedRepo = MutableStateFlow<String?>(null)
    override val selectedRepoFlow: Flow<String?> = _selectedRepo.asStateFlow()

    private val _selectedBranch = MutableStateFlow<String?>(null)
    override val selectedBranchFlow: Flow<String?> = _selectedBranch.asStateFlow()

    override suspend fun addRepository(repository: Repository) {
        _repositories.update { it + repository }
    }

    override suspend fun updateRepository(repository: Repository) {
        _repositories.update { list ->
            list.map { if (it.id == repository.id) repository else it }
        }
    }

    override suspend fun deleteRepository(id: String) {
        _repositories.update { list -> list.filter { it.id != id } }
    }

    override suspend fun saveSelectedRepo(path: String) {
        _selectedRepo.value = path
    }

    override suspend fun saveSelectedBranch(branchName: String) {
        _selectedBranch.value = branchName
    }

    override suspend fun resetSelectedBranch() {
        _selectedBranch.value = null
    }
}
