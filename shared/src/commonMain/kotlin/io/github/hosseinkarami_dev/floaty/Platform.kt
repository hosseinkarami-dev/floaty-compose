package io.github.hosseinkarami_dev.floaty

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform