package com.krisna.stickerwamaker

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.krisna.stickerwamaker.data.repository.StickerRepositoryImpl
import com.krisna.stickerwamaker.ui.detail.PackDetailScreen
import com.krisna.stickerwamaker.ui.detail.PackDetailViewModel
import com.krisna.stickerwamaker.ui.editor.StickerEditorScreen
import com.krisna.stickerwamaker.ui.editor.StickerEditorViewModel
import com.krisna.stickerwamaker.ui.home.HomeScreen
import com.krisna.stickerwamaker.ui.home.HomeViewModel
import com.krisna.stickerwamaker.util.BitmapUtils

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val repository = remember { StickerRepositoryImpl.getInstance(context) }
    val backStack = rememberNavBackStack(HomeRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeRoute> {
                val homeViewModel = viewModel { HomeViewModel(repository) }
                HomeScreen(
                    onPackClick = { packId ->
                        backStack.add(PackDetailRoute(packId))
                    },
                    viewModel = homeViewModel
                )
            }
            entry<PackDetailRoute> { route ->
                val detailViewModel = viewModel(key = route.packIdentifier) {
                    PackDetailViewModel(route.packIdentifier, repository)
                }
                PackDetailScreen(
                    onBackClick = { backStack.removeLastOrNull() },
                    onNavigateToEditor = { uriString ->
                        backStack.add(EditorRoute(route.packIdentifier, uriString))
                    },
                    viewModel = detailViewModel
                )
            }
            entry<EditorRoute> { route ->
                val uri = Uri.parse(route.imageUriString)
                val bitmap = remember(route.imageUriString) {
                    BitmapUtils.loadBitmapFromUri(context, uri)
                }
                if (bitmap != null) {
                    val editorViewModel = viewModel(key = "${route.packIdentifier}_${route.imageUriString}") {
                        StickerEditorViewModel(
                            packIdentifier = route.packIdentifier,
                            initialBitmap = bitmap,
                            repository = repository
                        )
                    }
                    StickerEditorScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onSaved = { backStack.removeLastOrNull() },
                        viewModel = editorViewModel
                    )
                } else {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}
