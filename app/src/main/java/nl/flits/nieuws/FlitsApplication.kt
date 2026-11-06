package nl.flits.nieuws

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import nl.flits.nieuws.data.NewsRepository
import nl.flits.nieuws.data.SettingsRepository

class FlitsApplication : Application(), ImageLoaderFactory {
    lateinit var news: NewsRepository
        private set
    lateinit var settings: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        news = NewsRepository(this)
        settings = SettingsRepository(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .crossfade(true)
        .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.2).build() }
        .diskCache { DiskCache.Builder().directory(cacheDir.resolve("images")).maxSizeBytes(80L * 1024 * 1024).build() }
        .build()
}
