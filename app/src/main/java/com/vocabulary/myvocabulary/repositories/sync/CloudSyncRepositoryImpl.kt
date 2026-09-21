package com.vocabulary.myvocabulary.repositories.sync

import com.google.firebase.firestore.FirebaseFirestore
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
        val dictsQuery = db.collection("users").document(userId)
            .collection("dictionaries").get().await()

        dictsQuery.documents.map { dictDoc ->
            val cloudDict = dictDoc.toObject(CloudDictionary::class.java)!!
            val wordsQuery = dictDoc.reference.collection("words").get().await()
            val cloudWords = wordsQuery.documents.map { it.toObject(CloudWord::class.java)!! }
            
            Pair(cloudDict, cloudWords)
        }
    }
}
