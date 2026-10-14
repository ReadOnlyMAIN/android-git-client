package fr.readonlymain.gitclient.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.readonlymain.gitclient.data.model.GitCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages the persistent storage and retrieval of [GitCredential] objects using Android DataStore
 * with AES-GCM encryption via Android Keystore.
 */
class DataStoreCredentialsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : CredentialsPreferences {
    companion object {
        private val CREDENTIALS_KEY = stringPreferencesKey("git_credentials_encrypted")
        private const val KEY_ALIAS = "git_credentials_key"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    }

    override val credentialsFlow: Flow<List<GitCredential>> = context.dataStore.data
        .map { preferences ->
            val encryptedJson = preferences[CREDENTIALS_KEY] ?: return@map emptyList()
            val json = decrypt(encryptedJson)
            try {
                Json.decodeFromString<List<GitCredential>>(json)
            } catch (e: Exception) {
                throw CredentialStorageException("Cannot decode saved credentials", e)
            }
        }

    override suspend fun addCredential(credential: GitCredential) {
        val currentList = credentialsFlow.first()
        val newList = currentList + credential
        val encryptedJson = encrypt(Json.encodeToString(newList))

        context.dataStore.edit { preferences ->
            preferences[CREDENTIALS_KEY] = encryptedJson
        }
    }

    override suspend fun deleteCredential(id: String) {
        val currentList = credentialsFlow.first()
        val newList = currentList.filter { it.id != id }
        val encryptedJson = encrypt(Json.encodeToString(newList))

        context.dataStore.edit { preferences ->
            preferences[CREDENTIALS_KEY] = encryptedJson
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            //.setIsStrongBoxBacked(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw CredentialStorageException("Cannot encrypt credentials", e)
        }
    }

    private fun decrypt(encryptedData: String): String {
        return try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            require(combined.size >= 28) { "Invalid encrypted credential payload" }

            val iv = combined.sliceArray(0 until 12)
            val encrypted = combined.sliceArray(12 until combined.size)

            val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw CredentialStorageException("Cannot decrypt credentials; the encryption key may be unavailable", e)
        }
    }
}
