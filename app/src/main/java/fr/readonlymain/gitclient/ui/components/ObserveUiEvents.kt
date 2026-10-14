package fr.readonlymain.gitclient.ui.components

import android.content.ClipData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import fr.readonlymain.gitclient.data.model.UiEvent
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun ObserveUiEvents(
    flow: Flow<UiEvent>,
    snackbarHostState: SnackbarHostState
) {
    val clipboard = LocalClipboard.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.collect { event ->
            val result = when (event) {
                is UiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Copy",
                        duration = SnackbarDuration.Long
                    )
                }

                is UiEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = null,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            if (result == SnackbarResult.ActionPerformed && event is UiEvent.Error) {
                clipboard.setClipEntry(
                    ClipEntry(ClipData.newPlainText("error_log", event.message))
                )
            }
        }
        }
    }
}
