package fr.readonlymain.gitclient.data.repository

import java.io.File
import java.nio.file.Files

internal fun checkedRepositoryFile(repoPath: String, relativePath: String): File {
    require(relativePath.isNotBlank() && !File(relativePath).isAbsolute) { "Expected a relative file path" }
    val root = File(repoPath).canonicalFile.toPath()
    val path = root.resolve(relativePath).normalize()
    require(path.startsWith(root) && path != root && !path.startsWith(root.resolve(".git"))) {
        "Path is outside the repository working tree"
    }
    var current = path
    while (current != root) {
        require(!Files.isSymbolicLink(current)) { "Symbolic links are not supported for this operation" }
        current = current.parent
    }
    require(path.toFile().canonicalFile.toPath().startsWith(root)) { "Path escapes repository" }
    return path.toFile()
}

/** Never follows symbolic links, including links introduced during a failed clone. */
internal fun deleteOwnedCloneDirectory(directory: File) {
    Files.walk(directory.toPath()).use { paths ->
        paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    }
}
