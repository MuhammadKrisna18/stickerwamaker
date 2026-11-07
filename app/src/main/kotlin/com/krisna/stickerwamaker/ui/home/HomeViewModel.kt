package com.krisna.stickerwamaker.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krisna.stickerwamaker.data.model.StickerPack
import com.krisna.stickerwamaker.data.repository.StickerRepository
import com.krisna.stickerwamaker.updater.AppUpdateChecker
import com.krisna.stickerwamaker.updater.AppUpdateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: StickerRepository
) : ViewModel() {

    val stickerPacks: StateFlow<List<StickerPack>> = repository.getStickerPacks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    init {
        checkForUpdates()
    }

    fun checkForUpdates(onChecked: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = AppUpdateChecker.checkForUpdate()
            result.onSuccess { info ->
                _updateInfo.value = if (info.isUpdateAvailable) info else null
                onChecked?.invoke(info.isUpdateAvailable)
            }.onFailure {
                onChecked?.invoke(false)
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun createPack(name: String, publisher: String, onCreated: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isCreating.value = true
            val result = repository.createStickerPack(name, publisher)
            _isCreating.value = false
            result.onSuccess { pack ->
                onCreated(pack.identifier)
            }
        }
    }

    fun deletePack(identifier: String) {
        viewModelScope.launch {
            repository.deleteStickerPack(identifier)
        }
    }

    fun importPack(zipUri: Uri, onImported: (StickerPack) -> Unit) {
        viewModelScope.launch {
            _isCreating.value = true
            val result = repository.importPackFromZip(zipUri)
            _isCreating.value = false
            result.onSuccess { pack ->
                onImported(pack)
            }
        }
    }
}
