package hu.ngayd.justwrite.editorscreen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.ngayd.justwrite.SessionState
import hu.ngayd.justwrite.repository.SettingsRepository
import hu.ngayd.justwrite.repository.TextRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class TextEditorPresenter : ViewModel() {

	private var eraseJob: Job? = null
	val appBarText = mutableStateOf("")
	val placeholder = TextFieldValue("Start your story...")

	val textFlow: StateFlow<TextFieldValue>
		get() = TextRepository.text

	private fun textValue(): TextFieldValue =
		TextRepository.text.value

	fun onTextChange(newValue: TextFieldValue) {
		TextRepository.text.value = newValue
	}

	init {
		viewModelScope.launch {
			SessionState.isSystemDialogOpen.collect { isOpen ->
				if (isOpen) {
					cancelEraseTimer()
				} else {
					restartEraseTimer()
				}
			}
		}
	}

	fun restartEraseTimer() {
		appBarText.value = ""
		eraseJob?.cancel()
		if (textValue().text != placeholder.text && textValue().text.isNotEmpty())
			eraseJob = viewModelScope.launch {
				delay(SettingsRepository.beforeTimerSeconds.seconds)
				var time = SettingsRepository.afterTimerSeconds
				while (time >= 0) {
					appBarText.value = time.toString()
					delay(1_000L)
					time--
				}
				while (true) {
					if (TextRepository.text.value.text.isEmpty()) {
						appBarText.value = ""
						break
					}

					TextRepository.eraseLastCharacter()

					delay(500L)
				}
			}
	}

	private fun cancelEraseTimer() {
		eraseJob?.cancel()
		eraseJob = null
		appBarText.value = ""
	}
}