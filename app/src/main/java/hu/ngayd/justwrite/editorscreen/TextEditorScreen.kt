package hu.ngayd.justwrite.editorscreen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.ngayd.justwrite.R
import hu.ngayd.justwrite.rememberImeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TextEditorScreen(
	private val pr: TextEditorPresenter,
	private val onSave: () -> Unit,
	private val onSaveAs: () -> Unit,
	private val onOpen: () -> Unit,
	private val onOpenSettings: () -> Unit,
	private val onOpenStats: () -> Unit,
	private val onOpenAbout: () -> Unit,
) {

	@Composable
	fun Screen() {
		val coroutineScope = rememberCoroutineScope()
		val imeState = rememberImeState()
		val scrollState = rememberScrollState()
		val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
		val text = pr.textFlow.collectAsState()
		val configuration = LocalConfiguration.current
		val isVertical = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
		val scrollOffset = if (isVertical) 0.9 else 0.6

		//scrolling to cursor on keyboard opening
		LaunchedEffect(imeState.value) {
			val cursorPosition = textLayoutResult.value?.getCursorRect(text.value.selection.start)?.top?.toInt()
			if (imeState.value && cursorPosition != null) {
				coroutineScope.launch {
					delay(100)
					val scrollPosition = cursorPosition - (scrollState.viewportSize * scrollOffset).toInt()
					if (scrollPosition < scrollState.maxValue) scrollState.animateScrollTo(scrollPosition)
					else scrollState.animateScrollTo(scrollState.maxValue)
				}
			}
		}

		val interactionSource = remember { MutableInteractionSource() }
		val isFocused = interactionSource.collectIsFocusedAsState()

		//setting placeholder or no placeholder if text is empty
		LaunchedEffect(isFocused.value) {
			if (isFocused.value && text.value.text == pr.placeholder.text) pr.onTextChange(TextFieldValue(""))
			if (!isFocused.value && text.value.text == "") pr.onTextChange(pr.placeholder)
		}

		val selectionColors = TextSelectionColors(
			handleColor = MaterialTheme.colorScheme.onPrimaryContainer,
			backgroundColor = MaterialTheme.colorScheme.inversePrimary.copy(alpha = 0.3f)
		)
		val bringIntoViewRequester = remember { BringIntoViewRequester() }
		val topBottomOffset = if (isVertical) 8.dp else 4.dp

		Scaffold(
			modifier = Modifier
				.fillMaxSize(),
			topBar = {
				TopBar(
					modifier = Modifier,
					appBarText = pr.appBarText,
				)
			}
		) { innerPadding ->
			Column(
				modifier = Modifier
					.imePadding()
					.fillMaxSize()
					.background(color = MaterialTheme.colorScheme.primary)
					.padding(
						top = innerPadding.calculateTopPadding() + topBottomOffset,
						start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
						end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
						bottom = if (!imeState.value) innerPadding.calculateBottomPadding() + topBottomOffset else topBottomOffset
					)
					.verticalScroll(scrollState)
			) {
				CompositionLocalProvider(
					LocalTextSelectionColors provides selectionColors
				) {
					BasicTextField(
						modifier = Modifier
							.fillMaxWidth()
							.fillMaxHeight()
							.bringIntoViewRequester(bringIntoViewRequester)
							.padding(start = 16.dp, end = 16.dp),
						value = text.value,
						textStyle = TextStyle(
							color = MaterialTheme.colorScheme.onPrimary,
							fontSize = 18.sp
						),
						onValueChange = {
							if (it.text != text.value.text) {
								pr.restartEraseTimer()
							}
							pr.onTextChange(it)
						},
						onTextLayout = { layoutResult ->
							textLayoutResult.value = layoutResult
							val cursorRect = layoutResult.getCursorRect(text.value.selection.start)
							coroutineScope.launch {
								bringIntoViewRequester.bringIntoView(cursorRect) //scroll to cursor position by typing
							}
						},
						interactionSource = interactionSource,
						cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun TopBar(
		modifier: Modifier,
		appBarText: MutableState<String>,
	) {
		var isMenuExpanded by remember { mutableStateOf(false) }

		Column {
			TopAppBar(
				modifier = modifier.height(80.dp),
				title = {
					Text(
						appBarText.value,
						color = MaterialTheme.colorScheme.onPrimary
					)
				},
				actions = {
					IconButton(onClick = {
						onOpen()
					}) {
						Image(
							painter = painterResource(id = R.drawable.open_from_cataloque),
							contentDescription = stringResource(R.string.open_title)
						)
					}
					IconButton(onClick = {
						onSaveAs()
					}) {
						Image(
							painter = painterResource(id = R.drawable.download),
							contentDescription = stringResource(R.string.save_as_title)
						)
					}
					IconButton(onClick = {
						onSave()
					}) {
						Image(
							painter = painterResource(id = R.drawable.save),
							contentDescription = stringResource(R.string.save_title)
						)
					}
					IconButton(onClick = { isMenuExpanded = true }) {
						Image(
							painter = painterResource(id = R.drawable.menuvertical),
							contentDescription = stringResource(R.string.menu_title)
						)
					}
					DropdownMenu(
						expanded = isMenuExpanded,
						onDismissRequest = { isMenuExpanded = false },
						offset = DpOffset(x = 50.dp, y = 12.dp)
					) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.stats_title)) },
							onClick = {
								isMenuExpanded = false
								onOpenStats()
							},
							leadingIcon = {
								Image(
									painter = painterResource(id = R.drawable.stats),
									contentDescription = stringResource(R.string.stats_title)
								)
							},
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.settings_title)) },
							onClick = {
								isMenuExpanded = false
								onOpenSettings()
							},
							leadingIcon = {
								Image(
									painter = painterResource(id = R.drawable.settings),
									contentDescription = stringResource(R.string.settings_title)
								)
							},
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.about_title)) },
							onClick = {
								isMenuExpanded = false
								onOpenAbout()
							},
							leadingIcon = {
								Image(
									painter = painterResource(id = R.drawable.about),
									contentDescription = stringResource(R.string.about_title)
								)
							},
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primary,
				)
			)

			HorizontalDivider(
				modifier = Modifier
					.fillMaxWidth(),
				thickness = 1.dp,
				color = Color.Black
			)
		}
	}
}