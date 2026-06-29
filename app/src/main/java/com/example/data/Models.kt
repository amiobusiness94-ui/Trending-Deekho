package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val instructor: String,
    val category: String,
    val duration: String,
    val rating: Double,
    val reviewsCount: Int,
    val price: Double, // 0.0 means Free, >0 means Premium
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val isBestSelling: Boolean = false,
    val isNew: Boolean = false,
    val lecturesJson: String // Serialized List<Lecture>
)

data class Lecture(
    val title: String,
    val duration: String,
    val videoUrl: String,
    val description: String
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val courseId: Int,
    val lastWatchedLectureIndex: Int = 0,
    val lastWatchedPositionMs: Long = 0,
    val percentCompleted: Int = 0,
    val isBookmarked: Boolean = false,
    val isWishlisted: Boolean = false,
    val isDownloaded: Boolean = false,
    val isPurchased: Boolean = false,
    val completedLecturesCsv: String = "" // "0,1,3" indicating which lectures are done
)

@Entity(tableName = "course_notes")
data class CourseNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val lectureIndex: Int,
    val noteText: String,
    val positionMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSettings(
    @PrimaryKey val id: Int = 1,
    val adsEnabled: Boolean = true,
    val admobBannerId: String = "ca-app-pub-3940256099942544/6300978111",
    val admobInterstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    val admobRewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    val adPosition: String = "Bottom Banner", // "Top Banner", "Bottom Banner", "Feed Card"
    val couponCode: String = "GROW50",
    val discountPercent: Int = 50,
    val totalPurchasesRevenue: Double = 3450.0,
    val totalActiveUsers: Int = 1240,
    // Custom pricing & payment settings
    val coursePrice: Double = 29.0,
    val currency: String = "INR",
    val paymentType: String = "One-Time Payment",
    val access: String = "Lifetime Access",
    val unlockMethod: String = "Payment Verification",
    val upiId: String = "amiyosarkar.a-1@okaxis",
    val qrCodeUrlOrPath: String = "" // can be path or raw Base64
)

@Entity(tableName = "payment_requests")
data class PaymentRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val courseTitle: String,
    val userEmail: String,
    val userName: String,
    val transactionId: String,
    val amount: Double,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val upiIdUsed: String,
    val screenshotPath: String = "", // Base64 of image, mock file path, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val rejectionReason: String = ""
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val mobileNumber: String,
    val password: String,
    val profilePhotoUri: String = "",
    val address: String = "",
    val district: String = "",
    val state: String = "",
    val pinCode: String = "",
    val registrationNumber: String = "",
    val isApproved: Boolean = false,
    val isPending: Boolean = false,
    val isRejected: Boolean = false,
    val rejectionReason: String = "",
    val paymentTxnId: String = "",
    val paymentScreenshot: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

