package fr.readonlymain.gitclient.data.preferences

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/** Isolated cryptography boundary; a missing decryption key must never be silently replaced. */
class AndroidKeystoreCredentialCipher internal constructor(private val alias: String) {
    @Inject constructor() : this("git_credentials_key")

    companion object {
        private val keyLock = Any()
        private const val STORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private fun key(create: Boolean): SecretKey = synchronized(keyLock) {
        val store = KeyStore.getInstance(STORE).apply { load(null) }
        store.getKey(alias, null)?.let { return@synchronized it as SecretKey }
        check(create) { "Credential encryption key is unavailable" }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE)
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        generator.generateKey()
    }

    fun encrypt(plaintext: String): String = guarded("Cannot encrypt credentials") {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
        Base64.encodeToString(cipher.iv + cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    fun decrypt(payload: String): String = guarded("Cannot decrypt credentials; the encryption key may be unavailable") {
        val bytes = Base64.decode(payload, Base64.DEFAULT)
        require(bytes.size >= 28) { "Invalid encrypted credential payload" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(create = false), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
    }

    private fun guarded(message: String, operation: () -> String): String = try {
        operation()
    } catch (e: Exception) {
        throw CredentialStorageException(message, e)
    }
}
