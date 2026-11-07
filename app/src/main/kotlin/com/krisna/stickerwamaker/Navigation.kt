package com.krisna.stickerwamaker

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
import com.krisna.stickerwamaker.ui.home.HomeScreen
import com.krisna.stickerwamaker.ui.home.HomeViewModel

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
                    viewModel = detailViewModel
                )
            }
        }
    )
}
