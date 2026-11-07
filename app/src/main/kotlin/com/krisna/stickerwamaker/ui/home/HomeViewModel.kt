package com.krisna.stickerwamaker.ui.home

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
}

