package com.example.whatsappwallpaperpro.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappwallpaperpro.data.ExportRepository
import com.example.whatsappwallpaperpro.data.ImageProcessor
import com.example.whatsappwallpaperpro.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

 data class WallpaperUiState(val source: Bitmap?=null, val rendered: Bitmap?=null, val selectedSize: OutputSize=presetSizes[0], val background: BackgroundMode=BackgroundMode.BLUR, val format: OutputFormat=OutputFormat.JPG, val quality: Int=92, val zoom: Float=1f, val customColor: Int=0xFF263238.toInt(), val busy: Boolean=false, val message: String?=null, val savedUri: Uri?=null)
class WallpaperViewModel(app: Application): AndroidViewModel(app) {
    private val _state=MutableStateFlow(WallpaperUiState()); val state: StateFlow<WallpaperUiState> = _state
    private val exporter=ExportRepository(app)
    fun pick(uri: Uri) = viewModelScope.launch(Dispatchers.IO) { _state.value=_state.value.copy(busy=true,message=null); runCatching { ImageProcessor.decode(getApplication<Application>().contentResolver,uri) }.onSuccess { updateSource(it) }.onFailure { _state.value=_state.value.copy(busy=false,message=it.message ?: "تعذر فتح الصورة") } }
    private fun updateSource(bitmap: Bitmap) { val s=_state.value.copy(source=bitmap,busy=false); _state.value=s.copy(rendered=ImageProcessor.render(bitmap,s.selectedSize,s.background,s.customColor,s.zoom)) }
    fun setBackground(mode: BackgroundMode){ _state.value=_state.value.copy(background=mode).rerender() }
    fun setSize(size: OutputSize){ _state.value=_state.value.copy(selectedSize=size).rerender() }
    fun setFormat(format: OutputFormat){ _state.value=_state.value.copy(format=format) }
    fun setQuality(q: Int){ _state.value=_state.value.copy(quality=q) }
    fun setZoom(z: Float){ _state.value=_state.value.copy(zoom=z).rerender() }
    fun save(){ val s=_state.value; val b=s.rendered ?: return; viewModelScope.launch(Dispatchers.IO){ _state.value=s.copy(busy=true,message=null); exporter.save(b,s.format,s.quality).onSuccess { _state.value=_state.value.copy(busy=false,message="تم الحفظ في المعرض",savedUri=Uri.parse(it)) }.onFailure { _state.value=_state.value.copy(busy=false,message=it.message ?: "تعذر الحفظ") } } }
    private fun WallpaperUiState.rerender() = copy(rendered=source?.let{ImageProcessor.render(it,selectedSize,background,customColor,zoom)})
}
