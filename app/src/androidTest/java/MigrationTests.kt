import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vocabulary.myvocabulary.repositories.AppDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        val testId = 1L
        val testName = "Test v4"
        val testCreated = 123456789L

        // 1. Create v4 and insert data
        var db = helper.createDatabase(TEST_DB, 4)
        db.execSQL("""
        INSERT INTO dictionaries (dictionary_id, dictionary_name, dictionary_created) 
        VALUES ($testId, '$testName', $testCreated)
    """.trimIndent())
        db.close()

        // 2. Migrate to v5
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // 3. Verify using safe index checks
        val cursor = db.query("SELECT * FROM dictionaries WHERE dictionary_id = $testId")
        assert(cursor.moveToFirst())

        val nameIdx = cursor.getColumnIndex("dictionary_name")
        val createdIdx = cursor.getColumnIndex("dictionary_created")
        val lastPracticedIdx = cursor.getColumnIndex("dictionary_last_practiced")

        // Ensure columns actually exist (indices are not -1)
        assert(nameIdx != -1 && createdIdx != -1 && lastPracticedIdx != -1)

        // Verify data hasn't changed
        assert(cursor.getString(nameIdx) == testName)
        assert(cursor.getLong(createdIdx) == testCreated)

        // Verify new column is NULL (as expected for nullable Date?)
        assert(cursor.isNull(lastPracticedIdx))

        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        val testName = "Test v5"
        val lastPracticed = 987654321L

        // 1. Create v5 and insert data
        var db = helper.createDatabase(TEST_DB, 5)
        db.execSQL("INSERT INTO dictionaries (dictionary_name, dictionary_created, dictionary_last_practiced) VALUES ('$testName', 123, '$lastPracticed')")
        db.close()

        // 2. Migrate to v6
        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // 3. Verify data integrity
        val cursor = db.query("SELECT * FROM dictionaries WHERE dictionary_name = '$testName'")
        assert(cursor.moveToFirst())
        assert(cursor.getLong(cursor.getColumnIndex("dictionary_last_practiced")) == lastPracticed)
        // Verify new columns added in v6 are present
        assert(cursor.getColumnIndex("dictionary_last_result") != -1)
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_finished_count")) == 0) // Check default value
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        val testName = "Test v6"
        val finishedCount = 5

        // 1. Create v6 and insert data
        var db = helper.createDatabase(TEST_DB, 6)
        db.execSQL("INSERT INTO dictionaries (dictionary_name, dictionary_created, dictionary_finished_count) VALUES ('$testName', 123, '$finishedCount')")
        db.close()

        // 2. Migrate to v7
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        // 3. Verify data integrity
        val cursor = db.query("SELECT * FROM dictionaries WHERE dictionary_name = '$testName'")
        assert(cursor.moveToFirst())
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_finished_count")) == finishedCount)
        // Verify new column added in v7
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_total_score")) == 0) // Default value
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To7_JumpTest() {
        val testName = "Jump User"
        val testCreated = 111222333L

        // 1. Start at v4
        var db = helper.createDatabase(TEST_DB, 4)
        db.execSQL("INSERT INTO dictionaries (dictionary_name, dictionary_created) VALUES ('$testName', '$testCreated')")
        db.close()

        // 2. Jump directly to v7
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_4_7)

        // 3. Verify all columns across the whole history are present and data is safe
        val cursor = db.query("SELECT * FROM dictionaries WHERE dictionary_name = '$testName'")
        assert(cursor.moveToFirst())
        assert(cursor.getLong(cursor.getColumnIndex("dictionary_created")) == testCreated)
        assert(cursor.getColumnIndex("dictionary_last_practiced") != -1)
        assert(cursor.getColumnIndex("dictionary_last_result") != -1)
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_finished_count")) == 0)
        assert(cursor.getInt(cursor.getColumnIndex("dictionary_total_score")) == 0)
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To7_JumpTest() {
        helper.createDatabase(TEST_DB, 5).close()
        helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_5_7)
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To6_JumpTest() {
        helper.createDatabase(TEST_DB, 4).close()
        helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_4_6)
    }
}