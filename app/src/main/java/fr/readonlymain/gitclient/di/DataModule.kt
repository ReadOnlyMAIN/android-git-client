package fr.readonlymain.gitclient.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.readonlymain.gitclient.data.preferences.RepositoriesPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideRepositoriesPreferences(@ApplicationContext context: Context): RepositoriesPreferences {
        return RepositoriesPreferences(context)
    }
}
