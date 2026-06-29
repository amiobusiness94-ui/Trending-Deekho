package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Course
import com.example.data.CourseRepository
import com.example.data.Lecture
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    viewModel: LearningViewModel,
    courseId: Int,
    onNavigateToPlayer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Select the active course detail inside ViewModel
    LaunchedEffect(courseId) {
        viewModel.selectCourse(courseId)
    }

    val course by viewModel.focusedCourse.collectAsState()
    val progress by viewModel.focusedProgress.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()

    var couponCode by remember { mutableStateOf("") }
    var discountedPrice by remember { mutableStateOf<Double?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    var transactionId by remember { mutableStateOf("") }
    var screenshotUploaded by remember { mutableStateOf(false) }
    var screenshotName by remember { mutableStateOf("") }
    var clipboardFeedback by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if (course == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RoyalPurple)
        }
        return
    }

    val activeCourse = course!!
    val lectures = CourseRepository.parseLecturesJson(activeCourse.lecturesJson)
    val isPurchased = progress?.isPurchased ?: false || activeCourse.price == 0.0
    val isBookmarked = progress?.isBookmarked ?: false
    val isWishlisted = progress?.isWishlisted ?: false
    val isDownloaded = progress?.isDownloaded ?: false

    val currentPrice = discountedPrice ?: activeCourse.price

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    text = "Course Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Bookmark & Wishlist Buttons
                Row {
                    IconButton(onClick = { viewModel.toggleBookmark(activeCourse.id) }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) GoldStar else Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.toggleWishlist(activeCourse.id) }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Color.Red else Color.White
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Course Header/Banner Backing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(RoyalPurple, TechBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(100.dp)
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SlateDark.copy(alpha = 0.8f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeCourse.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeCourse.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Rows (Instructor, Rating, Lectures count, Hours)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetadataChip(Icons.Default.Person, activeCourse.instructor, Modifier.weight(1.5f))
                    MetadataChip(Icons.Default.Star, "${activeCourse.rating} (${activeCourse.reviewsCount})", Modifier.weight(1f), GoldStar)
                    MetadataChip(Icons.Default.Schedule, activeCourse.duration, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Course Description
                Text(
                    text = "About This Course",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = activeCourse.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                    lineHeight = 17.sp
                )

                // Bookmark & Downloads Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleDownload(activeCourse.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, SlateMuted),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.FileDownloadDone else Icons.Default.FileDownload,
                            contentDescription = "Download",
                            tint = if (isDownloaded) GrowthGreen else TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isDownloaded) "Downloaded" else "Download HD", fontSize = 11.sp)
                    }

                    if (isPurchased) {
                        GradientButton(
                            text = if (progress != null && progress!!.percentCompleted > 0) "Resume watching" else "Start watching",
                            onClick = onNavigateToPlayer,
                            modifier = Modifier.weight(1f),
                            testTagStr = "start_learning_button"
                        )
                    } else {
                        GradientButton(
                            text = "Play Free Preview (2m)",
                            onClick = onNavigateToPlayer,
                            modifier = Modifier.weight(1f),
                            testTagStr = "play_preview_button"
                        )
                    }
                }

                // Purchase flow if not purchased
                if (!isPurchased) {
                    val requests by viewModel.paymentRequests.collectAsState()
                    val courseRequest = requests.find { it.courseId == activeCourse.id }
                    
                    val isPending = courseRequest?.status == "PENDING"
                    val isRejected = courseRequest?.status == "REJECTED"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(1.dp, TechBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isPending) {
                                // PENDING VERIFICATION STATE UI
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(GoldStar.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.HourglassEmpty, "Pending", tint = GoldStar, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Verification Pending ⏳",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldStar
                                        )
                                        Text(
                                            text = "UTR/Txn: ${courseRequest?.transactionId}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Your payment request of ₹${courseRequest?.amount?.toInt()} has been successfully submitted and is being manually verified by our administration team. This usually takes between 5 to 15 minutes.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Once approved, the full course curriculum and certificate will unlock automatically. Feel free to watch the free preview in the meantime!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TechBlue,
                                    lineHeight = 16.sp
                                )
                            } else {
                                // SHOW FORM OR RETRY STATE
                                Text(
                                    text = "Premium Lifetime Access",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Unlock all high-definition professional video lectures, custom notes, downloadable files, and direct certificates.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                                )

                                if (isRejected) {
                                    // REJECTED WARNING BANNER
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Red.copy(alpha = 0.15f))
                                            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Cancel, "Rejected", tint = Color.Red, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Verification Failed ❌",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Red
                                            )
                                            Text(
                                                text = "Reason: ${courseRequest?.rejectionReason}",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Coupon Section
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponCode,
                                        onValueChange = { couponCode = it },
                                        placeholder = { Text("Coupon Code (e.g. GROW50)", fontSize = 11.sp, color = TextDisabled) },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .testTag("coupon_input_field")
                                            .height(44.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = RoyalPurple,
                                            unfocusedBorderColor = SlateMuted
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            discountedPrice = viewModel.applyPromo(couponCode, activeCourse.price)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("apply_coupon_button")
                                            .height(44.dp)
                                    ) {
                                        Text("Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Coupon response message
                                val couponMsg by viewModel.couponMessage.collectAsState()
                                couponMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (msg.contains("Applied")) GrowthGreen else Color.Red,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                // Pricing display
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Amount to Pay", fontSize = 12.sp, color = TextSecondary)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        if (discountedPrice != null && discountedPrice!! < activeCourse.price) {
                                            Text(
                                                text = "₹${activeCourse.price.toInt()}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextDisabled,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                ),
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        }
                                        Text(
                                            text = "₹${currentPrice.toInt()}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GrowthGreen
                                        )
                                    }
                                }

                                // Payment Method Selector
                                Text(
                                    text = "Select UPI App Method",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val paymentMethodsList = listOf("UPI ID", "PhonePe", "Google Pay")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    paymentMethodsList.forEach { method ->
                                        val isSelected = selectedPaymentMethod == method
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) TechBlue.copy(alpha = 0.2f) else SlateDark)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) TechBlue else SlateMuted,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedPaymentMethod = method }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = method,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) TechBlue else TextSecondary
                                            )
                                        }
                                    }
                                }

                                // UPI Payment details (UPI ID copy + custom QR Code)
                                val currentUpiId = settings.upiId
                                val clipboardManager = LocalClipboardManager.current

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SlateDark)
                                        .border(1.dp, SlateMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "1. Copy Official UPI ID & Pay",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateCard)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = currentUpiId,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TechBlue
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(currentUpiId))
                                                clipboardFeedback = true
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    clipboardFeedback = false
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (clipboardFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = if (clipboardFeedback) GrowthGreen else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (clipboardFeedback) {
                                        Text(
                                            text = "UPI ID Copied to Clipboard!",
                                            fontSize = 10.sp,
                                            color = GrowthGreen,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "OR Scan QR Code to Pay instantly",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Live Canvas Drawn QR Code Component
                                    Canvas(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        val sizePx = size.width
                                        val squareSize = sizePx / 15f
                                        
                                        // Draw background
                                        drawRect(Color.White)
                                        
                                        // Top-Left Finder Pattern
                                        drawRect(Color.Black, Offset(0f, 0f), Size(squareSize * 5, squareSize * 5))
                                        drawRect(Color.White, Offset(squareSize, squareSize), Size(squareSize * 3, squareSize * 3))
                                        drawRect(Color.Black, Offset(squareSize * 2, squareSize * 2), Size(squareSize, squareSize))
                                        
                                        // Top-Right Finder Pattern
                                        drawRect(Color.Black, Offset(sizePx - squareSize * 5, 0f), Size(squareSize * 5, squareSize * 5))
                                        drawRect(Color.White, Offset(sizePx - squareSize * 4, squareSize), Size(squareSize * 3, squareSize * 3))
                                        drawRect(Color.Black, Offset(sizePx - squareSize * 3, squareSize * 2), Size(squareSize, squareSize))
                                        
                                        // Bottom-Left Finder Pattern
                                        drawRect(Color.Black, Offset(0f, sizePx - squareSize * 5), Size(squareSize * 5, squareSize * 5))
                                        drawRect(Color.White, Offset(squareSize, sizePx - squareSize * 4), Size(squareSize * 3, squareSize * 3))
                                        drawRect(Color.Black, Offset(squareSize * 2, sizePx - squareSize * 3), Size(squareSize, squareSize))
                                        
                                        // Bottom-Right Small Finder
                                        drawRect(Color.Black, Offset(sizePx - squareSize * 3, sizePx - squareSize * 3), Size(squareSize * 2, squareSize * 2))
                                        
                                        // Random fake digital blocks in the grid
                                        val randomBits = listOf(
                                            0, 1, 0, 1, 1, 0, 0, 1, 1, 0,
                                            1, 0, 1, 0, 0, 1, 1, 0, 0, 1,
                                            0, 1, 1, 1, 0, 0, 1, 1, 1, 0,
                                            1, 1, 0, 0, 1, 1, 0, 0, 1, 1,
                                            0, 0, 1, 1, 0, 1, 0, 1, 0, 0,
                                            1, 0, 1, 0, 1, 0, 1, 1, 0, 1,
                                            0, 1, 0, 1, 0, 1, 0, 0, 1, 0,
                                            1, 1, 0, 0, 1, 0, 1, 0, 1, 1
                                        )
                                        for (i in 0..7) {
                                            for (j in 0..9) {
                                                if (randomBits[(i * 10 + j) % randomBits.size] == 1) {
                                                    val x = squareSize * (i + 6)
                                                    val y = squareSize * (j + 4)
                                                    drawRect(Color.Black, Offset(x, y), Size(squareSize, squareSize))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Scan with PhonePe, GPay, or Paytm",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "2. Enter Transaction ID & Verification Details",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedTextField(
                                    value = transactionId,
                                    onValueChange = { transactionId = it },
                                    label = { Text("Transaction ID / UTR (12 Digits)", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. 129845371042", fontSize = 11.sp, color = TextDisabled) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("transaction_id_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = RoyalPurple,
                                        unfocusedBorderColor = SlateMuted
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Screenshot Picker row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            screenshotUploaded = true
                                            screenshotName = "PAYMENT_RECEIPT_${System.currentTimeMillis().toString().takeLast(4)}.PNG"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("screenshot_upload_button")
                                    ) {
                                        Icon(Icons.Default.UploadFile, "Upload", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload Screenshot", fontSize = 11.sp)
                                    }
                                    if (screenshotUploaded) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, "Done", tint = GrowthGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = screenshotName,
                                                fontSize = 10.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 120.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                GradientButton(
                                    text = "Submit Verification Request",
                                    onClick = {
                                        if (transactionId.trim().isEmpty()) {
                                            viewModel.addManualNotification("Verification Error ⚠️", "Transaction ID / UTR is required to verify payment.")
                                            return@GradientButton
                                        }
                                        isProcessingPayment = true
                                        coroutineScope.launch {
                                            delay(1500) // Simulated processing latency
                                            isProcessingPayment = false
                                            viewModel.submitPaymentRequest(
                                                courseId = activeCourse.id,
                                                courseTitle = activeCourse.title,
                                                transactionId = transactionId,
                                                amount = currentPrice,
                                                upiIdUsed = currentUpiId,
                                                screenshotBase64 = screenshotName
                                            )
                                            // Reset inputs
                                            transactionId = ""
                                            screenshotUploaded = false
                                            screenshotName = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTagStr = "secure_checkout_button"
                                )
                            }
                        }
                    }
                }

                // Curriculum Table of contents
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Course Curriculum (${lectures.size} Lessons)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val completedList = progress?.completedLecturesCsv?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                val isLevel1Completed = completedList.contains("0") && completedList.contains("1")
                val isLevel2Completed = isLevel1Completed && completedList.contains("2") && completedList.contains("3")

                lectures.forEachIndexed { index, lec ->
                    // Level header injection
                    val showLevelHeader = index == 0 || index == 2 || index == 4
                    if (showLevelHeader) {
                        val (lvlName, lvlStatus, lvlColor) = when (index) {
                            0 -> Triple("LEVEL 1: Beginner Basics", "Active 🟢", GrowthGreen)
                            2 -> Triple("LEVEL 2: Intermediate Mastery", if (isLevel1Completed) "Unlocked 🟢" else "Locked 🔒 (Complete Level 1)", if (isLevel1Completed) GrowthGreen else TextSecondary)
                            else -> Triple("LEVEL 3: Advanced & Certification", if (isLevel2Completed) "Unlocked 🎓" else "Locked 🔒 (Complete Level 2)", if (isLevel2Completed) GoldStar else TextSecondary)
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .background(SlateCard.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lvlName, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(lvlStatus, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = lvlColor)
                        }
                    }

                    val isLecLocked = when {
                        index <= 1 -> false
                        index <= 3 -> !isLevel1Completed
                        else -> !isLevel2Completed
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(
                                1.dp,
                                if (isLecLocked) SlateMuted.copy(alpha = 0.2f) else if (isPurchased) RoyalPurple.copy(alpha = 0.3f) else SlateMuted.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = isPurchased) {
                                if (isLecLocked) {
                                    // Trigger safe notification to let student know it is locked!
                                    viewModel.addManualNotification(
                                        "Lesson Locked 🔒",
                                        "Please complete all lessons in previous levels to unlock this chapter!"
                                    )
                                } else {
                                    viewModel.activeLectureIndex.value = index
                                    onNavigateToPlayer()
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLecLocked) SlateCard.copy(alpha = 0.2f) else SlateCard.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular icon backing
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLecLocked) SlateMuted 
                                        else if (isPurchased) RoyalPurple.copy(alpha = 0.15f) 
                                        else SlateMuted
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLecLocked) Icons.Default.Lock 
                                                  else if (isPurchased) Icons.Default.PlayArrow 
                                                  else Icons.Default.Lock,
                                    contentDescription = if (isLecLocked) "Locked" else "Play",
                                    tint = if (isLecLocked) TextDisabled 
                                           else if (isPurchased) RoyalPurple 
                                           else TextDisabled,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lec.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLecLocked) TextDisabled else Color.White
                                )
                                Text(
                                    text = lec.duration + if (isLecLocked) " (Locked)" else " (Ready)",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            
                            // Completed checkbox state
                            if (isPurchased) {
                                val isDone = completedList.contains(index.toString())
                                
                                IconButton(
                                    onClick = {
                                        if (!isLecLocked) {
                                            viewModel.toggleLectureCompletion(activeCourse.id, index)
                                        } else {
                                            viewModel.addManualNotification(
                                                "Lesson Locked 🔒",
                                                "Cannot complete a locked lesson. Complete previous chapters first!"
                                            )
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isDone) "Done" else "Todo",
                                        tint = if (isLecLocked) TextDisabled.copy(alpha = 0.5f) else if (isDone) GrowthGreen else TextDisabled,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Processing payment dialog overlay
        if (isProcessingPayment) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = RoyalPurple)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Securing Connection...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Processing payment via $selectedPaymentMethod. Please do not close the app.",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Purchase Success dialog overlay
        if (purchaseSuccess) {
            Dialog(onDismissRequest = { viewModel.clearPurchaseSuccess() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                        .border(1.dp, GrowthGreen, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GrowthGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = GrowthGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Approved! 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Your digital course has been unlocked successfully. Start learning right away!",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )
                        GradientButton(
                            text = "Start Learning",
                            onClick = {
                                viewModel.clearPurchaseSuccess()
                                onNavigateToPlayer()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    iconColor: Color = TechBlue
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateCard)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
