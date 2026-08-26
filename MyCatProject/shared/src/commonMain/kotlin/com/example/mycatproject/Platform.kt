package com.example.mycatproject

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform