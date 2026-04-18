package fr.readonlymain.gitclient.utils

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract

/**
 * Resolves a given content [Uri] to its absolute physical file path on the device.
 *
 * This function specifically handles Tree URIs from the Storage Access Framework (SAF),
 * parsing the document ID to reconstruct the path for both primary internal storage
 * and secondary storage (e.g., SD cards).
 *
 * @param context The [android.content.Context] used for operations (currently unused but preserved for API consistency).
 * @param uri The [Uri] to resolve.
 * @return The absolute filesystem path as a [String], or `null` if the URI is not a
 * valid Tree URI or cannot be resolved.
 */
fun resolveUriToPath(uri: Uri): String? {
    try {
        if (DocumentsContract.isTreeUri(uri)) {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]
            val relativePath = if (split.size > 1) split[1] else ""

            return if ("primary".equals(type, ignoreCase = true)) {
                Environment.getExternalStorageDirectory().absolutePath + "/" + relativePath
            } else {
                // For SD cards, we try to rebuild the path
                "/storage/$type/$relativePath"
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
