package fr.readonlymain.gitclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.readonlymain.gitclient.data.repository.DefaultGitRepository
import fr.readonlymain.gitclient.data.repository.GitRepository
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGitRepository(
        jGitRepository: DefaultGitRepository
    ): GitRepository
}