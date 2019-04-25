package com.vocabulary.myvocabulary.repositories.guessedWord

sealed class GuessedMapData {

    object EMPTY : GuessedMapData()

    data class GuessedData(
            val map: Map<Long, String>
    ) : GuessedMapData()
}