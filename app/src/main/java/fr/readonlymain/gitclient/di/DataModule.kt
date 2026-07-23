package fr.readonlymain.gitclient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.readonlymain.gitclient.data.preferences.CredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.DataStoreCredentialsPreferences
import fr.readonlymain.gitclient.data.preferences.DataStoreGitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.DataStoreRepositoriesPreferences
import fr.readonlymain.gitclient.data.preferences.DataStoreThemePreferences
import fr.readonlymain.gitclient.data.preferences.GitConfigPreferences
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import fr.readonlymain.gitclient.data.preferences.ThemePreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindRepositoriesPreferences(impl: DataStoreRepositoriesPreferences): RepositoriesPreferences

    @Binds
    @Singleton
    abstract fun bindGitConfigPreferences(impl: DataStoreGitConfigPreferences): GitConfigPreferences

    @Binds
    @Singleton
    abstract fun bindCredentialsPreferences(impl: DataStoreCredentialsPreferences): CredentialsPreferences

    @Binds
    @Singleton
    abstract fun bindThemePreferences(impl: DataStoreThemePreferences): ThemePreferences
}
