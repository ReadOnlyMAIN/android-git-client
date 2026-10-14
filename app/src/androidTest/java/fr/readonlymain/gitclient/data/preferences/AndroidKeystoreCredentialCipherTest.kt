package fr.readonlymain.gitclient.data.preferences

import android.util.Base64
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.KeyStore
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreCredentialCipherTest {
    private lateinit var alias: String
    private lateinit var root: File
    private val scopes = mutableListOf<CoroutineScope>()
    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    @Before fun setup() {
        alias = "agit-validation-${UUID.randomUUID()}"
        root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, alias).apply { mkdirs() }
    }

    @After fun cleanup() = runBlocking {
        scopes.forEach { scope -> scope.cancel(); scope.coroutineContext.job.join() }
        keyStore().deleteEntry(alias)
        root.deleteRecursively()
        Unit
    }

    @Test fun encryption_roundtrip_uses_fresh_IV_and_survives_new_cipher_instance() {
        val cipher = AndroidKeystoreCredentialCipher(alias)
        val first = cipher.encrypt("secret-token")
        val second = cipher.encrypt("secret-token")
        assertThat(first).isNotEqualTo(second)
        assertThat(first).doesNotContain("secret-token")
        assertThat(AndroidKeystoreCredentialCipher(alias).decrypt(first)).isEqualTo("secret-token")
    }

    @Test fun tampered_ciphertext_is_rejected() {
        val cipher = AndroidKeystoreCredentialCipher(alias)
        val bytes = Base64.decode(cipher.encrypt("secret"), Base64.DEFAULT)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        val failure = runCatching { cipher.decrypt(Base64.encodeToString(bytes, Base64.NO_WRAP)) }.exceptionOrNull()
        assertThat(failure).isInstanceOf(CredentialStorageException::class.java)
    }

    @Test fun missing_key_is_reported_without_creating_a_replacement() {
        val cipher = AndroidKeystoreCredentialCipher(alias)
        val payload = cipher.encrypt("secret")
        keyStore().deleteEntry(alias)
        assertThat(runCatching { cipher.decrypt(payload) }.exceptionOrNull()).isInstanceOf(CredentialStorageException::class.java)
        assertThat(keyStore().containsAlias(alias)).isFalse()
    }

    @Test fun malformed_payload_is_rejected() {
        val cipher = AndroidKeystoreCredentialCipher(alias)
        listOf("", "not-base64", Base64.encodeToString(ByteArray(12), Base64.NO_WRAP)).forEach {
            assertThat(runCatching { cipher.decrypt(it) }.exceptionOrNull()).isInstanceOf(CredentialStorageException::class.java)
        }
    }

    @Test fun encrypted_DataStore_survives_closing_and_reopening(): Unit = runBlocking {
        fun persistence(): Pair<CredentialPersistence, CoroutineScope> {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO).also { scopes += it }
            val store = PreferenceDataStoreFactory.create(scope = scope) { File(root, "credentials.preferences_pb") }
            val cipher = AndroidKeystoreCredentialCipher(alias)
            return CredentialPersistence(store, cipher::encrypt, cipher::decrypt) to scope
        }
        val credential = GitCredential(accountName = "test", username = "test", token = "persisted-secret")
        val (first, scope) = persistence()
        first.update { listOf(credential) }
        scope.cancel(); scope.coroutineContext.job.join()
        assertThat(File(root, "credentials.preferences_pb").readBytes().toString(Charsets.UTF_8)).doesNotContain(credential.token)
        val (second) = persistence()
        assertThat(second.credentials.first()).containsExactly(credential)
    }

    @Test fun concurrent_real_encrypted_DataStore_updates_do_not_lose_credentials(): Unit = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO).also { scopes += it }
        val store = PreferenceDataStoreFactory.create(scope = scope) { File(root, "concurrent.preferences_pb") }
        val cipher = AndroidKeystoreCredentialCipher(alias)
        val persistence = CredentialPersistence(store, cipher::encrypt, cipher::decrypt)
        val jobs = (1..30).map { index -> launch(Dispatchers.IO) {
            persistence.update { it + GitCredential(accountName = "account-$index", username = "test", token = "secret-$index") }
        } }
        jobs.forEach { it.join() }
        assertThat(persistence.credentials.first()).hasSize(30)
    }
}
