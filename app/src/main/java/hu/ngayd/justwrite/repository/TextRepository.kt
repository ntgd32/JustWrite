package hu.ngayd.justwrite.repository

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.MutableStateFlow

object TextRepository {
	val text = MutableStateFlow(TextFieldValue("Start your story..."))
	private var lostCharacters = 0
	private var lostWords = 0
	var uri: Uri? = null

	fun eraseLastCharacter() {
		val currentText = text.value.text
		val removedChar = currentText.last()
		val newText = currentText.dropLast(1)
		val newLastChar = newText.lastOrNull()
		val removedWasLetter = removedChar.isLetterOrDigit()
		val newEndsWithLetter = newLastChar?.isLetterOrDigit() == true

		if (removedWasLetter && !newEndsWithLetter) {
			lostWords++
		}
		lostCharacters++

		text.value = text.value.copy(
			text = newText,
		)
	}

	private fun countWords(): Int {
		return Regex("\\b[\\p{L}\\p{N}]+\\b")
			.findAll(text.value.text)
			.count()
	}

	fun resetLostStats() {
		lostCharacters = 0
		lostWords = 0
	}

	fun getStats(): TextStats {
		return TextStats(
			wordsWritten = countWords(),
			charactersWritten = text.value.text.length,
			wordsLost = lostWords,
			charactersLost = lostCharacters
		)
	}
}

data class TextStats(
	val wordsWritten: Int,
	val charactersWritten: Int,
	val wordsLost: Int,
	val charactersLost: Int
)