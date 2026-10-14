package fr.readonlymain.gitclient.data.repository

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish
import java.io.File

/** All repositories and remotes live underneath a JUnit-owned temporary directory. */
internal class GitTestFixture(private val root: File) {
    fun repository(name: String = "work"): File = File(root, name).also { directory ->
        Git.init().setDirectory(directory).setInitialBranch("trunk").call().use { git ->
            git.repository.config.apply {
                setString("core", null, "autocrlf", "false")
                save()
            }
            File(directory, "file.txt").writeText("initial\n")
            commit(git, "initial")
        }
    }

    fun commit(git: Git, message: String) {
        git.add().addFilepattern(".").call()
        git.commit().setMessage(message).setAuthor("Test", "test@example.invalid")
            .setCommitter("Test", "test@example.invalid").call()
    }

    fun remoteWithSeed(): Pair<File, File> {
        val remote = File(root, "origin.git")
        Git.init().setBare(true).setInitialBranch("trunk").setDirectory(remote).call().close()
        val seed = repository("seed")
        Git.open(seed).use { git ->
            git.remoteAdd().setName("origin").setUri(URIish(remote.toURI().toString())).call()
            git.push().setRemote("origin").setRefSpecs(RefSpec("refs/heads/trunk:refs/heads/trunk")).call()
        }
        return remote to seed
    }

    fun clone(remote: File, name: String): File = File(root, name).also {
        Git.cloneRepository().setURI(remote.toURI().toString()).setDirectory(it).call().close()
    }
}
