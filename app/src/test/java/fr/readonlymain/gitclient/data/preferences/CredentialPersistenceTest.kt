package fr.readonlymain.gitclient.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CredentialPersistenceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun `concurrent additions do not lose updates`() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            temporary.root.resolve("settings.preferences_pb")
        }
        val persistence = CredentialPersistence(store, { "encrypted:$it" }, { it.removePrefix("encrypted:") })
        val jobs = (1..30).map { index -> launch {
            persistence.update { it + GitCredential(accountName = "account$index", username = "user", token = "secret") }
        } }
        jobs.forEach { it.join() }
        assertThat(persistence.credentials.first()).hasSize(30)
        assertThat(persistence.credentials.first().map { it.accountName }.toSet()).hasSize(30)
    }

    @Test fun `failed encryption leaves original payload untouched`() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            temporary.root.resolve("settings.preferences_pb")
        }
        var fail = false
        val persistence = CredentialPersistence(store, {
            if (fail) throw CredentialStorageException("key unavailable")
            "encrypted:$it"
        }, { it.removePrefix("encrypted:") })
        persistence.update { listOf(GitCredential(accountName = "saved", username = "user", token = "secret")) }
        val before = store.data.first()
        fail = true
        val failure = runCatching { persistence.update { emptyList() } }.exceptionOrNull()
        assertThat(failure).isInstanceOf(CredentialStorageException::class.java)
        assertThat(store.data.first()).isEqualTo(before)
    }

    @Test fun `failed decryption does not replace unreadable credentials`() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            temporary.root.resolve("settings.preferences_pb")
        }
        val key = stringPreferencesKey("git_credentials_encrypted")
        store.edit { it[key] = "unreadable-ciphertext" }
        val persistence = CredentialPersistence(store, { error("must not encrypt") }, {
            throw CredentialStorageException("key lost")
        })
        assertThat(runCatching { persistence.credentials.first() }.exceptionOrNull())
            .isInstanceOf(CredentialStorageException::class.java)
        assertThat(runCatching { persistence.update { emptyList() } }.exceptionOrNull())
            .isInstanceOf(CredentialStorageException::class.java)
        assertThat(store.data.first()[key]).isEqualTo("unreadable-ciphertext")
    }

    @Test fun `malformed credential JSON is not an empty collection`() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            temporary.root.resolve("settings.preferences_pb")
        }
        store.edit { it[stringPreferencesKey("git_credentials_encrypted")] = "invalid JSON" }
        val persistence = CredentialPersistence(store, { it }, { it })
        assertThat(runCatching { persistence.update { emptyList() } }.exceptionOrNull())
            .isInstanceOf(CredentialStorageException::class.java)
        assertThat(store.data.first()[stringPreferencesKey("git_credentials_encrypted")]).isEqualTo("invalid JSON")
    }
}
