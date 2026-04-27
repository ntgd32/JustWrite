package hu.ngayd.justwrite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun InfoDialog(
	title: String,
	text: String,
	annotatedText: AnnotatedString,
	onDismiss: () -> Unit,
) {
	Dialog(onDismissRequest = { onDismiss() }) {
		Surface(
			shape = RoundedCornerShape(12.dp),
			tonalElevation = 6.dp
		) {
			Column(
				modifier = Modifier
					.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					modifier = Modifier
						.padding(bottom = 12.dp),
					text = title,
					style = TextStyle(
						fontSize = 18.sp,
					)
				)

				if (text.isNotEmpty())
					Text(
						text = text,
						style = TextStyle(
							fontSize = 14.sp,
						)
					)
				else
					Text(
						text = annotatedText,
						style = TextStyle(
							fontSize = 14.sp,
						)
					)
			}
		}
	}
}

enum class DialogType { STATS, ABOUT }