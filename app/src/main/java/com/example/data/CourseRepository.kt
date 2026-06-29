package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class CourseRepository(private val db: AppDatabase) {

    val allCourses: Flow<List<Course>> = db.courseDao().getAllCourses()
    val allProgress: Flow<List<UserProgress>> = db.userProgressDao().getAllProgress()
    val adminSettings: Flow<AdminSettings?> = db.adminSettingsDao().getSettings()
    val allNotifications: Flow<List<AppNotification>> = db.notificationDao().getAllNotifications()
    val allPaymentRequests: Flow<List<PaymentRequest>> = db.paymentRequestDao().getAllRequests()

    fun getCourseById(courseId: Int): Flow<Course?> = db.courseDao().getCourseById(courseId)
    
    fun getProgressForCourse(courseId: Int): Flow<UserProgress?> = 
        db.userProgressDao().getProgressForCourse(courseId)

    fun getPaymentRequestsByEmail(email: String): Flow<List<PaymentRequest>> = 
        db.paymentRequestDao().getRequestsByEmail(email)

    suspend fun submitPaymentRequest(request: PaymentRequest) {
        db.paymentRequestDao().insertRequest(request)
    }

    suspend fun approvePaymentRequest(id: Int, courseId: Int) {
        db.paymentRequestDao().updateRequestStatus(id, "APPROVED", "")
        purchaseCourse(courseId)
    }

    suspend fun rejectPaymentRequest(id: Int, reason: String) {
        db.paymentRequestDao().updateRequestStatus(id, "REJECTED", reason)
    }

    suspend fun deletePaymentRequest(id: Int) = db.paymentRequestDao().deleteRequest(id)

    fun getNotesForCourse(courseId: Int): Flow<List<CourseNote>> = 
        db.courseNoteDao().getNotesForCourse(courseId)

    suspend fun saveNote(note: CourseNote) = db.courseNoteDao().insertNote(note)

    suspend fun deleteNote(id: Int) = db.courseNoteDao().deleteNoteById(id)

    suspend fun toggleBookmark(courseId: Int, isBookmarked: Boolean) {
        val progress = db.userProgressDao().getProgressForCourse(courseId).firstOrNull()
        if (progress == null) {
            db.userProgressDao().insertOrUpdateProgress(
                UserProgress(courseId = courseId, isBookmarked = isBookmarked)
            )
        } else {
            db.userProgressDao().updateBookmarked(courseId, isBookmarked)
        }
    }

    suspend fun toggleWishlist(courseId: Int, isWishlisted: Boolean) {
        val progress = db.userProgressDao().getProgressForCourse(courseId).firstOrNull()
        if (progress == null) {
            db.userProgressDao().insertOrUpdateProgress(
                UserProgress(courseId = courseId, isWishlisted = isWishlisted)
            )
        } else {
            db.userProgressDao().updateWishlisted(courseId, isWishlisted)
        }
    }

    suspend fun toggleDownload(courseId: Int, isDownloaded: Boolean) {
        val progress = db.userProgressDao().getProgressForCourse(courseId).firstOrNull()
        if (progress == null) {
            db.userProgressDao().insertOrUpdateProgress(
                UserProgress(courseId = courseId, isDownloaded = isDownloaded)
            )
        } else {
            db.userProgressDao().updateDownloaded(courseId, isDownloaded)
        }
    }

    suspend fun purchaseCourse(courseId: Int) {
        val progress = db.userProgressDao().getProgressForCourse(courseId).firstOrNull()
        if (progress == null) {
            db.userProgressDao().insertOrUpdateProgress(
                UserProgress(courseId = courseId, isPurchased = true)
            )
        } else {
            db.userProgressDao().updatePurchased(courseId, true)
        }
    }

    suspend fun updateWatchProgress(
        courseId: Int,
        lectureIndex: Int,
        positionMs: Long,
        percentCompleted: Int,
        completedLecturesCsv: String
    ) {
        val progress = db.userProgressDao().getProgressForCourse(courseId).firstOrNull()
        val isPurchasedVal = progress?.isPurchased ?: false
        val isBookmarkedVal = progress?.isBookmarked ?: false
        val isWishlistedVal = progress?.isWishlisted ?: false
        val isDownloadedVal = progress?.isDownloaded ?: false

        db.userProgressDao().insertOrUpdateProgress(
            UserProgress(
                courseId = courseId,
                lastWatchedLectureIndex = lectureIndex,
                lastWatchedPositionMs = positionMs,
                percentCompleted = percentCompleted,
                isBookmarked = isBookmarkedVal,
                isWishlisted = isWishlistedVal,
                isDownloaded = isDownloadedVal,
                isPurchased = isPurchasedVal,
                completedLecturesCsv = completedLecturesCsv
            )
        )
    }

    suspend fun saveAdminSettings(settings: AdminSettings) =
        db.adminSettingsDao().insertOrUpdateSettings(settings)

    suspend fun addCourse(course: Course) = db.courseDao().insertCourse(course)

    suspend fun deleteCourse(id: Int) = db.courseDao().deleteCourseById(id)

    // User Account / Student Management functions
    val allUsers: Flow<List<UserAccount>> = db.userAccountDao().getAllUsers()

    suspend fun getUserByEmail(email: String): UserAccount? = db.userAccountDao().getUserByEmail(email)

    suspend fun getUserByEmailOrMobile(email: String, mobile: String): UserAccount? = 
        db.userAccountDao().getUserByEmailOrMobile(email, mobile)

    suspend fun registerUser(user: UserAccount) = db.userAccountDao().insertUser(user)

    suspend fun updateUserStatus(email: String, approved: Boolean, pending: Boolean, rejected: Boolean, reason: String, regNum: String) = 
        db.userAccountDao().updateUserStatus(email, approved, pending, rejected, reason, regNum)

    suspend fun deleteUser(email: String) = db.userAccountDao().deleteUser(email)

    suspend fun sendNotification(title: String, message: String) =
        db.notificationDao().insertNotification(AppNotification(title = title, message = message))

    suspend fun markNotificationsRead() = db.notificationDao().markAllAsRead()

    suspend fun seedDatabaseIfEmpty() {
        val coursesCount = db.courseDao().getAllCourses().firstOrNull()?.size ?: 0
        if (coursesCount == 0) {
            // Seed Admin Settings
            db.adminSettingsDao().insertOrUpdateSettings(AdminSettings())

            // Seed Notifications
            db.notificationDao().insertNotification(
                AppNotification(
                    title = "Welcome to Trending Deekho! 🎉",
                    message = "Start your digital skills learning journey today. Explore free and premium courses to grow your income!"
                )
            )
            db.notificationDao().insertNotification(
                AppNotification(
                    title = "Gemini AI Mastery Updated 🤖",
                    message = "We have added 4 new lectures to the AI tools category covering Google AI Studio and prompt engineering."
                )
            )

            // Seed Courses
            val seededCourses = listOf(
                Course(
                    id = 1,
                    title = "Instagram Reels Mastery 2026",
                    description = "Learn how to build a mass following on Instagram using optimized Reels strategy. Cover Reels monetization, editing techniques, posting schedules, and digital product sales through Instagram. Perfect for digital creators.",
                    instructor = "Amio Rahman (Insta Growth Lead)",
                    category = "Instagram Mastery",
                    duration = "6h 40m",
                    rating = 4.8,
                    reviewsCount = 312,
                    price = 29.0,
                    isFeatured = true,
                    isBestSelling = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. Introduction to Reels Algorithm", "12:15", "inst_lec_1", "Understand how the Reels algorithm ranks content in 2026 and how to triggers viral loops."),
                            Lecture("2. Hook Mastery & Copywriting", "15:45", "inst_lec_2", "Master the first 3 seconds of your reels to maximize retention and view duration."),
                            Lecture("3. Lighting & Gear on a Budget", "18:20", "inst_lec_3", "Learn how to capture ultra-crisp cinematic videos using just your smartphone."),
                            Lecture("4. Reel Editing in CapCut Pro", "25:10", "inst_lec_4", "Step-by-step editing tutorial for adding dynamic captions, cuts, and transitions."),
                            Lecture("5. Finding Trending Audio & Sounds", "10:30", "inst_lec_5", "Techniques to spot trending audio before they peak to ride the viral wave."),
                            Lecture("6. Content Planning & Scheduling", "20:15", "inst_lec_6", "Set up a highly efficient automated posting calendar using free scheduler tools."),
                            Lecture("7. Monetization: Reels Play & Brand Deals", "32:40", "inst_lec_7", "Unlock direct revenue streams and secure five-figure brand collaborations.")
                        )
                    )
                ),
                Course(
                    id = 2,
                    title = "YouTube Shorts & SEO Blueprint",
                    description = "A complete blueprint to build and scale a YouTube channel. Master YouTube SEO, high-retention editing, thumbnail design secrets, Shorts creation, and optimization for the USA audience to get 10x higher CPM earnings.",
                    instructor = "Devendra Pal (YT Creator - 2M+ Subs)",
                    category = "YouTube Mastery",
                    duration = "8h 15m",
                    rating = 4.9,
                    reviewsCount = 480,
                    price = 0.0, // Free Course
                    isPopular = true,
                    isFeatured = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. Channel Niche Selection in 2026", "14:20", "yt_lec_1", "How to pick a high-paying, evergreen niche with low competition."),
                            Lecture("2. Setting up SEO-Optimized Channel", "19:10", "yt_lec_2", "Configuring channel tags, descriptions, and advanced upload settings."),
                            Lecture("3. YouTube Algorithm: CTR & AVD", "22:45", "yt_lec_3", "Deep dive into Click-Through Rate and Average View Duration calculations."),
                            Lecture("4. Scriptwriting for High Retention", "17:30", "yt_lec_4", "Structuring educational or entertainment videos to keep viewers engaged."),
                            Lecture("5. Creating the Ultimate Clickable Thumbnail", "28:50", "yt_lec_5", "Step-by-step thumbnail design secrets utilizing high contrast and visual cues."),
                            Lecture("6. USA Audience Secrets: Target English SEO", "35:15", "yt_lec_6", "Get high CPM rates by targeting premium North American viewers successfully.")
                        )
                    )
                ),
                Course(
                    id = 3,
                    title = "Facebook Professional Mode Mastery",
                    description = "Unlock massive earnings using Facebook Professional Mode. Master reels monetization, fan subscriptions, in-stream ads, page recommendation setups, and engagement secrets to leverage the world's largest social platform.",
                    instructor = "Rajesh Kumar (FB Monetization Expert)",
                    category = "Facebook Growth",
                    duration = "5h 20m",
                    rating = 4.6,
                    reviewsCount = 184,
                    price = 29.0,
                    isNew = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. Switching to Professional Mode", "08:40", "fb_lec_1", "How to transform your personal profile into a monetization engine."),
                            Lecture("2. Facebook Recommendation Guidelines", "15:10", "fb_lec_2", "Avoid red flags and policy violations to keep your organic reach high."),
                            Lecture("3. Reels Play & Bonus Monetization", "18:35", "fb_lec_3", "Learn the official criteria and application process for FB bonus systems."),
                            Lecture("4. In-Stream Ads Eligibility Hacks", "22:10", "fb_lec_4", "Fulfill the 60,000 view minutes criteria quickly with legal viral strategies."),
                            Lecture("5. Community Engagement & Groups Strategy", "16:50", "fb_lec_5", "Use Facebook groups to drive initial organic views to your new videos.")
                        )
                    )
                ),
                Course(
                    id = 4,
                    title = "AI-Powered Creator with Gemini",
                    description = "Harness the power of cutting-edge AI tools to generate infinite content, automate scripting, draft designs, and code digital solutions. Features deep dives into ChatGPT, Google AI Studio, Canva AI, and Gemini API integrations.",
                    instructor = "Dr. Shreya Roy (AI Architect)",
                    category = "AI Tools",
                    duration = "4h 10m",
                    rating = 4.9,
                    reviewsCount = 295,
                    price = 29.0,
                    isFeatured = true,
                    isPopular = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. Prompt Engineering Fundamentals", "15:20", "ai_lec_1", "Learn the master formulas to get pinpoint precise answers from LLMs."),
                            Lecture("2. Custom GPTs & Google AI Studio Prompting", "21:40", "ai_lec_2", "Construct your own content assistant tools in AI Studio without coding."),
                            Lecture("3. Designing Assets with Canva AI", "18:10", "ai_lec_3", "Instantly produce hundreds of templates, social banners, and text-to-image assets."),
                            Lecture("4. Automating Content Scripting with Gemini", "24:35", "ai_lec_4", "Set up a content factory script utilizing Gemini API to generate reels concepts."),
                            Lecture("5. Bulk Video Creation for Faceless Channels", "30:15", "ai_lec_5", "The exact software combination to auto-produce 30 shorts in 10 minutes.")
                        )
                    )
                ),
                Course(
                    id = 5,
                    title = "Mobile Video Editing Masterclass",
                    description = "Learn how to edit high-quality commercial, narrative, and social media videos entirely on your phone. Cover CapCut, VN Editor, sound design, color grading, and speed ramping secrets to compete with desktop editors.",
                    instructor = "Kabir Singh (Senior Video Editor)",
                    category = "Video Editing",
                    duration = "7h 00m",
                    rating = 4.7,
                    reviewsCount = 215,
                    price = 0.0, // Free Course
                    isNew = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. CapCut Interface & Basic Cuts", "13:30", "ed_lec_1", "Get comfortable with timelines, splitting, layers, and basic keyframes."),
                            Lecture("2. Speed Ramping & Cinematic Timing", "19:40", "ed_lec_2", "Add extreme polish to your travel videos using smooth velocity curves."),
                            Lecture("3. Advanced Transitions & Sound Effects", "22:15", "ed_lec_3", "Pair whoosh, swoosh, and rise sound FX with matched visual cuts."),
                            Lecture("4. Color Grading on Smartphone screens", "15:50", "ed_lec_4", "Apply custom LUTs and manual HSL adjustments for rich cinematic colors."),
                            Lecture("5. Export Settings for TikTok & YouTube", "10:12", "ed_lec_5", "The secret bitrates and resolutions to stop Instagram from compressing your work.")
                        )
                    )
                ),
                Course(
                    id = 6,
                    title = "Modern Freelancing & Clients Landing",
                    description = "Step-by-step masterclass to launch an online freelancing business. Learn how to list high-income digital skills, design high-converting portfolios, negotiate premium packages, and close clients on Upwork, Fiverr, and cold email.",
                    instructor = "Nisha Patel (Upwork Top-Rated Plus)",
                    category = "Freelancing & Online Business",
                    duration = "10h 30m",
                    rating = 4.9,
                    reviewsCount = 540,
                    price = 29.0,
                    isBestSelling = true,
                    lecturesJson = makeLecturesJson(
                        listOf(
                            Lecture("1. High-Income Skills Selection", "18:40", "fr_lec_1", "Define what skills clients will pay $1,000+ a month to manage."),
                            Lecture("2. Fiverr Gig Optimization Secrets", "25:30", "fr_lec_2", "Keyword stuffing, visual branding, and package structures that convert."),
                            Lecture("3. Writing the Perfect Upwork Proposal", "22:10", "fr_lec_3", "Learn the 3-step proposal layout that gets a response rate over 60%."),
                            Lecture("4. Closing Clients on Video Calls", "30:45", "fr_lec_4", "Live sales call mockups, price negotiations, and handling objections."),
                            Lecture("5. Direct Outbound: Cold Email Blueprint", "28:15", "fr_lec_5", "Source decision-makers and draft non-spammy pitches that win accounts.")
                        )
                    )
                )
            )

            db.courseDao().insertCourses(seededCourses)
            
            // Auto unlock Free Courses for the user
            for (c in seededCourses) {
                if (c.price == 0.0) {
                    db.userProgressDao().insertOrUpdateProgress(
                        UserProgress(courseId = c.id, isPurchased = true)
                    )
                }
            }
        }
    }

    companion object {
        fun makeLecturesJson(lectures: List<Lecture>): String {
            val arr = JSONArray()
            for (l in lectures) {
                val obj = JSONObject()
                obj.put("title", l.title)
                obj.put("duration", l.duration)
                obj.put("videoUrl", l.videoUrl)
                obj.put("description", l.description)
                arr.put(obj)
            }
            return arr.toString()
        }

        fun parseLecturesJson(json: String): List<Lecture> {
            val list = mutableListOf<Lecture>()
            try {
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        Lecture(
                            title = obj.getString("title"),
                            duration = obj.getString("duration"),
                            videoUrl = obj.getString("videoUrl"),
                            description = obj.getString("description")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
