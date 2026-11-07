package com.krisna.stickerwamaker.ui.editor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krisna.stickerwamaker.data.repository.StickerRepository
import com.krisna.stickerwamaker.editor.effect.CanvasStickerOutline
import com.krisna.stickerwamaker.editor.effect.StickerOutlineEffect
import com.krisna.stickerwamaker.editor.segmentation.BackgroundRemover
import com.krisna.stickerwamaker.editor.segmentation.MlKitBackgroundRemover
import com.krisna.stickerwamaker.editor.text.StickerTextDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StickerEditorViewModel(
    private val packIdentifier: String,
    private val initialBitmap: Bitmap,
    private val repository: StickerRepository,
    private val backgroundRemover: BackgroundRemover = MlKitBackgroundRemover(),
    private val outlineEffect: StickerOutlineEffect = CanvasStickerOutline()
) : ViewModel() {

    private val _previewBitmap = MutableStateFlow<Bitmap?>(initialBitmap)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isCutoutEnabled = MutableStateFlow(false)
    val isCutoutEnabled: StateFlow<Boolean> = _isCutoutEnabled.asStateFlow()

    private val _hasOutline = MutableStateFlow(false)
    val hasOutline: StateFlow<Boolean> = _hasOutline.asStateFlow()

    private val _outlineWidth = MutableStateFlow(12f)
    val outlineWidth: StateFlow<Float> = _outlineWidth.asStateFlow()

    private val _textOverlay = MutableStateFlow("")
    val textOverlay: StateFlow<String> = _textOverlay.asStateFlow()

    private val _textColor = MutableStateFlow(Color.WHITE)
    val textColor: StateFlow<Int> = _textColor.asStateFlow()

    private val _textPositionTop = MutableStateFlow(false)
    val textPositionTop: StateFlow<Boolean> = _textPositionTop.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var cachedCutoutBitmap: Bitmap? = null

    fun toggleCutout(enabled: Boolean) {
        _isCutoutEnabled.value = enabled
        recomputePreview()
    }

    fun toggleOutline(enabled: Boolean) {
        _hasOutline.value = enabled
        recomputePreview()
    }

    fun setOutlineWidth(width: Float) {
        _outlineWidth.value = width
        recomputePreview()
    }

    fun setTextOverlay(text: String) {
        _textOverlay.value = text
        recomputePreview()
    }

    fun setTextColor(color: Int) {
        _textColor.value = color
        recomputePreview()
    }

    fun toggleTextPosition(top: Boolean) {
        _textPositionTop.value = top
        recomputePreview()
    }

    private fun recomputePreview() {
        viewModelScope.launch {
            _isProcessing.value = true

            val base = if (_isCutoutEnabled.value) {
                if (cachedCutoutBitmap == null) {
                    cachedCutoutBitmap = backgroundRemover.removeBackground(initialBitmap)
                }
                cachedCutoutBitmap ?: initialBitmap
            } else {
                initialBitmap
            }

            val outlined = withContext(Dispatchers.Default) {
                if (_hasOutline.value) {
                    outlineEffect.applyOutline(base, _outlineWidth.value, Color.WHITE)
                } else {
                    base
                }
            }

            val withText = withContext(Dispatchers.Default) {
                if (_textOverlay.value.isNotBlank()) {
                    val yFraction = if (_textPositionTop.value) 0.18f else 0.88f
                    StickerTextDrawer.drawText(
                        source = outlined,
                        text = _textOverlay.value,
                        textColor = _textColor.value,
                        strokeColor = Color.BLACK,
                        textSizeSp = 36f,
                        positionYFraction = yFraction
                    )
                } else {
                    outlined
                }
            }

            _previewBitmap.value = withText
            _isProcessing.value = false
        }
    }

    fun saveSticker(onSaved: () -> Unit) {
        viewModelScope.launch {
            _isProcessing.value = true
            val bitmapToSave = _previewBitmap.value ?: initialBitmap
            repository.addSticker(packIdentifier, bitmapToSave)
            _isProcessing.value = false
            onSaved()
        }
    }
}

