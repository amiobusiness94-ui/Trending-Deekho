package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel

@Composable
fun MainAppContainer(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    val userState by viewModel.userProfile.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()

    // Screen navigation state
    var currentScreen by remember { mutableStateOf("Dashboard") } // "Dashboard", "Explore", "Profile", "Detail", "Player", "Admin"
    var lastScreenBeforeDetail by remember { mutableStateOf("Dashboard") }
    var activeCourseIdDetail by remember { mutableStateOf<Int?>(null) }

    // If the user isn't logged in, force the LoginScreen
    if (!userState.isLoggedIn) {
        LoginScreen(viewModel = viewModel, modifier = modifier)
        return
    }

    // Awaiting Administrator Registration Approval Blocker Screen
    if (!userState.isApproved && userState.role == "Student") {
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
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glow Halo Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .border(3.dp, if (userState.isRejected) CrimsonRed else RoyalPurple, CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (userState.isRejected) CrimsonRed.copy(alpha = 0.2f) else RoyalPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userState.profilePhotoUri.isNotEmpty()) userState.profilePhotoUri.take(2) else "🎓",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (userState.isRejected) "Verification Rejected ❌" else "Verification Pending ⏳",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (userState.isRejected) 
                        "Your registration fee payment of ₹29 was rejected by the administrator."
                        else "Your registration is currently under administrative manual review.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Rejection Reason Alert Card
                if (userState.isRejected && userState.rejectionReason.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, "Error", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reason for Rejection:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                            Text(
                                text = userState.rejectionReason,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Student Profile Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    backgroundColor = SlateCard.copy(alpha = 0.8f)
                ) {
                    Text("Student Registered Info", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechBlue, modifier = Modifier.padding(bottom = 12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Name:", fontSize = 11.sp, color = TextSecondary)
                        Text(userState.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email:", fontSize = 11.sp, color = TextSecondary)
                        Text(userState.email, fontSize = 11.sp, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mobile:", fontSize = 11.sp, color = TextSecondary)
                        Text(userState.mobileNumber.ifEmpty { "N/A" }, fontSize = 11.sp, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Address:", fontSize = 11.sp, color = TextSecondary)
                        Text("${userState.district}, ${userState.state}", fontSize = 11.sp, color = Color.White)
                    }
                }

                // If rejected, show interactive resubmit panel!
                if (userState.isRejected) {
                    var newTxn by remember { mutableStateOf("") }
                    var newScreenshot by remember { mutableStateOf("GPay_success_9182.jpg") }
                    
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        backgroundColor = SlateCard.copy(alpha = 0.5f)
                    ) {
                        Text("Resubmit Registration Payment (₹29)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
                        
                        OutlinedTextField(
                            value = newTxn,
                            onValueChange = { newTxn = it },
                            label = { Text("New Transaction ID / UTR") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        
                        Text("Selected screenshot: $newScreenshot", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
                        
                        GradientButton(
                            text = "Resubmit for Approval",
                            onClick = {
                                if (newTxn.trim().length < 6) {
                                    // simple validation
                                } else {
                                    viewModel.submitRegistrationPayment(newTxn.trim(), newScreenshot)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Under Review Message Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = RoyalPurple.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, RoyalPurple.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WatchLater, "Review", tint = RoyalPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Verification takes about 2-5 minutes. Once our admin verifies your transaction screenshot, your full access unlocks.",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Simulation Control Buttons (Prevent Dead Ends!)
                GradientButton(
                    text = "Demo: Instant Self-Approve ⚡",
                    onClick = {
                        viewModel.selfApproveUser(userState.email)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_self_approve_btn")
                        .padding(bottom = 12.dp)
                )

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, SlateMuted),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, "Sign Out", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out / Switch Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main Active Screen Router View
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentScreen) {
                    "Dashboard" -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToSearch = { currentScreen = "Explore" },
                            onNavigateToCourseDetail = { id ->
                                activeCourseIdDetail = id
                                lastScreenBeforeDetail = "Dashboard"
                                currentScreen = "Detail"
                            }
                        )
                    }

                    "Explore" -> {
                        SearchScreen(
                            viewModel = viewModel,
                            onNavigateToCourseDetail = { id ->
                                activeCourseIdDetail = id
                                lastScreenBeforeDetail = "Explore"
                                currentScreen = "Detail"
                            }
                        )
                    }

                    "Profile" -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onNavigateToCourseDetail = { id ->
                                activeCourseIdDetail = id
                                lastScreenBeforeDetail = "Profile"
                                currentScreen = "Detail"
                            },
                            onEnterAdminMode = { currentScreen = "Admin" }
                        )
                    }

                    "Detail" -> {
                        activeCourseIdDetail?.let { cid ->
                            CourseDetailScreen(
                                viewModel = viewModel,
                                courseId = cid,
                                onNavigateToPlayer = { currentScreen = "Player" },
                                onBack = { currentScreen = lastScreenBeforeDetail }
                            )
                        }
                    }

                    "Player" -> {
                        VideoPlayerScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "Detail" }
                        )
                    }

                    "Admin" -> {
                        AdminPanelScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "Profile" }
                        )
                    }
                }
            }

            // Bottom Navigation Bar - only show on root tabs (Dashboard, Explore, Profile)
            val showBottomBar = currentScreen == "Dashboard" || currentScreen == "Explore" || currentScreen == "Profile"
            if (showBottomBar) {
                // Persistent AdMob bottom banner check
                if (settings.adsEnabled && settings.adPosition == "Bottom Banner") {
                    AdMobBanner(
                        positionName = "Container Bottom",
                        adsEnabled = settings.adsEnabled,
                        adUnitId = settings.admobBannerId
                    )
                }

                NavigationBar(
                    containerColor = SlateCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(72.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "Dashboard",
                        onClick = { currentScreen = "Dashboard" },
                        icon = { Icon(Icons.Default.Home, "Dashboard") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RoyalPurple,
                            indicatorColor = RoyalPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "Explore",
                        onClick = { currentScreen = "Explore" },
                        icon = { Icon(Icons.Default.Explore, "Explore") },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RoyalPurple,
                            indicatorColor = RoyalPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_explore")
                    )

                    NavigationBarItem(
                        selected = currentScreen == "Profile",
                        onClick = { currentScreen = "Profile" },
                        icon = { Icon(Icons.Default.Person, "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RoyalPurple,
                            indicatorColor = RoyalPurple,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        }
    }
}
