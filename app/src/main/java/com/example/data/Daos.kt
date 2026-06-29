package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun getCourseById(id: Int): Flow<Course?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Int)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Query("SELECT * FROM user_progress WHERE courseId = :courseId")
    fun getProgressForCourse(courseId: Int): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    @Query("UPDATE user_progress SET isBookmarked = :isBookmarked WHERE courseId = :courseId")
    suspend fun updateBookmarked(courseId: Int, isBookmarked: Boolean)

    @Query("UPDATE user_progress SET isWishlisted = :isWishlisted WHERE courseId = :courseId")
    suspend fun updateWishlisted(courseId: Int, isWishlisted: Boolean)

    @Query("UPDATE user_progress SET isDownloaded = :isDownloaded WHERE courseId = :courseId")
    suspend fun updateDownloaded(courseId: Int, isDownloaded: Boolean)

    @Query("UPDATE user_progress SET isPurchased = :isPurchased WHERE courseId = :courseId")
    suspend fun updatePurchased(courseId: Int, isPurchased: Boolean)
}

@Dao
interface CourseNoteDao {
    @Query("SELECT * FROM course_notes WHERE courseId = :courseId ORDER BY timestamp DESC")
    fun getNotesForCourse(courseId: Int): Flow<List<CourseNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CourseNote)

    @Query("DELETE FROM course_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface AdminSettingsDao {
    @Query("SELECT * FROM admin_settings WHERE id = 1")
    fun getSettings(): Flow<AdminSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AdminSettings)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface PaymentRequestDao {
    @Query("SELECT * FROM payment_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<PaymentRequest>>

    @Query("SELECT * FROM payment_requests WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getRequestsByEmail(email: String): Flow<List<PaymentRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: PaymentRequest): Long

    @Query("UPDATE payment_requests SET status = :status, rejectionReason = :rejectionReason WHERE id = :id")
    suspend fun updateRequestStatus(id: Int, status: String, rejectionReason: String)

    @Query("DELETE FROM payment_requests WHERE id = :id")
    suspend fun deleteRequest(id: Int)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY timestamp DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE email = :email OR mobileNumber = :mobile")
    suspend fun getUserByEmailOrMobile(email: String, mobile: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Query("UPDATE user_accounts SET isApproved = :approved, isPending = :pending, isRejected = :rejected, rejectionReason = :reason, registrationNumber = :regNum WHERE email = :email")
    suspend fun updateUserStatus(email: String, approved: Boolean, pending: Boolean, rejected: Boolean, reason: String, regNum: String)

    @Query("DELETE FROM user_accounts WHERE email = :email")
    suspend fun deleteUser(email: String)
}

