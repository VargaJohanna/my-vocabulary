package com.vocabulary.myvocabulary.repositories

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AppDatabaseMigrationTest {

    @Test
    fun `migration 8 to 9 should execute ALTER TABLE adding is_synced column with default 0`() {
        val mockDb: SupportSQLiteDatabase = mockk(relaxed = true)

        AppDatabase.MIGRATION_8_9.migrate(mockDb)

        verify {
            mockDb.execSQL("ALTER TABLE `dictionaries` ADD COLUMN `is_synced` INTEGER NOT NULL DEFAULT 0")
        }
    }
}
