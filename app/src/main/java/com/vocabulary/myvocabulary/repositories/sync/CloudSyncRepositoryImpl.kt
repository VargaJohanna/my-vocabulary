package com.vocabulary.myvocabulary.repositories.sync

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.vocabulary.myvocabulary.ui.dictionaries.Dictionary
import com.vocabulary.myvocabulary.ui.words.Word
import kotlinx.coroutines.tasks.await

class CloudSyncRepositoryImpl(
    private val db: FirebaseFirestore
) : CloudSyncRepository {

    override suspend fun uploadDictionary(
        userId: String,
        dictionary: Dictionary,
        words: List<Word>
    ): Result<Unit> = runCatching {
        val dictRef = db.collection("users").document(userId)
            .collection("dictionaries").document(dictionary.dictionaryId.toString())

        db.runBatch { batch ->
            // Set dictionary data
            batch.set(dictRef, dictionary.toCloud())

            // Set words in subcollection
            words.forEach { word ->
                val wordRef = dictRef.collection("words").document(word.wordId.toString())
                batch.set(wordRef, word.toCloud())
            }
        }.await()
    }

    override suspend fun deleteDictionary(userId: String, dictionaryId: Long): Result<Unit> = runCatching {
        val dictRef = db.collection("users").document(userId)
            .collection("dictionaries").document(dictionaryId.toString())
        
        // Note: Firestore delete doesn't automatically delete subcollections. 
        // For a full production app, we would use a Cloud Function or manually delete sub-items.
        // For now, we'll just delete the dictionary document.
        dictRef.delete().await()
    }

    override suspend fun downloadDictionaries(userId: String): Result<List<Pair<CloudDictionary, List<CloudWord>>>> = runCatching {
        Log.d("Sync", "Downloading dictionaries for user $userId from Firestore")
        val dictsQuery = db.collection("users").document(userId)
            .collection("dictionaries").get().await()
        
        Log.d("Sync", "Firestore returned ${dictsQuery.size()} dictionaries")

        dictsQuery.documents.map { dictDoc ->
            Log.d("Sync", "Raw dictionary data: ${dictDoc.data}")
            val cloudDict = dictDoc.toObject(CloudDictionary::class.java)!!
            
            val wordsQuery = dictDoc.reference.collection("words").get().await()
            Log.d("Sync", "Found ${wordsQuery.size()} words for dictionary ${cloudDict.name}")
            
            val cloudWords = wordsQuery.documents.map { wordDoc ->
                Log.d("Sync", "Raw word data: ${wordDoc.data}")
                wordDoc.toObject(CloudWord::class.java)!!
            }
            
            Log.d("Sync", "Fetched dictionary ${cloudDict.name} with ${cloudWords.size} words")
            Pair(cloudDict, cloudWords)
        }
    }
}
