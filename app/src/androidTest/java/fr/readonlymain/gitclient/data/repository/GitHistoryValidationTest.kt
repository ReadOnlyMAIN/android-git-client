package fr.readonlymain.gitclient.data.repository

import android.os.SystemClock
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.junit.Test
import java.io.File
import java.util.UUID

class GitHistoryValidationTest {
    @Test fun generated_history_is_complete_and_repeat_reads_remain_consistent(): Unit = runBlocking {
        val root = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "history-${UUID.randomUUID()}")
        try {
            Git.init().setDirectory(root).setInitialBranch("main").call().use { git ->
                repeat(200) { index ->
                    File(root, "file.txt").writeText("revision $index")
                    git.add().addFilepattern(".").call()
                    git.commit().setMessage("revision $index").setAuthor("Test", "test@example.invalid")
                        .setCommitter("Test", "test@example.invalid").call()
                }
            }
            val repository = DefaultGitRepository()
            val start = SystemClock.elapsedRealtime()
            val first = repository.getCommits(root.path, "main")
            val coldMs = SystemClock.elapsedRealtime() - start
            val warmStart = SystemClock.elapsedRealtime()
            val second = repository.getCommits(root.path, "main")
            val warmMs = SystemClock.elapsedRealtime() - warmStart
            Log.i("AGitValidation", "200-commit history: first=${coldMs}ms repeat=${warmMs}ms")
            assertThat(first).hasSize(200)
            assertThat(first.first().commitMessage).isEqualTo("revision 199")
            assertThat(second).isEqualTo(first)
            // Timings are reported, not a flaky hardware-dependent pass/fail threshold.
        } finally {
            root.deleteRecursively()
        }
    }
}
