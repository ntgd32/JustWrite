package hu.ngayd.justwrite

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.lifecycleScope
import hu.ngayd.justwrite.editorscreen.TextEditorPresenter
import hu.ngayd.justwrite.editorscreen.TextEditorScreen
import hu.ngayd.justwrite.repository.SettingsRepository
import hu.ngayd.justwrite.repository.TextRepository
import hu.ngayd.justwrite.ui.theme.JustWriteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

	private val createTextFileLauncher =
		registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
			SessionState.setDialogClosed()
			uri ?: return@registerForActivityResult

			lifecycleScope.launch(Dispatchers.IO) {
				writeToFile(uri, TextRepository.text.value.text, contentResolver)
			}
			TextRepository.uri = uri
			Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
		}

	private val openTextFileLauncher =
		registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			if (uri != null) {
				lifecycleScope.launch(Dispatchers.IO) {
					val text = readFromFile(uri, contentResolver)
					TextRepository.text.value = TextFieldValue(text)
					TextRepository.uri = uri
					TextRepository.resetLostStats()
					SessionState.setDialogClosed()
				}
			} else {
				SessionState.setDialogClosed()
			}
		}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		SettingsRepository.init(this)

		setContent {
			val openedDialog = remember { mutableStateOf<DialogType?>(null) }
			val areSettingsOpened = remember { mutableStateOf(false) }
			val presenter: TextEditorPresenter = androidx.lifecycle.viewmodel.compose.viewModel()

			JustWriteTheme {
				TextEditorScreen(
					pr = presenter,
					onSaveAs = {
						SessionState.setDialogOpened()
						createTextFileLauncher.launch("${TextRepository.text.value.text.take(15)}.txt")
					},
					onOpen = {
						SessionState.setDialogOpened()
						openTextFileLauncher.launch(arrayOf("text/plain"))
					},
					onSave = {
						SessionState.setDialogOpened()
						val uri = TextRepository.uri
						if (uri != null) {
							writeToFile(uri, TextRepository.text.value.text, contentResolver)
							Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
						} else createTextFileLauncher.launch("${TextRepository.text.value.text.take(15)}.txt")
					},
					onOpenSettings = {
						SessionState.setDialogOpened()
						areSettingsOpened.value = true
					},
					onOpenStats = {
						SessionState.setDialogOpened()
						openedDialog.value = DialogType.STATS
					},
					onOpenAbout = {
						SessionState.setDialogOpened()
						openedDialog.value = DialogType.ABOUT
					},
				).Screen()

				if (areSettingsOpened.value)

					SettingsScreen(
						onBack = {
							SessionState.setDialogClosed()
							areSettingsOpened.value = false
						},
					).Screen()

				if (openedDialog.value == DialogType.STATS) {
					val stats = TextRepository.getStats()
					InfoDialog(
						title = stringResource(R.string.stats_title),
						text = stringResource(
							R.string.stats_text,
							stats.wordsWritten,
							stats.charactersWritten,
							stats.wordsLost,
							stats.charactersLost
						),
						annotatedText = AnnotatedString(""),
						onDismiss = {
							SessionState.setDialogClosed()
							openedDialog.value = null
						}
					)
				}

				if (openedDialog.value == DialogType.ABOUT)
					InfoDialog(
						title = stringResource(R.string.about_title),
						text = "",
						annotatedText = aboutText,
						onDismiss = {
							SessionState.setDialogClosed()
							openedDialog.value = null
						}
					)

			}
		}
	}
}