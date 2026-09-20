package com.sevvanam.android_interview_architect.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sevvanam.android_interview_architect.core.database.di.seedTopicsAndQuestions

/**
 * v1 -> v2: adds the `topics` and `questions` tables. `posts` is untouched, so users keep their likes
 * (the old fallbackToDestructiveMigration would have wiped them). The Callback's onCreate seeding only
 * runs for brand-new databases, so the migration seeds the new tables itself.
 *
 * The SQL mirrors the schema Room exports to core/database/schemas/.../2.json; a mismatch fails Room's
 * post-migration schema validation at open time (covered by AppDatabaseMigrationTest).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `topics` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, `imageUrl` TEXT, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `questions` (`id` TEXT NOT NULL, `topicId` TEXT NOT NULL, " +
                "`question` TEXT NOT NULL, `answer` TEXT NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`topicId`) REFERENCES `topics`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_topicId` ON `questions` (`topicId`)")
        seedTopicsAndQuestions(db)
    }
}
