package fr.readonlymain.gitclient.utils

import android.net.Uri
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUtilsTest {

    @Test
    fun resolveUriToPath_withPrimaryStorage_shouldReturnCorrectPath() {
        // Given
        // A typical Tree URI from SAF for primary storage
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload%2FTestRepo")
        val expectedBase = Environment.getExternalStorageDirectory().absolutePath
        val expectedPath = "$expectedBase/Download/TestRepo"

        // When
        val result = resolveUriToPath(uri)

        // Then
        assertThat(result).isEqualTo(expectedPath)
    }

    @Test
    fun resolveUriToPath_withSecondaryStorage_shouldReturnReconstructedPath() {
        // Given
        // A typical Tree URI from SAF for an SD card (e.g., ID is 1234-5678)
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/1234-5678%3AMyFiles")
        val expectedPath = "/storage/1234-5678/MyFiles"

        // When
        val result = resolveUriToPath(uri)

        // Then
        assertThat(result).isEqualTo(expectedPath)
    }

    @Test
    fun resolveUriToPath_withInvalidUri_shouldReturnNull() {
        // Given
        val uri = Uri.parse("https://github.com/test/repo")

        // When
        val result = resolveUriToPath(uri)

        // Then
        assertThat(result).isNull()
    }
}
