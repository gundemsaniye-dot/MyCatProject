package com.example.mycatproject

class WebPlatform : Platform {
    override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebPlatform()
