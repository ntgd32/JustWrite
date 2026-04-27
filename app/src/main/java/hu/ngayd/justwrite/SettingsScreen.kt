package hu.ngayd.justwrite

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.ngayd.justwrite.repository.EraseDelay
import hu.ngayd.justwrite.repository.SettingsRepository

class SettingsScreen(
	private val onBack: () -> Unit,
) {

	@Composable
	fun Screen() {
		BackHandler(enabled = true, onBack = {
			onBack()
		})

		Column(
			modifier = Modifier
				.fillMaxSize()
				.windowInsetsPadding(WindowInsets.systemBars)
				.background(color = MaterialTheme.colorScheme.secondary)
		) {
			IconButton(
				modifier = Modifier
					.size(64.dp)
					.align(Alignment.Start),
				onClick = {
					onBack()
				}) {
				Image(
					painter = painterResource(id = R.drawable.back),
					contentDescription = "Back",
					modifier = Modifier.padding(8.dp)
				)
			}

			EraseDelaySettings(
				onSelected = {
					SettingsRepository.setEraseDelay(it.seconds)
				}
			)

			HorizontalDivider(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp, horizontal = 12.dp),
				thickness = 1.dp,
			)
		}
	}

	@Composable
	fun EraseDelaySettings(
		onSelected: (EraseDelay) -> Unit,
	) {
		val eraseDelay = SettingsRepository.eraseDelaySeconds

		Column {
			Text(
				modifier = Modifier
					.padding(start = 12.dp),
				text = "Erase text after",
				style = TextStyle(
					fontSize = 18.sp,
					lineHeight = 24.sp,
				),
				color = MaterialTheme.colorScheme.onSecondary
			)

			Spacer(Modifier.height(8.dp))

			EraseDelayOption(
				text = stringResource(R.string.timer_15),
				selected = eraseDelay.intValue == EraseDelay.SEC_15.seconds,
				onClick = { onSelected(EraseDelay.SEC_15) },
			)

			EraseDelayOption(
				text = stringResource(R.string.timer_30),
				selected = eraseDelay.intValue == EraseDelay.SEC_30.seconds,
				onClick = { onSelected(EraseDelay.SEC_30) },
			)

			EraseDelayOption(
				text = stringResource(R.string.timer_45),
				selected = eraseDelay.intValue == EraseDelay.SEC_45.seconds,
				onClick = { onSelected(EraseDelay.SEC_45) },
			)

			EraseDelayOption(
				text = stringResource(R.string.timer_60),
				selected = eraseDelay.intValue == EraseDelay.SEC_60.seconds,
				onClick = { onSelected(EraseDelay.SEC_60) },
			)
		}
	}

	@Composable
	private fun EraseDelayOption(
		text: String,
		selected: Boolean,
		onClick: () -> Unit,
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onClick() },
			verticalAlignment = Alignment.CenterVertically,
		) {
			RadioButton(
				selected = selected,
				onClick = onClick,
				colors = RadioButtonColors(
					selectedColor = MaterialTheme.colorScheme.onSecondary,
					unselectedColor = MaterialTheme.colorScheme.onSecondary,
					disabledSelectedColor = MaterialTheme.colorScheme.onSecondary,
					disabledUnselectedColor = MaterialTheme.colorScheme.onSecondary
				)
			)
			Spacer(Modifier.width(8.dp))
			Text(
				text = text,
				color = MaterialTheme.colorScheme.onSecondary
			)
		}
	}
}