package com.flipperdevices.bridge.synchronization.impl.repository

import com.flipperdevices.bridge.dao.api.delegates.FavoriteApi
import com.flipperdevices.bridge.dao.api.model.FlipperKeyPath
import com.flipperdevices.bridge.synchronization.impl.executor.FlipperKeyStorage
import com.flipperdevices.bridge.synchronization.impl.model.DiffSource
import com.flipperdevices.bridge.synchronization.impl.model.KeyAction
import com.flipperdevices.bridge.synchronization.impl.model.KeyDiff
import com.flipperdevices.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepository
import com.flipperdevices.bridge.synchronization.impl.repository.storage.ManifestRepository
import com.flipperdevices.bridge.synchronization.impl.utils.KeyDiffCombiner
import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info

class FavoriteSynchronization constructor(
    private val favoriteApi: FavoriteApi,
    private val manifestRepository: ManifestRepository,
    private val flipperStorage: FlipperKeyStorage
) : LogTagProvider {
    override val TAG = "FavoriteSynchronization"

    private val favoritesRepository = FlipperFavoritesRepository()

    suspend fun syncFavorites(): List<FlipperKeyPath> {
        val favoritesFromFlipper = favoritesRepository.getFavorites(flipperStorage)
        val favoritesFromAndroid = favoriteApi.getFavorites().map { it.path }
        val diffWithManifestAndFlipper = manifestRepository
            .compareFavoritesWithManifest(favoritesFromFlipper, DiffSource.FLIPPER)
        val diffWithManifestAndAndroid = manifestRepository
            .compareFavoritesWithManifest(favoritesFromAndroid, DiffSource.ANDROID)

        info {
            "Receive favorites data. " +
                "Flipper: ${favoritesFromFlipper.size} " +
                "(Diff - ${diffWithManifestAndFlipper.size}). " +
                "Android: ${favoritesFromAndroid.size} " +
                "(Diff - ${diffWithManifestAndAndroid.size})"
        }

        val combinedDiff = KeyDiffCombiner.combineKeyDiffs(
            diffWithManifestAndFlipper,
            diffWithManifestAndAndroid
        )
        info { "Favorites diff is $combinedDiff" }

        if (combinedDiff.isEmpty()) {
            return favoritesFromFlipper
        }

        val resultFavoritesList = mergedWithManifestList(combinedDiff)
        favoritesRepository.updateFavorites(
            flipperStorage,
            resultFavoritesList
        ) // Update on Flipper
        favoriteApi.updateFavorites(resultFavoritesList)

        return resultFavoritesList
    }

    private suspend fun mergedWithManifestList(combinedDiff: List<KeyDiff>): List<FlipperKeyPath> {
        val favoritesFromManifest = manifestRepository.getFavorites() ?: emptyList()
        val resultFavoritesList = ArrayList(favoritesFromManifest)
        for (diff in combinedDiff) {
            when (diff.action) {
                KeyAction.ADD -> resultFavoritesList.add(diff.newHash.keyPath)
                KeyAction.MODIFIED -> resultFavoritesList.add(diff.newHash.keyPath)
                KeyAction.DELETED -> resultFavoritesList.remove(diff.newHash.keyPath)
            }
        }
        info { "Favorites list is $resultFavoritesList" }
        return resultFavoritesList
    }
}
