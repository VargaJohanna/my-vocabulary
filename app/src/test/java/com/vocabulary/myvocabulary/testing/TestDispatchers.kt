package com.vocabulary.myvocabulary.testing

import com.vocabulary.myvocabulary.DispatcherProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatchers(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
): DispatcherProvider {
    override val main: TestDispatcher
        get() = testDispatcher
    override val mainImmediate: TestDispatcher
        get() = testDispatcher
    override val io: TestDispatcher
        get() = testDispatcher
    override val default: TestDispatcher
        get() = testDispatcher
}