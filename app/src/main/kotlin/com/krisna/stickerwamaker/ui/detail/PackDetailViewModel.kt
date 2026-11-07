package com.krisna.stickerwamaker.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krisna.stickerwamaker.data.model.StickerPack
import com.krisna.stickerwamaker.data.repository.StickerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class PackDetailViewModel(
    private val packIdentifier: String,
    private val repository: StickerRepository
) : ViewModel() {

    val pack: StateFlow<StickerPack?> = repository.getStickerPack(packIdentifier)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun getStickerFile(fileName: String): File? {
        return repository.getPackFile(packIdentifier, fileName)
    }

    fun addSticker(bitmap: Bitmap, emojis: List<String> = emptyList()) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.addSticker(packIdentifier, bitmap, emojis)
            _isProcessing.value = false
        }
    }

    fun removeSticker(fileName: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.removeSticker(packIdentifier, fileName)
            _isProcessing.value = false
        }
    }

    fun updateTrayIcon(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.updateTrayIcon(packIdentifier, bitmap)
            _isProcessing.value = false
        }
    }

    fun deletePack(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            repository.deleteStickerPack(packIdentifier).onSuccess {
                onDeleted()
            }
            _isProcessing.value = false
        }
    }
}

