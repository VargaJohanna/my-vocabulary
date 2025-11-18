package com.vocabulary.myvocabulary.navigation

/**
 * Contract for information needed on every MyVocabulary navigation destination
 */
interface MyVocabularyDestinations {
    val route: String
}

/**
 * MyVocabulary app navigation destinations
 */
object Home: MyVocabularyDestinations {
    override val route: String = "home"
}

object DictionaryList: MyVocabularyDestinations {
    override val route: String = "dictionaryList"
}
object About: MyVocabularyDestinations {
    override val route: String = "about"
}