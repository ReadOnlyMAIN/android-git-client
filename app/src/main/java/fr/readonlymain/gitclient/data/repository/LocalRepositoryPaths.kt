package fr.readonlymain.gitclient.data.repository

import java.io.File

internal fun requireLocalDirectory(path: String, writable: Boolean = false): File {
    require(path.isNotBlank() && File(path).isAbsolute && !path.contains("://")) {
        "Select a supported local filesystem directory; document and cloud providers are not supported"
    }
    val directory = File(path).canonicalFile
    require(directory.isDirectory && directory.canRead()) { "Local directory is unavailable or unreadable" }
    if (writable) require(directory.canWrite()) { "Local directory is not writable" }
    return directory
}
