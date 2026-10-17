package com.example.whatsappwallpaperpro

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whatsappwallpaperpro.domain.*
import com.example.whatsappwallpaperpro.ui.WallpaperViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { WallpaperTheme { WallpaperApp() } } }
}

@Composable private fun WallpaperTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF128C7E), secondary=Color(0xFF25D366)), content=content) }

@Composable private fun WallpaperApp(vm: WallpaperViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var showExport by remember { mutableStateOf(false) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){ it?.let(vm::pick) }
    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Scaffold(topBar={ TopAppBar(title={ Text("WhatsApp Wallpaper Pro",fontWeight=FontWeight.Bold)}, actions={ IconButton(onClick={}){Icon(Icons.Default.Info,"معلومات")} })}) { padding ->
            Column(Modifier.padding(padding).padding(horizontal=16.dp).fillMaxSize(), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                if(state.source==null) EmptyHome(onPick={ picker.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) })
                else Editor(state=state, onPick={ picker.launch(ActivityResultContracts.PickVisualMedia.ImageOnly) }, onBackground=vm::setBackground, onSize=vm::setSize, onZoom=vm::setZoom, onExport={ showExport=true })
                if(state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                state.message?.let { Text(it,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold) }
            }
        }
    }
    if(showExport) ExportDialog(state, onDismiss={showExport=false}, onFormat=vm::setFormat, onQuality=vm::setQuality, onSave={vm.save();showExport=false})
}

@Composable private fun EmptyHome(onPick:()->Unit) { Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){ Icon(Icons.Default.Wallpaper,"خلفية",Modifier.size(76.dp),tint=MaterialTheme.colorScheme.primary); Spacer(Modifier.height(16.dp)); Text("حوّل صورتك إلى خلفية كاملة",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold); Text("بدون قص أو تشويه وبمعالجة محلية",color=MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(24.dp)); Button(onClick=onPick,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().height(56.dp)){Icon(Icons.Default.AddPhotoAlternate,null);Spacer(Modifier.width(8.dp));Text("اختر صورة")} } }

@Composable private fun Editor(state: com.example.whatsappwallpaperpro.ui.WallpaperUiState,onPick:()->Unit,onBackground:(BackgroundMode)->Unit,onSize:(OutputSize)->Unit,onZoom:(Float)->Unit,onExport:()->Unit){
    Text("المعاينة",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(Color(0xFFECE5DD)),contentAlignment=Alignment.Center){ state.rendered?.let{Image(it.asImageBitmap(),"المعاينة",Modifier.fillMaxSize(),contentScale=ContentScale.Fit)} }
    OutlinedButton(onClick=onPick,Modifier.fillMaxWidth()){Icon(Icons.Default.ChangeCircle,null);Spacer(Modifier.width(8.dp));Text("تغيير الصورة")}
    Text("الخلفية الذكية",fontWeight=FontWeight.Bold)
    LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){ items(BackgroundMode.entries){ mode -> FilterChip(selected=state.background==mode,onClick={onBackground(mode)},label={Text(when(mode){BackgroundMode.BLUR->"ضبابية";BackgroundMode.SMART_COLOR->"لون ذكي";BackgroundMode.CUSTOM->"لون مخصص"})}) } }
    Text("المقاس",fontWeight=FontWeight.Bold)
    LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){ items(presetSizes){ size -> FilterChip(selected=state.selectedSize==size,onClick={onSize(size)},label={Text(size.label)}) } }
    Row(verticalAlignment=Alignment.CenterVertically){Text("التكبير",Modifier.width(64.dp));Slider(value=state.zoom,valueRange=0.75f..1.5f,onValueChange=onZoom,modifier=Modifier.weight(1f));Text("%.1fx".format(state.zoom))}
    Button(onClick=onExport,Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Icon(Icons.Default.Save,null);Spacer(Modifier.width(8.dp));Text("حفظ وتصدير")}
}

@Composable private fun ExportDialog(state: com.example.whatsappwallpaperpro.ui.WallpaperUiState,onDismiss:()->Unit,onFormat:(OutputFormat)->Unit,onQuality:(Int)->Unit,onSave:()->Unit){ AlertDialog(onDismissRequest=onDismiss,title={Text("تصدير الخلفية")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text("اختر الصيغة");LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){items(OutputFormat.entries){f->FilterChip(selected=state.format==f,onClick={onFormat(f)},label={Text(f.extension.uppercase())})}};Text("الجودة: ${state.quality}%");Slider(value=state.quality.toFloat(),onValueChange={onQuality(it.toInt())},valueRange=50f..100f,steps=9);state.rendered?.let{Image(it.asImageBitmap(),"النتيجة",Modifier.fillMaxWidth().height(180.dp),contentScale=ContentScale.Fit)}}},confirmButton={Button(onClick=onSave){Text("حفظ في المعرض")}},dismissButton={TextButton(onClick=onDismiss){Text("إلغاء")}}) }
