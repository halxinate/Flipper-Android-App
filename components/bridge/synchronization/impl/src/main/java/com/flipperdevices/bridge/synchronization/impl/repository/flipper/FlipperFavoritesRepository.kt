package com.flipperdevices.bridge.synchronization.impl.repository.flipper

import com.flipperdevices.bridge.api.utils.Constants
import com.flipperdevices.bridge.dao.api.model.FlipperKeyContent
import com.flipperdevices.bridge.dao.api.model.FlipperKeyPath
import com.flipperdevices.bridge.synchronization.impl.executor.FlipperKeyStorage
import com.flipperdevices.core.log.LogTagProvider
import java.io.File
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val FAVORITES_PATH = FlipperKeyPath("/", "favorites.txt")

class FlipperFavoritesRepository : LogTagProvider {
    override val TAG = "FavoritesRepository"

    suspend fun getFavorites(flipperKeyStorage: FlipperKeyStorage): List<FlipperKeyPath> {
        val favoritesPaths = getFavoritesFromFlipper(flipperKeyStorage)
        return favoritesPaths.map {
            val relativePath = it.replace(Constants.KEYS_DEFAULT_STORAGE, "").replace("/ext/", "")
            return@map relativePath.substringBefore("/") to relativePath.substringAfter("/")
        }.filter { (keyFolder, keyName) ->
            keyFolder.trim().isNotEmpty() || keyName.trim().isNotEmpty()
        }.map { (keyFolder, keyName) ->
            return@map FlipperKeyPath(
                folder = keyFolder,
                name = keyName
            )
        }
    }

    private suspend fun getFavoritesFromFlipper(
        flipperKeyStorage: FlipperKeyStorage
    ): List<String> = withContext(Dispatchers.IO) {
        val favoritesFile = flipperKeyStorage.loadKey(FAVORITES_PATH).openStream().use {
            it.readBytes().toString(Charset.defaultCharset())
        }
        return@withContext favoritesFile
            .split("\n")
    }

    suspend fun updateFavorites(
        flipperKeyStorage: FlipperKeyStorage,
        favorites: List<FlipperKeyPath>
    ) {
        val newFavoritesFile = favorites.joinToString("\n") {
            File(Constants.KEYS_DEFAULT_STORAGE, it.pathToKey).absolutePath
        } + "\n"
        flipperKeyStorage.saveKey(
            FAVORITES_PATH,
            FlipperKeyContent.RawData(newFavoritesFile.toByteArray())
        )
    }
}
