package com.example.whatsappwallpaperpro.domain

enum class BackgroundMode { BLUR, SMART_COLOR, CUSTOM }
enum class OutputFormat(val extension: String) { PNG("png"), JPG("jpg"), WEBP("webp") }
data class OutputSize(val label: String, val width: Int, val height: Int)
val presetSizes = listOf(OutputSize("تلقائي 9:16",1080,1920), OutputSize("أفقي 16:9",1920,1080), OutputSize("مربع 1:1",1080,1080))
