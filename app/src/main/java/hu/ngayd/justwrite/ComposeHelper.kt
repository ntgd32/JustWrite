package hu.ngayd.justwrite

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

val aboutText = buildAnnotatedString {
	append(
		"This app is for writing without overthinking.\n" +
			"Pause too long, and the text fades away.\n\n" +
			"Made by NGayd\n" +
			"Icons by "
	)

	withLink(
		LinkAnnotation.Url(
			url = "https://icons8.com"
		)
	) {
		append("Icons8")
	}
}

@Composable
fun rememberImeState(): MutableState<Boolean> {
	val imeState = remember { mutableStateOf(false) }

	val view = LocalView.current

	DisposableEffect(view) {
		val listener = ViewTreeObserver.OnGlobalLayoutListener {
			val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
			imeState.value = isKeyboardOpen
		}
		view.viewTreeObserver.addOnGlobalLayoutListener(listener)
		onDispose {
			view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
		}
	}
	return imeState
}