package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminSettings
import com.example.data.Course
import com.example.data.Lecture
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: LearningViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val courses by viewModel.courses.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()

    var activeAdminTab by remember { mutableStateOf("Analytics") } // "Analytics", "Ads & Coupon", "Course CRUD"

    // Form states for new course
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newInstructor by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("AI Tools") }
    var newPriceStr by remember { mutableStateOf("29.99") }
    var newDuration by remember { mutableStateOf("4h 30m") }
    var lecturesCountStr by remember { mutableStateOf("3") }

    // Form states for Ads & Coupons
    var adsEnabled by remember { mutableStateOf(settings.adsEnabled) }
    var adPosition by remember { mutableStateOf(settings.adPosition) }
    var bannerId by remember { mutableStateOf(settings.admobBannerId) }
    var interstitialId by remember { mutableStateOf(settings.admobInterstitialId) }
    var couponCode by remember { mutableStateOf(settings.couponCode) }
    var discountStr by remember { mutableStateOf(settings.discountPercent.toString()) }

    // Form states for Payment verification
    var upiIdConfig by remember { mutableStateOf(settings.upiId) }
    var coursePriceConfig by remember { mutableStateOf(settings.coursePrice.toString()) }
    var rejectionReasonConfig by remember { mutableStateOf("") }

    // Synchronize form values with database on entry
    LaunchedEffect(settings) {
        adsEnabled = settings.adsEnabled
        adPosition = settings.adPosition
        bannerId = settings.admobBannerId
        interstitialId = settings.admobInterstitialId
        couponCode = settings.couponCode
        discountStr = settings.discountPercent.toString()
        upiIdConfig = settings.upiId
        coursePriceConfig = settings.coursePrice.toString()
    }

    val categories = listOf("AI Tools", "Instagram Mastery", "YouTube Mastery", "Facebook Growth", "Video Editing", "Freelancing & Online Business")

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
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Creator & Admin Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time database controls and configuration panel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Tabs selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTabButton("Analytics", activeAdminTab == "Analytics") { activeAdminTab = "Analytics" }
                AdminTabButton("Ads & Promo", activeAdminTab == "Ads & Coupon") { activeAdminTab = "Ads & Coupon" }
                AdminTabButton("Course CRUD", activeAdminTab == "Course CRUD") { activeAdminTab = "Course CRUD" }
                AdminTabButton("Payments", activeAdminTab == "Payments") { activeAdminTab = "Payments" }
            }

            // Content Area Scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                when (activeAdminTab) {
                    "Analytics" -> {
                        Column {
                            // Total stats rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    title = "Total Revenue",
                                    value = "$${String.format("%.2f", settings.totalPurchasesRevenue)}",
                                    icon = Icons.Default.MonetizationOn,
                                    color = GrowthGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Active Students",
                                    value = "${settings.totalActiveUsers}",
                                    icon = Icons.Default.Groups,
                                    color = TechBlue,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Revenue Growth graph utilizing custom canvas drawings
                            Text(
                                text = "Weekly Platform Growth Trend",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val width = size.width
                                        val height = size.height
                                        val path = Path()

                                        // Draw graph line
                                        val points = listOf(
                                            Pair(0.1f, 0.8f),
                                            Pair(0.3f, 0.7f),
                                            Pair(0.5f, 0.4f),
                                            Pair(0.7f, 0.5f),
                                            Pair(0.9f, 0.2f)
                                        )

                                        points.forEachIndexed { index, point ->
                                            val x = point.first * width
                                            val y = point.second * height
                                            if (index == 0) {
                                                path.moveTo(x, y)
                                            } else {
                                                path.lineTo(x, y)
                                            }
                                        }

                                        drawPath(
                                            path = path,
                                            color = RoyalPurple,
                                            style = Stroke(width = 6f)
                                        )

                                        // Draw data points circles
                                        points.forEach { point ->
                                            drawCircle(
                                                color = TechBlue,
                                                radius = 10f,
                                                center = Offset(point.first * width, point.second * height)
                                            )
                                        }
                                    }

                                    // X-axis and labels
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Mon", fontSize = 9.sp, color = TextSecondary)
                                        Text("Wed", fontSize = 9.sp, color = TextSecondary)
                                        Text("Fri", fontSize = 9.sp, color = TextSecondary)
                                        Text("Sun", fontSize = 9.sp, color = TextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Distribution categories
                            Text(
                                text = "Active Courses Categories distribution",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    categories.forEach { cat ->
                                        val count = courses.count { it.category == cat }
                                        val total = maxOf(1, courses.size)
                                        val fraction = count.toFloat() / total

                                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cat, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                                Text("$count Courses", fontSize = 10.sp, color = TextSecondary)
                                            }
                                            LinearProgressIndicator(
                                                progress = { fraction },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp)
                                                    .height(6.dp)
                                                    .clip(CircleShape),
                                                color = RoyalPurple,
                                                trackColor = SlateMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Ads & Coupon" -> {
                        Column {
                            // AdMob section
                            Text(
                                text = "AdMob Monetization Engine Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Enable AdMob Android Ads", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Switch(
                                            checked = adsEnabled,
                                            onCheckedChange = { adsEnabled = it },
                                            modifier = Modifier.testTag("ads_enable_switch")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Ad Banner Position", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    val positions = listOf("Top Banner", "Bottom Banner", "Feed Card")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        positions.forEach { pos ->
                                            val isSelected = adPosition == pos
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) TechBlue.copy(alpha = 0.2f) else SlateDark)
                                                    .border(1.dp, if (isSelected) TechBlue else SlateMuted, RoundedCornerShape(8.dp))
                                                    .clickable { adPosition = pos }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(pos, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) TechBlue else TextSecondary)
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = bannerId,
                                        onValueChange = { bannerId = it },
                                        label = { Text("Banner Ad Unit ID") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = TechBlue,
                                            unfocusedBorderColor = SlateMuted
                                        )
                                    )

                                    OutlinedTextField(
                                        value = interstitialId,
                                        onValueChange = { interstitialId = it },
                                        label = { Text("Interstitial Ad Unit ID") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = TechBlue,
                                            unfocusedBorderColor = SlateMuted
                                        )
                                    )
                                }
                            }

                            // Coupons section
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Coupons & Platform Pricing Control",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = couponCode,
                                        onValueChange = { couponCode = it },
                                        label = { Text("Coupon Code String") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_coupon_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RoyalPurple,
                                            unfocusedBorderColor = SlateMuted
                                        )
                                    )

                                    OutlinedTextField(
                                        value = discountStr,
                                        onValueChange = { discountStr = it },
                                        label = { Text("Discount Percentage (%)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_discount_input")
                                            .padding(top = 10.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RoyalPurple,
                                            unfocusedBorderColor = SlateMuted
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            GradientButton(
                                text = "Save Configuration",
                                onClick = {
                                    val percentVal = discountStr.toIntOrNull() ?: 50
                                    viewModel.updateAdminAdSettings(adsEnabled, adPosition, bannerId, interstitialId)
                                    viewModel.updateCoupon(couponCode, percentVal)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTagStr = "save_admin_settings_button"
                            )
                        }
                    }

                    "Course CRUD" -> {
                        Column {
                            // Add Course form
                            Text(
                                text = "Publish New Premium Course",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = newTitle,
                                        onValueChange = { newTitle = it },
                                        label = { Text("Course Title") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_course_title"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )

                                    OutlinedTextField(
                                        value = newDesc,
                                        onValueChange = { newDesc = it },
                                        label = { Text("Description") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newInstructor,
                                            onValueChange = { newInstructor = it },
                                            label = { Text("Instructor Name") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = newDuration,
                                            onValueChange = { newDuration = it },
                                            label = { Text("Duration (e.g. 5h)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newPriceStr,
                                            onValueChange = { newPriceStr = it },
                                            label = { Text("Price (0 for Free)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = lecturesCountStr,
                                            onValueChange = { lecturesCountStr = it },
                                            label = { Text("Lessons Count") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                    }

                                    // Category chips list selector
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Select Course Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        categories.forEach { cat ->
                                            val isSelected = newCategory == cat
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) RoyalPurple.copy(alpha = 0.2f) else SlateDark)
                                                    .border(1.dp, if (isSelected) RoyalPurple else SlateMuted, RoundedCornerShape(8.dp))
                                                    .clickable { newCategory = cat }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) RoyalPurple else TextSecondary)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    GradientButton(
                                        text = "Add Course To Database",
                                        onClick = {
                                            if (newTitle.isNotEmpty() && newDesc.isNotEmpty() && newInstructor.isNotEmpty()) {
                                                val priceVal = newPriceStr.toDoubleOrNull() ?: 29.99
                                                val lecturesCount = lecturesCountStr.toIntOrNull() ?: 3
                                                
                                                val createdLectures = mutableListOf<Lecture>()
                                                for (i in 1..lecturesCount) {
                                                    createdLectures.add(
                                                        Lecture(
                                                            title = "Lecture $i: Core Concepts",
                                                            duration = "15:20",
                                                            videoUrl = "mock_url_$i",
                                                            description = "Introduction and deep analysis of core topics for this professional masterclass."
                                                        )
                                                    )
                                                }

                                                viewModel.addNewCourse(
                                                    title = newTitle,
                                                    description = newDesc,
                                                    instructor = newInstructor,
                                                    category = newCategory,
                                                    price = priceVal,
                                                    duration = newDuration,
                                                    lectures = createdLectures
                                                )

                                                // Reset fields
                                                newTitle = ""
                                                newDesc = ""
                                                newInstructor = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTagStr = "submit_add_course_button"
                                    )
                                }
                            }

                            // Active course deletion lists
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Active courses list (${courses.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            courses.forEach { c ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateCard.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${c.category} • $${c.price}", fontSize = 9.sp, color = TextSecondary)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteCourse(c.id) },
                                        modifier = Modifier.testTag("delete_course_btn_${c.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete course", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }

                    "Payments" -> {
                        val paymentRequests by viewModel.paymentRequests.collectAsState()
                        val pendingRequests = paymentRequests.filter { it.status == "PENDING" }
                        val verifiedRequests = paymentRequests.filter { it.status != "PENDING" }

                        Column {
                            // Section 1: Configure Payment details
                            Text(
                                text = "Global Payment Gateway Settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    OutlinedTextField(
                                        value = upiIdConfig,
                                        onValueChange = { upiIdConfig = it },
                                        label = { Text("Receiver UPI ID") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_upi_id_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = coursePriceConfig,
                                        onValueChange = { coursePriceConfig = it },
                                        label = { Text("Standard Course Price (INR / ₹)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_course_price_input"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    GradientButton(
                                        text = "Update Gateway Configuration",
                                        onClick = {
                                            val priceVal = coursePriceConfig.toDoubleOrNull() ?: 29.0
                                            viewModel.updatePaymentSettings(price = priceVal, currency = "INR", upiId = upiIdConfig, qrCodeBase64 = "")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTagStr = "save_payment_settings_button"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Section 2: Pending Requests
                            Text(
                                text = "Pending Verification Requests (${pendingRequests.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (pendingRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateCard.copy(alpha = 0.5f))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No pending payments to verify. All caught up! 🎉", fontSize = 11.sp, color = TextSecondary)
                                }
                            } else {
                                pendingRequests.forEach { req ->
                                    var showRejectionInput by remember(req.id) { mutableStateOf(false) }
                                    var rejectionReasonText by remember(req.id) { mutableStateOf("Invalid Transaction ID or Screenshot mismatch.") }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .border(1.dp, TechBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = SlateCard)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = req.courseTitle,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "UTR: ${req.transactionId}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TechBlue
                                                    )
                                                }
                                                Text(
                                                    text = "₹${req.amount.toInt()}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = GrowthGreen
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Screenshot: ${req.screenshotPath}",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            if (!showRejectionInput) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.approvePaymentRequest(id = req.id, courseId = req.courseId, courseTitle = req.courseTitle)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("approve_btn_${req.id}")
                                                    ) {
                                                        Text("Approve ✔", fontSize = 11.sp, color = Color.White)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            showRejectionInput = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("reject_btn_${req.id}")
                                                    ) {
                                                        Text("Reject ❌", fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                            } else {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    OutlinedTextField(
                                                        value = rejectionReasonText,
                                                        onValueChange = { rejectionReasonText = it },
                                                        label = { Text("Rejection Reason", fontSize = 10.sp) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedTextColor = Color.White,
                                                            unfocusedTextColor = Color.White
                                                        ),
                                                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                viewModel.rejectPaymentRequest(id = req.id, reason = rejectionReasonText, courseTitle = req.courseTitle)
                                                                showRejectionInput = false
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Confirm Reject", fontSize = 10.sp)
                                                        }
                                                        Button(
                                                            onClick = { showRejectionInput = false },
                                                            colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Text("Cancel", fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Section 3: Historical / Verified Requests
                            Text(
                                text = "Verification History / Audits (${verifiedRequests.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (verifiedRequests.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateCard.copy(alpha = 0.5f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No historical verified records.", fontSize = 11.sp, color = TextSecondary)
                                }
                            } else {
                                verifiedRequests.forEach { req ->
                                    val isApproved = req.status == "APPROVED"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateCard.copy(alpha = 0.5f))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(req.courseTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("UTR: ${req.transactionId} • ₹${req.amount.toInt()}", fontSize = 9.sp, color = TextSecondary)
                                            if (!isApproved) {
                                                Text("Reason: ${req.rejectionReason}", fontSize = 9.sp, color = Color.Red)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isApproved) GrowthGreen.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = req.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isApproved) GrowthGreen else Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AdminTabButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) RoyalPurple else SlateCard)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary)
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
