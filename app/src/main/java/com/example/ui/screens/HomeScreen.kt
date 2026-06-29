package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Course
import com.example.data.UserProgress
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LearningViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToCourseDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userProfile.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val progressList by viewModel.progressList.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var activeBannerIndex by remember { mutableStateOf(0) }
    var newsletterEmail by remember { mutableStateOf("") }
    var newsletterSubscribed by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val unreadNotifications = notifications.filter { !it.isRead }

    // Categories
    val categories = listOf(
        "Instagram Mastery",
        "YouTube Mastery",
        "Facebook Growth",
        "AI Tools",
        "Video Editing",
        "Thumbnail Design",
        "Freelancing & Online Business",
        "Digital Marketing",
        "Affiliate Marketing"
    )

    // Banners
    val bannerContents = listOf(
        Triple(
            "Learn Digital Skills & Grow Your Income 🚀",
            "Professional video courses for creators, freelancers, and entrepreneurs.",
            "Start Learning"
        ),
        Triple(
            "Gemini AI & Google AI Studio Tutorial 🤖",
            "Unleash LLMs, design prompts, and automate daily creative tasks.",
            "Explore AI"
        ),
        Triple(
            "Instagram Reels Masterclass 2026 📈",
            "Learn monetization, reel strategies, and grow 100k followers fast.",
            "Boost Growth"
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(RoyalPurple, TechBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (user.isLoggedIn) user.name.take(1).uppercase() else "G",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (user.isLoggedIn) "Hello, ${user.name}!" else "Hello, Guest!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Let's learn digital skills today",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Notification Icon with Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateCard)
                        .clickable {
                            showNotificationDialog = true
                            viewModel.markAllNotificationsRead()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotifications.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(GrowthGreen)
                        )
                    }
                }
            }

            // Outer Scrollable Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Ad position check
                if (settings.adsEnabled && settings.adPosition == "Top Banner") {
                    AdMobBanner(
                        positionName = "Top",
                        adsEnabled = settings.adsEnabled,
                        adUnitId = settings.admobBannerId
                    )
                }

                // Banner Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(SlateCard, SlateDark)
                            )
                        )
                        .border(1.dp, SlateMuted.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    val currentBanner = bannerContents[activeBannerIndex]
                    
                    // Banner background effects
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(RoyalPurple.copy(alpha = 0.25f), Color.Transparent),
                                radius = 250.dp.toPx()
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.2f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = currentBanner.first,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentBanner.second,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (activeBannerIndex == 0) {
                                        // YouTube free blueprint
                                        onNavigateToCourseDetail(2)
                                    } else if (activeBannerIndex == 1) {
                                        // Gemini course
                                        onNavigateToCourseDetail(4)
                                    } else {
                                        // Instagram reels
                                        onNavigateToCourseDetail(1)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = currentBanner.third,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Slide indicator circles
                            Row {
                                bannerContents.forEachIndexed { idx, _ ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (idx == activeBannerIndex) RoyalPurple else SlateMuted
                                            )
                                            .clickable { activeBannerIndex = idx }
                                    )
                                }
                            }
                        }
                    }
                }

                // Top Categories
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Categories",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TechBlue,
                            modifier = Modifier.clickable { onNavigateToSearch() }
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories) { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SlateCard)
                                    .border(1.dp, SlateMuted, RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.selectedCategory.value = cat
                                        onNavigateToSearch()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when {
                                        cat.contains("Instagram") -> Icons.Default.CameraAlt
                                        cat.contains("YouTube") -> Icons.Default.PlayArrow
                                        cat.contains("Facebook") -> Icons.Default.ThumbUp
                                        cat.contains("AI Tools") -> Icons.Default.AutoAwesome
                                        else -> Icons.Default.School
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = cat,
                                        tint = RoyalPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Middle Ad placement check
                if (settings.adsEnabled && settings.adPosition == "Feed Card") {
                    AdMobBanner(
                        positionName = "Middle Feed",
                        adsEnabled = settings.adsEnabled,
                        adUnitId = settings.admobBannerId
                    )
                }

                // Featured Courses (OTT Style Row)
                CourseRowSection(
                    title = "Featured Courses",
                    courses = courses.filter { it.isFeatured },
                    progressList = progressList,
                    onCourseClick = onNavigateToCourseDetail
                )

                // Popular Courses
                CourseRowSection(
                    title = "Popular Courses",
                    courses = courses.filter { it.isPopular },
                    progressList = progressList,
                    onCourseClick = onNavigateToCourseDetail
                )

                // Best Selling Courses
                CourseRowSection(
                    title = "Best Selling Courses",
                    courses = courses.filter { it.isBestSelling },
                    progressList = progressList,
                    onCourseClick = onNavigateToCourseDetail
                )

                // New Courses
                CourseRowSection(
                    title = "New Released",
                    courses = courses.filter { it.isNew },
                    progressList = progressList,
                    onCourseClick = onNavigateToCourseDetail
                )

                // Student Reviews
                Spacer(modifier = Modifier.height(24.dp))
                ReviewsSection()

                // FAQs
                Spacer(modifier = Modifier.height(24.dp))
                FaqSection()

                // Newsletter Signup
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(SlateCard, SlateDark)
                            )
                        )
                        .border(1.dp, RoyalPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Newsletter",
                            tint = RoyalPurple,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Subscribe to Our Newsletter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Get weekly coupon codes, digital income strategies, and YouTube secrets directly in your inbox.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        if (newsletterSubscribed) {
                            Text(
                                text = "Thank you! Subscribed successfully. 🎉",
                                color = GrowthGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = newsletterEmail,
                                onValueChange = { newsletterEmail = it },
                                placeholder = { Text("Enter your email", color = TextDisabled) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("newsletter_email_input")
                                    .height(50.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = SlateDark,
                                    unfocusedContainerColor = SlateDark,
                                    focusedBorderColor = TechBlue,
                                    unfocusedBorderColor = SlateMuted
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GradientButton(
                                text = "Subscribe Now",
                                onClick = {
                                    if (newsletterEmail.contains("@")) {
                                        newsletterSubscribed = true
                                        viewModel.addManualNotification(
                                            "Newsletter Subscribed! 📧",
                                            "You have successfully subscribed with $newsletterEmail. Use coupon code ${settings.couponCode} to save ${settings.discountPercent}%!"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTagStr = "submit_newsletter_button"
                            )
                        }
                    }
                }

                // Bottom Ad placement check
                if (settings.adsEnabled && settings.adPosition == "Bottom Banner") {
                    AdMobBanner(
                        positionName = "Bottom",
                        adsEnabled = settings.adsEnabled,
                        adUnitId = settings.admobBannerId
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Notification Dialog Popup
        if (showNotificationDialog) {
            Dialog(onDismissRequest = { showNotificationDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .border(1.dp, SlateMuted, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Inbox Notifications",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = { showNotificationDialog = false }) {
                                Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        if (notifications.isEmpty()) {
                            Text(
                                text = "No notifications yet.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                notifications.forEach { notif ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateDark)
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (notif.isRead) TextDisabled else GrowthGreen)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = notif.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Text(
                                                text = notif.message,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        GradientButton(
                            text = "Done",
                            onClick = { showNotificationDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseRowSection(
    title: String,
    courses: List<Course>,
    progressList: List<UserProgress>,
    onCourseClick: (Int) -> Unit
) {
    if (courses.isEmpty()) return

    Column(modifier = Modifier.padding(top = 18.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(courses) { course ->
                val progress = progressList.find { it.courseId == course.id }
                
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .testTag("course_item_${course.id}")
                        .clickable { onCourseClick(course.id) },
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        // Card header (Mock thumbnail image with gradient backing)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(RoyalPurple.copy(alpha = 0.8f), TechBlue.copy(alpha = 0.8f))
                                    )
                                )
                        ) {
                            // Category Badge
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SlateDark.copy(alpha = 0.75f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = course.category,
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Price Tag Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (course.price == 0.0) GrowthGreen else TechBlue)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (course.price == 0.0) "FREE" else "$${course.price}",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Graduation Cap icon overlay
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center)
                            )
                        }

                        // Details
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = course.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "By ${course.instructor}",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = GoldStar,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${course.rating}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Duration",
                                        tint = TextDisabled,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = course.duration,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Dynamic watch progress bar
                            if (progress != null && progress.percentCompleted > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Completed", fontSize = 9.sp, color = TextSecondary)
                                        Text("${progress.percentCompleted}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress.percentCompleted / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = GrowthGreen,
                                        trackColor = SlateMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewsSection() {
    val reviews = listOf(
        Pair("Aarav Sharma", "Made $500 in my first month of Instagram Reels! The hooks and editing strategies are gold! ⭐⭐⭐⭐⭐"),
        Pair("Pooja Patel", "This is like Netflix for learning digital skills. Completed Gemini AI and launched my copywriting agency. ⭐⭐⭐⭐⭐"),
        Pair("Nikhil K.", "The YouTube SEO blueprint unlocked everything. My channel hit 10k subscribers inside 60 days. Truly premium! ⭐⭐⭐⭐⭐")
    )
    var currentReviewIdx by remember { mutableStateOf(0) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "What Students Say",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val activeReview = reviews[currentReviewIdx]
                Text(
                    text = "\"${activeReview.second}\"",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "- ${activeReview.first}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TechBlue
                    )

                    // Navigation buttons
                    Row {
                        IconButton(
                            onClick = {
                                currentReviewIdx = if (currentReviewIdx == 0) reviews.lastIndex else currentReviewIdx - 1
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Prev", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                currentReviewIdx = if (currentReviewIdx == reviews.lastIndex) 0 else currentReviewIdx + 1
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, "Next", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FaqSection() {
    val faqs = listOf(
        Pair("Are the completion certificates official?", "Yes! Once you complete 100% of any course, our platform issues a verified completion certificate with your custom student ID, printable and directly shareable to LinkedIn."),
        Pair("How do I watch courses offline?", "Simply tap the download icon on the course page. It will download the lessons locally in HD, allowing you to learn on the go, completely without internet."),
        Pair("What payments are supported?", "We fully support all major localized payment options including instant UPI (Google Pay, Paytm, PhonePe), secure global Cards via Stripe, and PayPal for overseas students.")
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Frequently Asked Questions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        faqs.forEach { faq ->
            var expanded by remember { mutableStateOf(false) }
            val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = faq.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = RoyalPurple,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = faq.second,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
