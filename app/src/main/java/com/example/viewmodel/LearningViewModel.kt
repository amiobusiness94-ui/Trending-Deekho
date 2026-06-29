package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String,
    val email: String,
    val isGoogleLogin: Boolean = false,
    val isLoggedIn: Boolean = false,
    val mobileNumber: String = "",
    val profilePhotoUri: String = "",
    val registrationNumber: String = "",
    val isApproved: Boolean = true,
    val isPending: Boolean = false,
    val isRejected: Boolean = false,
    val rejectionReason: String = "",
    val address: String = "",
    val district: String = "",
    val state: String = "",
    val pinCode: String = "",
    val role: String = "Student" // "Student", "Admin"
)

class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CourseRepository(db)

    // Seeding trigger
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Auth state
    private val _userProfile = MutableStateFlow(
        UserProfile("Amio Rahman", "amiobusiness94@gmail.com", isGoogleLogin = true, isLoggedIn = true)
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Query states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    // Streams from database
    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressList: StateFlow<List<UserProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSettings: StateFlow<AdminSettings> = repository.adminSettings
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminSettings())

    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentRequests: StateFlow<List<PaymentRequest>> = repository.allPaymentRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Details/Focus screen states
    private val _focusedCourseId = MutableStateFlow<Int?>(null)
    val focusedCourseId: StateFlow<Int?> = _focusedCourseId.asStateFlow()

    val focusedCourse: StateFlow<Course?> = _focusedCourseId
        .flatMapLatest { id ->
            if (id != null) repository.getCourseById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val focusedProgress: StateFlow<UserProgress?> = _focusedCourseId
        .flatMapLatest { id ->
            if (id != null) repository.getProgressForCourse(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val focusedNotes: StateFlow<List<CourseNote>> = _focusedCourseId
        .flatMapLatest { id ->
            if (id != null) repository.getNotesForCourse(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated Video Player States
    val videoQuality = MutableStateFlow("1080p") // "360p", "480p", "720p", "1080p"
    val videoSpeed = MutableStateFlow(1.0f) // 0.5f, 1.0f, 1.5f, 2.0f
    val activeLectureIndex = MutableStateFlow(0)
    val isPlaying = MutableStateFlow(false)
    val playbackPositionMs = MutableStateFlow(0L)
    val isPiP = MutableStateFlow(false)
    val isFullscreen = MutableStateFlow(false)
    
    // UI temporary states
    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage: StateFlow<String?> = _couponMessage.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    // Auth functions
    fun login(emailOrMobile: String, passwordEntered: String): String? {
        if (emailOrMobile.trim().equals("admin@example.com", ignoreCase = true) || emailOrMobile.trim() == "admin") {
            _userProfile.value = UserProfile(
                name = "Admin Principal",
                email = "admin@example.com",
                isLoggedIn = true,
                role = "Admin"
            )
            return null
        }
        
        val users = allUsers.value
        val foundUser = users.find { 
            it.email.equals(emailOrMobile.trim(), ignoreCase = true) || it.mobileNumber == emailOrMobile.trim() 
        }
        
        if (foundUser == null) {
            if (emailOrMobile.trim().equals("amiobusiness94@gmail.com", ignoreCase = true)) {
                _userProfile.value = UserProfile(
                    name = "Amio Rahman",
                    email = "amiobusiness94@gmail.com",
                    isLoggedIn = true,
                    mobileNumber = "9876543210",
                    registrationNumber = "TD-2026-REG-8821",
                    isApproved = true,
                    address = "Sector 5, Salt Lake",
                    district = "North 24 Parganas",
                    state = "West Bengal",
                    pinCode = "700091",
                    role = "Student"
                )
                return null
            }
            return "User not found. Please Sign Up!"
        }
        
        if (foundUser.password != passwordEntered) {
            return "Incorrect password. Please try again."
        }
        
        _userProfile.value = UserProfile(
            name = "${foundUser.firstName} ${foundUser.lastName}",
            email = foundUser.email,
            isLoggedIn = true,
            mobileNumber = foundUser.mobileNumber,
            profilePhotoUri = foundUser.profilePhotoUri,
            registrationNumber = foundUser.registrationNumber,
            isApproved = foundUser.isApproved,
            isPending = foundUser.isPending,
            isRejected = foundUser.isRejected,
            rejectionReason = foundUser.rejectionReason,
            address = foundUser.address,
            district = foundUser.district,
            state = foundUser.state,
            pinCode = foundUser.pinCode,
            role = "Student"
        )
        return null
    }

    fun login(name: String, email: String, isGoogle: Boolean) {
        viewModelScope.launch {
            if (email.equals("admin@example.com", ignoreCase = true)) {
                _userProfile.value = UserProfile(
                    name = "Admin Principal",
                    email = "admin@example.com",
                    isLoggedIn = true,
                    role = "Admin"
                )
            } else {
                // If it is Amio, set it as approved
                val isApprovedVal = email.equals("amiobusiness94@gmail.com", ignoreCase = true)
                _userProfile.value = UserProfile(
                    name = name,
                    email = email,
                    isGoogleLogin = isGoogle,
                    isLoggedIn = true,
                    isApproved = isApprovedVal,
                    registrationNumber = if (isApprovedVal) "TD-2026-REG-8821" else "",
                    role = "Student"
                )
            }
            repository.sendNotification("Welcome Back! 🌟", "Logged in successfully as $name.")
        }
    }

    fun signUpUser(
        firstName: String,
        lastName: String,
        email: String,
        mobileNumber: String,
        address: String,
        district: String,
        state: String,
        pinCode: String,
        passwordEntered: String,
        profilePhotoUri: String
    ): String? {
        val existing = allUsers.value.find { 
            it.email.equals(email.trim(), ignoreCase = true) || it.mobileNumber == mobileNumber.trim() 
        }
        if (existing != null) {
            return "An account with this email or mobile number already exists."
        }
        
        viewModelScope.launch {
            val newUser = UserAccount(
                email = email.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                mobileNumber = mobileNumber.trim(),
                password = passwordEntered,
                profilePhotoUri = profilePhotoUri,
                address = address.trim(),
                district = district.trim(),
                state = state.trim(),
                pinCode = pinCode.trim(),
                isApproved = false,
                isPending = true,
                isRejected = false,
                registrationNumber = ""
            )
            repository.registerUser(newUser)
            
            _userProfile.value = UserProfile(
                name = "${firstName.trim()} ${lastName.trim()}",
                email = email.trim(),
                isLoggedIn = true,
                mobileNumber = mobileNumber.trim(),
                profilePhotoUri = profilePhotoUri,
                isApproved = false,
                isPending = true,
                address = address.trim(),
                district = district.trim(),
                state = state.trim(),
                pinCode = pinCode.trim(),
                role = "Student"
            )
            
            repository.sendNotification(
                "Account Registered! ⏳",
                "Your registration for Trending Deekho is successful. Please submit your ₹29 registration fee payment to activate your account."
            )
        }
        return null
    }

    fun submitRegistrationPayment(transactionId: String, screenshotPath: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            val user = repository.getUserByEmail(profile.email)
            if (user != null) {
                val updatedUser = user.copy(
                    paymentTxnId = transactionId,
                    paymentScreenshot = screenshotPath,
                    isPending = true,
                    isRejected = false
                )
                repository.registerUser(updatedUser)
                
                val request = PaymentRequest(
                    courseId = -1,
                    courseTitle = "Registration Fee",
                    userEmail = profile.email,
                    userName = profile.name,
                    transactionId = transactionId,
                    amount = 29.0,
                    upiIdUsed = adminSettings.value.upiId,
                    screenshotPath = screenshotPath
                )
                repository.submitPaymentRequest(request)
                
                _userProfile.value = profile.copy(
                    isPending = true,
                    isRejected = false,
                    rejectionReason = ""
                )
                
                repository.sendNotification(
                    "Registration Fee Submitted 💳",
                    "We have received your verification request for UTR: $transactionId. Our administrators are validating it."
                )
            } else {
                // If the user profile isn't in DB (e.g. google login or mock), insert it as pending!
                val newUser = UserAccount(
                    email = profile.email,
                    firstName = profile.name.substringBefore(" "),
                    lastName = profile.name.substringAfter(" ", ""),
                    mobileNumber = profile.mobileNumber.ifEmpty { "9876543210" },
                    password = "google_user_pass",
                    profilePhotoUri = profile.profilePhotoUri,
                    address = profile.address,
                    district = profile.district,
                    state = profile.state,
                    pinCode = profile.pinCode,
                    isApproved = false,
                    isPending = true,
                    isRejected = false,
                    paymentTxnId = transactionId,
                    paymentScreenshot = screenshotPath
                )
                repository.registerUser(newUser)
                
                val request = PaymentRequest(
                    courseId = -1,
                    courseTitle = "Registration Fee",
                    userEmail = profile.email,
                    userName = profile.name,
                    transactionId = transactionId,
                    amount = 29.0,
                    upiIdUsed = adminSettings.value.upiId,
                    screenshotPath = screenshotPath
                )
                repository.submitPaymentRequest(request)
                
                _userProfile.value = profile.copy(
                    isPending = true,
                    isRejected = false,
                    rejectionReason = ""
                )
            }
        }
    }

    fun forgotPassword(emailOrMobile: String, newPasswordEntered: String): String? {
        val users = allUsers.value
        val foundUser = users.find { 
            it.email.equals(emailOrMobile.trim(), ignoreCase = true) || it.mobileNumber == emailOrMobile.trim() 
        }
        if (foundUser == null) {
            return "No registered user found with this email/mobile number."
        }
        viewModelScope.launch {
            val updatedUser = foundUser.copy(password = newPasswordEntered)
            repository.registerUser(updatedUser)
            repository.sendNotification(
                "Password Restored 🔑",
                "Your password has been successfully reset. Please log in with your new password."
            )
        }
        return null
    }

    fun logout() {
        _userProfile.value = UserProfile("", "", isLoggedIn = false)
    }

    fun selfApproveUser(email: String) {
        viewModelScope.launch {
            val regNumber = "TD-2026-REG-${(1000..9999).random()}"
            repository.updateUserStatus(
                email = email,
                approved = true,
                pending = false,
                rejected = false,
                reason = "",
                regNum = regNumber
            )
            _userProfile.value = userProfile.value.copy(
                isApproved = true,
                isPending = false,
                isRejected = false,
                registrationNumber = regNumber
            )
            repository.sendNotification("Account Self-Approved! 🎓", "Your account has been instantly approved in demo simulation mode.")
        }
    }

    fun selectCourse(id: Int?) {
        _focusedCourseId.value = id
        // Reset player index when entering a new course
        activeLectureIndex.value = 0
        playbackPositionMs.value = 0L
        isPlaying.value = false
        _couponMessage.value = null
        _purchaseSuccess.value = false
    }

    // Student interactions
    fun toggleBookmark(courseId: Int) {
        viewModelScope.launch {
            val isCurrentlyBookmarked = progressList.value.find { it.courseId == courseId }?.isBookmarked ?: false
            repository.toggleBookmark(courseId, !isCurrentlyBookmarked)
            repository.sendNotification(
                if (!isCurrentlyBookmarked) "Course Bookmarked 🔖" else "Bookmark Removed",
                "You have updated bookmarks for course id: $courseId"
            )
        }
    }

    fun toggleWishlist(courseId: Int) {
        viewModelScope.launch {
            val isCurrentlyWishlisted = progressList.value.find { it.courseId == courseId }?.isWishlisted ?: false
            repository.toggleWishlist(courseId, !isCurrentlyWishlisted)
        }
    }

    fun toggleDownload(courseId: Int) {
        viewModelScope.launch {
            val isCurrentlyDownloaded = progressList.value.find { it.courseId == courseId }?.isDownloaded ?: false
            repository.toggleDownload(courseId, !isCurrentlyDownloaded)
            if (!isCurrentlyDownloaded) {
                repository.sendNotification("Download Started 📥", "Downloading high-definition videos for offline learning.")
            }
        }
    }

    fun applyPromo(code: String, price: Double): Double {
        val discount = adminSettings.value.discountPercent
        val expectedCode = adminSettings.value.couponCode
        if (code.equals(expectedCode, ignoreCase = true)) {
            _couponMessage.value = "Promo Code Applied! Save $discount%!"
            return price * (1.0 - (discount / 100.0))
        } else {
            _couponMessage.value = "Invalid Promo Code."
            return price
        }
    }

    fun completePurchase(courseId: Int, paymentMethod: String) {
        viewModelScope.launch {
            repository.purchaseCourse(courseId)
            _purchaseSuccess.value = true
            
            // Increment total purchases revenue in admin settings
            val currentSettings = adminSettings.value
            val course = courses.value.find { it.id == courseId }
            val coursePrice = course?.price ?: 0.0
            val updatedSettings = currentSettings.copy(
                totalPurchasesRevenue = currentSettings.totalPurchasesRevenue + coursePrice,
                totalActiveUsers = currentSettings.totalActiveUsers + 1
            )
            repository.saveAdminSettings(updatedSettings)

            repository.sendNotification(
                "Purchase Successful! 🎉",
                "Successfully purchased \"${course?.title ?: "Premium Course"}\" using $paymentMethod. Happy Learning!"
            )
        }
    }

    fun clearPurchaseSuccess() {
        _purchaseSuccess.value = false
    }

    // Video notes
    fun addNote(courseId: Int, noteText: String) {
        if (noteText.trim().isEmpty()) return
        viewModelScope.launch {
            val note = CourseNote(
                courseId = courseId,
                lectureIndex = activeLectureIndex.value,
                noteText = noteText,
                positionMs = playbackPositionMs.value
            )
            repository.saveNote(note)
        }
    }

    fun deleteNote(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    // Watch progress updates
    fun selectLecture(index: Int) {
        activeLectureIndex.value = index
        playbackPositionMs.value = 0L
        isPlaying.value = false
    }

    fun triggerPlayPause() {
        isPlaying.value = !isPlaying.value
    }

    fun updateVideoPosition(positionMs: Long) {
        playbackPositionMs.value = positionMs
    }

    fun toggleLectureCompletion(courseId: Int, lectureIndex: Int) {
        viewModelScope.launch {
            val course = courses.value.find { it.id == courseId } ?: return@launch
            val lectures = CourseRepository.parseLecturesJson(course.lecturesJson)
            val progress = progressList.value.find { it.courseId == courseId }
            
            val currentCsv = progress?.completedLecturesCsv ?: ""
            val list = if (currentCsv.isEmpty()) mutableListOf() else currentCsv.split(",").filter { it.isNotEmpty() }.toMutableList()
            
            if (list.contains(lectureIndex.toString())) {
                list.remove(lectureIndex.toString())
            } else {
                list.add(lectureIndex.toString())
            }
            
            val newCsv = list.joinToString(",")
            val percent = if (lectures.isEmpty()) 0 else (list.size * 100) / lectures.size

            repository.updateWatchProgress(
                courseId = courseId,
                lectureIndex = lectureIndex,
                positionMs = 0,
                percentCompleted = percent,
                completedLecturesCsv = newCsv
            )

            if (percent == 100) {
                repository.sendNotification(
                    "Course Completed! 🎓🏆",
                    "Congratulations! You completed all lessons of \"${course.title}\". Download your certification on your profile!"
                )
            }
        }
    }

    fun completeLecture(courseId: Int, lectureIndex: Int) {
        viewModelScope.launch {
            val course = courses.value.find { it.id == courseId } ?: return@launch
            val lectures = CourseRepository.parseLecturesJson(course.lecturesJson)
            val progress = progressList.value.find { it.courseId == courseId }
            
            val currentCsv = progress?.completedLecturesCsv ?: ""
            val list = if (currentCsv.isEmpty()) mutableListOf() else currentCsv.split(",").toMutableList()
            
            if (!list.contains(lectureIndex.toString())) {
                list.add(lectureIndex.toString())
            }
            
            val newCsv = list.joinToString(",")
            val percent = if (lectures.isEmpty()) 0 else (list.size * 100) / lectures.size

            repository.updateWatchProgress(
                courseId = courseId,
                lectureIndex = lectureIndex,
                positionMs = 0,
                percentCompleted = percent,
                completedLecturesCsv = newCsv
            )

            if (percent == 100) {
                repository.sendNotification(
                    "Course Completed! 🎓🏆",
                    "Congratulations! You completed all lessons of \"${course.title}\". Download your certification on your profile!"
                )
            }
        }
    }

    // Admin commands
    fun updateAdminAdSettings(enabled: Boolean, position: String, bannerId: String, interstitialId: String) {
        viewModelScope.launch {
            val current = adminSettings.value
            val updated = current.copy(
                adsEnabled = enabled,
                adPosition = position,
                admobBannerId = bannerId,
                admobInterstitialId = interstitialId
            )
            repository.saveAdminSettings(updated)
            repository.sendNotification("Admin Ad Config Updated ⚙️", "Banner position changed to $position.")
        }
    }

    fun updateCoupon(code: String, percent: Int) {
        viewModelScope.launch {
            val current = adminSettings.value
            val updated = current.copy(
                couponCode = code,
                discountPercent = percent
            )
            repository.saveAdminSettings(updated)
            repository.sendNotification("Coupon Settings Updated 🏷️", "New Coupon Code: $code ($percent% Off)")
        }
    }

    fun submitPaymentRequest(courseId: Int, courseTitle: String, transactionId: String, amount: Double, upiIdUsed: String, screenshotBase64: String) {
        viewModelScope.launch {
            val profile = userProfile.value
            val request = PaymentRequest(
                courseId = courseId,
                courseTitle = courseTitle,
                userEmail = profile.email,
                userName = profile.name,
                transactionId = transactionId,
                amount = amount,
                upiIdUsed = upiIdUsed,
                screenshotPath = screenshotBase64
            )
            repository.submitPaymentRequest(request)
            repository.sendNotification(
                "Payment Submitted ⏳",
                "Your payment of ${adminSettings.value.currency} ${amount} for \"$courseTitle\" is submitted for verification (UTR/Txn: $transactionId)."
            )
        }
    }

    fun approvePaymentRequest(id: Int, courseId: Int, courseTitle: String) {
        viewModelScope.launch {
            if (courseId == -1) {
                val req = paymentRequests.value.find { it.id == id }
                if (req != null) {
                    val email = req.userEmail
                    val regNumber = "TD-2026-REG-${(1000..9999).random()}"
                    repository.updateUserStatus(
                        email = email,
                        approved = true,
                        pending = false,
                        rejected = false,
                        reason = "",
                        regNum = regNumber
                    )
                    repository.approvePaymentRequest(id, courseId)
                    repository.sendNotification(
                        "Registration Approved! 🎓",
                        "Welcome to the inner circle! Your registration payment is approved. Your Registration Number is $regNumber. Start learning now!"
                    )
                    
                    if (userProfile.value.email.equals(email, ignoreCase = true)) {
                        _userProfile.value = userProfile.value.copy(
                            isApproved = true,
                            isPending = false,
                            isRejected = false,
                            registrationNumber = regNumber
                        )
                    }
                }
            } else {
                repository.approvePaymentRequest(id, courseId)
                val currentSettings = adminSettings.value
                val updatedSettings = currentSettings.copy(
                    totalPurchasesRevenue = currentSettings.totalPurchasesRevenue + currentSettings.coursePrice,
                    totalActiveUsers = currentSettings.totalActiveUsers + 1
                )
                repository.saveAdminSettings(updatedSettings)
                repository.sendNotification(
                    "Payment Approved! 🎉",
                    "Payment of ${currentSettings.currency} ${currentSettings.coursePrice} for \"$courseTitle\" is verified. Your course is unlocked!"
                )
            }
        }
    }

    fun rejectPaymentRequest(id: Int, reason: String, courseTitle: String) {
        viewModelScope.launch {
            val req = paymentRequests.value.find { it.id == id }
            if (req != null && req.courseId == -1) {
                val email = req.userEmail
                repository.updateUserStatus(
                    email = email,
                    approved = false,
                    pending = false,
                    rejected = true,
                    reason = reason,
                    regNum = ""
                )
                repository.rejectPaymentRequest(id, reason)
                repository.sendNotification(
                    "Registration Payment Rejected ❌",
                    "We could not verify your registration payment. Reason: $reason. Please re-submit in settings or signup screen."
                )
                
                if (userProfile.value.email.equals(email, ignoreCase = true)) {
                    _userProfile.value = userProfile.value.copy(
                        isApproved = false,
                        isPending = false,
                        isRejected = true,
                        rejectionReason = reason
                    )
                }
            } else {
                repository.rejectPaymentRequest(id, reason)
                repository.sendNotification(
                    "Payment Rejected ❌",
                    "Payment verification failed for \"$courseTitle\". Reason: $reason"
                )
            }
        }
    }

    fun updatePaymentSettings(price: Double, currency: String, upiId: String, qrCodeBase64: String) {
        viewModelScope.launch {
            val current = adminSettings.value
            val updated = current.copy(
                coursePrice = price,
                currency = currency,
                upiId = upiId,
                qrCodeUrlOrPath = qrCodeBase64
            )
            repository.saveAdminSettings(updated)
            repository.sendNotification("Pricing Settings Updated ⚙️", "Premium course price set to $price $currency.")
        }
    }

    fun addNewCourse(
        title: String,
        description: String,
        instructor: String,
        category: String,
        price: Double,
        duration: String,
        lectures: List<Lecture>
    ) {
        viewModelScope.launch {
            val newId = (courses.value.maxOfOrNull { it.id } ?: 0) + 1
            val json = CourseRepository.makeLecturesJson(lectures)
            val newCourse = Course(
                id = newId,
                title = title,
                description = description,
                instructor = instructor,
                category = category,
                duration = duration,
                rating = 5.0,
                reviewsCount = 1,
                price = price,
                isNew = true,
                lecturesJson = json
            )
            repository.addCourse(newCourse)
            
            // If free, auto-unlock progress
            if (price == 0.0) {
                repository.purchaseCourse(newId)
            }

            repository.sendNotification("New Course Published! 📚", "\"$title\" is now available in category \"$category\".")
        }
    }

    fun deleteCourse(id: Int) {
        viewModelScope.launch {
            val course = courses.value.find { it.id == id }
            repository.deleteCourse(id)
            repository.sendNotification("Course Removed 🗑️", "\"${course?.title ?: "Course"}\" deleted successfully.")
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }

    fun addManualNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.sendNotification(title, message)
        }
    }
}
