import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vocabulary.myvocabulary.repositories.AppDatabase
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"
    private fun createV4DatabaseWithSchemaAndData(
        dbName: String,
        dictionaryName: String,
        dictionaryCreated: Long,
        insertWord: Boolean
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        context.deleteDatabase(dbName)
        val dbPath = context.getDatabasePath(dbName).path
        val v4Db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
        v4Db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dictionaries (
                dictionary_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dictionary_name TEXT NOT NULL,
                dictionary_created INTEGER NOT NULL
            )
            """.trimIndent()
        )
        // Words table schema as in v4 (and still in v7)
        v4Db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS words (
                word_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                container_dictionary_id INTEGER NOT NULL,
                word TEXT NOT NULL,
                translation TEXT NOT NULL,
                been_asked INTEGER NOT NULL,
                failed INTEGER NOT NULL,
                passed INTEGER NOT NULL,
                created INTEGER NOT NULL,
                last_result INTEGER NOT NULL,
                last_guess TEXT NOT NULL,
                FOREIGN KEY(container_dictionary_id) REFERENCES dictionaries(dictionary_id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        v4Db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_words_container_dictionary_id ON words(container_dictionary_id)"
        )
        v4Db.execSQL(
            "INSERT INTO dictionaries (dictionary_name, dictionary_created) " +
                    "VALUES ('$dictionaryName', $dictionaryCreated)"
        )
        if (insertWord) {
            v4Db.execSQL(
                """
                INSERT INTO words (
                    container_dictionary_id,
                    word,
                    translation,
                    been_asked,
                    failed,
                    passed,
                    created,
                    last_result,
                    last_guess
                ) VALUES (
                    1,
                    'new',
                    'novus',
                    1,
                    0,
                    1,
                    $dictionaryCreated,
                    100,
                    'novus'
                )
                """.trimIndent()
            )
        }
        // Mark DB as version 4 so Room runs MIGRATION_4_7
        v4Db.execSQL("PRAGMA user_version = 4")
        v4Db.close()
    }

    /**
     * Validate the real-world production path: schema version 4 -> 7 for dictionaries.
     */
    @Test
    @Throws(IOException::class)
    fun migrate4To7_preservesDictionaryData() {
        val testName = "Jump User"
        val testCreated = 111222333L

        createV4DatabaseWithSchemaAndData(TEST_DB, testName, testCreated, insertWord = false)

        val context = InstrumentationRegistry.getInstrumentation().targetContext


        // Open with Room, applying MIGRATION_4_7
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DB
        )
            .addMigrations(AppDatabase.MIGRATION_4_7)
            .allowMainThreadQueries()
            .build()

        // Verify all expected columns and data on the migrated schema
        val cursor = db.openHelper.readableDatabase.query(
            "SELECT * FROM dictionaries WHERE dictionary_name = '$testName'"
        )
        assert(cursor.moveToFirst())
        assert(cursor.getLong(cursor.getColumnIndex("dictionary_created")) == testCreated)
        assert(cursor.getColumnIndex("dictionary_last_practiced") != -1)
        assert(cursor.getColumnIndex("dictionary_last_result") != -1)
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_finished_count")) == 0)
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_total_score")) == 0)
        cursor.close()
        db.close()
    }

    /**
     * Validate that existing word data from version 4 is preserved after migrating to 7.
     */
    @Test
    @Throws(IOException::class)
    fun migrate4To7_preservesWordData() {
        val testName = "Jump User"
        val testCreated = 111222333L

        createV4DatabaseWithSchemaAndData(TEST_DB, testName, testCreated, insertWord = true)

        val context = InstrumentationRegistry.getInstrumentation().targetContext


        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DB
        )
            .addMigrations(AppDatabase.MIGRATION_4_7)
            .allowMainThreadQueries()
            .build()

        val cursor = db.openHelper.readableDatabase.query(
            "SELECT * FROM words WHERE container_dictionary_id = 1"
        )
        assert(cursor.moveToFirst())
        // Original columns should still have the same values
        assert(cursor.getString(cursor.getColumnIndex("word")) == "new")
        assert(cursor.getString(cursor.getColumnIndex("translation")) == "novus")
        assert(cursor.getInt(cursor.getColumnIndex("been_asked")) == 1)
        assert(cursor.getInt(cursor.getColumnIndex("failed")) == 0)
        assert(cursor.getInt(cursor.getColumnIndex("passed")) == 1)
        assert(cursor.getInt(cursor.getColumnIndex("last_result")) == 100)
        assert(cursor.getString(cursor.getColumnIndex("last_guess")) == "novus")

        cursor.close()
        db.close()
    }
}