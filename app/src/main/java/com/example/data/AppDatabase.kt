package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Course::class,
        UserProgress::class,
        CourseNote::class,
        AdminSettings::class,
        AppNotification::class,
        PaymentRequest::class,
        UserAccount::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun courseNoteDao(): CourseNoteDao
    abstract fun adminSettingsDao(): AdminSettingsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun paymentRequestDao(): PaymentRequestDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trending_deekho_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
