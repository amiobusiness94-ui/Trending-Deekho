package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Course
import com.example.data.UserProgress
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LearningViewModel,
    onNavigateToCourseDetail: (Int) -> Unit,
    onEnterAdminMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userProfile.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val progressList by viewModel.progressList.collectAsState()

    var activeSubTab by remember { mutableStateOf("Bookmarks") } // "Bookmarks", "Wishlist", "Downloads"
    var showCertificateForCourse by remember { mutableStateOf<Course?>(null) }

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
            // Title Header
            Text(
                text = "My Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // User Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
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
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (user.isLoggedIn) user.name else "Guest Learner",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (user.isLoggedIn) user.email else "guest@trendingdeekho.com",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (user.isGoogleLogin) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TechBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Linked via Google",
                                            fontSize = 9.sp,
                                            color = TechBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Logout button
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_profile_button")
                        ) {
                            Icon(Icons.Default.ExitToApp, "Logout", tint = Color.Red)
                        }
                    }
                }

                // Certificate Center
                Text(
                    text = "My Certifications Center 🎓",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val completedCourseProgress = progressList.filter { it.percentCompleted == 100 }
                if (completedCourseProgress.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateCard.copy(alpha = 0.4f))
                            .border(1.dp, SlateMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.WorkspacePremium, "Premium", tint = TextDisabled, modifier = Modifier.size(36.dp))
                            Text(
                                text = "No Certificates Unlocked Yet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Complete 100% of any course curriculum (by watching lectures and clicking mark complete) to download your printable, official credential here.",
                                fontSize = 10.sp,
                                color = TextDisabled,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        completedCourseProgress.forEach { prog ->
                            val c = courses.find { it.id == prog.courseId }
                            if (c != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("certificate_row_${c.id}")
                                        .border(1.dp, GoldStar.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Completed",
                                                tint = GoldStar,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = c.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Completed on June 29, 2026",
                                                    fontSize = 9.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { showCertificateForCourse = c },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldStar),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .testTag("view_certificate_button_${c.id}")
                                                .height(28.dp)
                                        ) {
                                            Text("View", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Progress Library Tabs switcher
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileSubTabItem("Bookmarks", activeSubTab == "Bookmarks", Modifier.weight(1f)) { activeSubTab = "Bookmarks" }
                    ProfileSubTabItem("Wishlist", activeSubTab == "Wishlist", Modifier.weight(1f)) { activeSubTab = "Wishlist" }
                    ProfileSubTabItem("Downloads", activeSubTab == "Downloads", Modifier.weight(1f)) { activeSubTab = "Downloads" }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List filter based on chosen tab
                val targetCourses = when (activeSubTab) {
                    "Bookmarks" -> {
                        val ids = progressList.filter { it.isBookmarked }.map { it.courseId }
                        courses.filter { ids.contains(it.id) }
                    }
                    "Wishlist" -> {
                        val ids = progressList.filter { it.isWishlisted }.map { it.courseId }
                        courses.filter { ids.contains(it.id) }
                    }
                    else -> {
                        val ids = progressList.filter { it.isDownloaded }.map { it.courseId }
                        courses.filter { ids.contains(it.id) }
                    }
                }

                if (targetCourses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No courses added in $activeSubTab.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        targetCourses.forEach { tc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateCard.copy(alpha = 0.5f))
                                    .clickable { onNavigateToCourseDetail(tc.id) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(RoyalPurple.copy(alpha = 0.6f), TechBlue.copy(alpha = 0.6f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tc.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tc.category,
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // SECRET Portal: Open Admin console controls
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalPurple.copy(alpha = 0.15f))
                        .border(1.dp, RoyalPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onEnterAdminMode)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = RoyalPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Developer & Creator Studio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Manage courses, customize coupon settings, and configure AdMob ads.",
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = RoyalPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Show Certificate overlay if toggled
        showCertificateForCourse?.let { c ->
            CertificateDialog(
                user = user,
                courseTitle = c.title,
                onDismiss = { showCertificateForCourse = null }
            )
        }
    }
}

@Composable
fun ProfileSubTabItem(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) TechBlue.copy(alpha = 0.15f) else SlateCard)
            .border(1.dp, if (isSelected) TechBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TechBlue else TextSecondary
        )
    }
}
