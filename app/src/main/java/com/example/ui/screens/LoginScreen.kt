package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var forgotPasswordMode by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    // Login fields
    var loginEmailOrMobile by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    // Signup Step 1 fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupMobile by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Avatar / Profile photo options
    val avatars = listOf("👨‍💻 Coder", "👩‍🎨 Creative", "🎓 Scholar", "💼 Analyst", "🚀 Lead")
    var selectedAvatar by remember { mutableStateOf("👨‍💻 Coder") }

    // Signup Step 2 fields
    var address by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }

    // Signup Step 3 fields
    var transactionId by remember { mutableStateOf("") }
    val screenshotOptions = listOf("Paytm_receipt_29.png", "GPay_success_9182.jpg", "PhonePe_txn_9281.png")
    var selectedScreenshot by remember { mutableStateOf("Paytm_receipt_29.png") }

    // Multi-step track
    var currentSignUpStep by remember { mutableStateOf(1) } // 1: Profile & Password, 2: Address, 3: Payment Verification

    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        GridBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Branded Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(RoyalPurple, TechBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Logo Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trending Deekho",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending",
                            tint = GrowthGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Premium Video Course Platform",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TechBlue,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Forgot Password Screen Mode
            if (forgotPasswordMode) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    backgroundColor = SlateCard.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Reset Password 🔑",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Enter your registered Email or Mobile, then provide your new desired password.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    var recoveryTarget by remember { mutableStateOf("") }
                    var newPass by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = recoveryTarget,
                        onValueChange = { recoveryTarget = it },
                        label = { Text("Email or Mobile") },
                        leadingIcon = { Icon(Icons.Default.Person, "Target") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Pass") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    errorMsg?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }

                    GradientButton(
                        text = "Restore & Reset Password",
                        onClick = {
                            if (recoveryTarget.isEmpty() || newPass.isEmpty()) {
                                errorMsg = "Please fill in all fields."
                            } else {
                                val err = viewModel.forgotPassword(recoveryTarget, newPass)
                                if (err != null) {
                                    errorMsg = err
                                } else {
                                    errorMsg = null
                                    successMsg = "Password reset successfully! Log in now."
                                    forgotPasswordMode = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTagStr = "submit_restore_btn"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Back to Login",
                        fontSize = 13.sp,
                        color = TechBlue,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                forgotPasswordMode = false
                                errorMsg = null
                            }
                    )
                }
            } else if (!isSignUp) {
                // NORMAL LOGIN VIEW
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    backgroundColor = SlateCard.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Enter your credentials to resume watching",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    successMsg?.let {
                        Text(it, color = GrowthGreen, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }

                    OutlinedTextField(
                        value = loginEmailOrMobile,
                        onValueChange = { loginEmailOrMobile = it },
                        label = { Text("Email or Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = SlateMuted
                        )
                    )

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                            .padding(bottom = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RoyalPurple,
                            unfocusedBorderColor = SlateMuted
                        )
                    )

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = RoyalPurple)
                            )
                            Text("Remember Me", color = Color.White, fontSize = 12.sp)
                        }
                        Text(
                            text = "Forgot Password?",
                            color = TechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                forgotPasswordMode = true
                                errorMsg = null
                                successMsg = null
                            }
                        )
                    }

                    errorMsg?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    GradientButton(
                        text = "Login Securely",
                        onClick = {
                            if (loginEmailOrMobile.isEmpty() || loginPassword.isEmpty()) {
                                errorMsg = "Please fill in all fields."
                            } else {
                                val err = viewModel.login(loginEmailOrMobile, loginPassword)
                                if (err != null) {
                                    errorMsg = err
                                } else {
                                    errorMsg = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTagStr = "submit_login_button"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SlateMuted)
                        Text(
                            text = "OR",
                            color = TextDisabled,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SlateMuted)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mock Google Login
                    OutlinedButton(
                        onClick = {
                            viewModel.login("Amio Rahman", "amiobusiness94@gmail.com", isGoogle = true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_login_button")
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateMuted),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", fontWeight = FontWeight.Bold, color = TechBlue, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = "Sign Up",
                            color = TechBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .testTag("toggle_signup_button")
                                .clickable {
                                    isSignUp = true
                                    errorMsg = null
                                    successMsg = null
                                    currentSignUpStep = 1
                                }
                        )
                    }
                }
            } else {
                // 3-STEP SIGNUP FLOW
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    backgroundColor = SlateCard.copy(alpha = 0.85f)
                ) {
                    // Header with Steps Progress indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Register Student",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..3).forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (currentSignUpStep >= step) RoyalPurple else SlateMuted),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = step.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                if (step < 3) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }
                    }
                    Text(
                        text = when (currentSignUpStep) {
                            1 -> "Step 1: Student Credentials & Avatar"
                            2 -> "Step 2: Communication Details"
                            else -> "Step 3: Registration Fee Payment (₹29)"
                        },
                        fontSize = 11.sp,
                        color = TechBlue,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Error Box
                    errorMsg?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (currentSignUpStep == 1) {
                        // STEP 1: Profile & Password
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            leadingIcon = { Icon(Icons.Default.Person, "Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            leadingIcon = { Icon(Icons.Default.Person, "Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = signupMobile,
                            onValueChange = { signupMobile = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = { signupEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        // Avatar Picker (profile_photo_upload)
                        Text(
                            text = "Select Student Profile Photo Avatar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            items(avatars) { avatar ->
                                val isSelected = avatar == selectedAvatar
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) RoyalPurple.copy(alpha = 0.3f) else SlateMuted)
                                        .border(2.dp, if (isSelected) RoyalPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedAvatar = avatar }
                                        .padding(8.dp)
                                ) {
                                    Text(avatar, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = { signupPassword = it },
                            label = { Text("Create Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Pass") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, "ConfirmPass") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        GradientButton(
                            text = "Continue to Address ➔",
                            onClick = {
                                if (firstName.isEmpty() || lastName.isEmpty() || signupEmail.isEmpty() || signupMobile.isEmpty() || signupPassword.isEmpty()) {
                                    errorMsg = "Please fill in all details."
                                } else if (signupPassword != confirmPassword) {
                                    errorMsg = "Passwords do not match."
                                } else if (!signupEmail.contains("@")) {
                                    errorMsg = "Invalid email format."
                                } else {
                                    errorMsg = null
                                    currentSignUpStep = 2
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (currentSignUpStep == 2) {
                        // STEP 2: Communication Details
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Full Residential Address") },
                            leadingIcon = { Icon(Icons.Default.Home, "Address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District") },
                            leadingIcon = { Icon(Icons.Default.LocationCity, "District") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            leadingIcon = { Icon(Icons.Default.Map, "State") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("PIN Code") },
                            leadingIcon = { Icon(Icons.Default.Pin, "PIN") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { currentSignUpStep = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateMuted),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }

                            GradientButton(
                                text = "Go to Payment ➔",
                                onClick = {
                                    if (address.isEmpty() || district.isEmpty() || state.isEmpty() || pinCode.isEmpty()) {
                                        errorMsg = "Please fill in all address fields."
                                    } else {
                                        errorMsg = null
                                        currentSignUpStep = 3
                                    }
                                },
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    } else {
                        // STEP 3: Payment Verification Gateway (₹29)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "To complete your registration, a one-time lifetime membership fee of ₹29 is required.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Vector QR Code Generator Draw
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val sizePx = size.width
                                    val strokeW = 4f
                                    
                                    // Corners for QR Code
                                    drawRect(Color.Black, Offset(0f, 0f), Size(sizePx * 0.25f, sizePx * 0.25f))
                                    drawRect(Color.White, Offset(strokeW, strokeW), Size(sizePx * 0.25f - strokeW * 2, sizePx * 0.25f - strokeW * 2))
                                    drawRect(Color.Black, Offset(strokeW * 2, strokeW * 2), Size(sizePx * 0.25f - strokeW * 4, sizePx * 0.25f - strokeW * 4))

                                    drawRect(Color.Black, Offset(sizePx * 0.75f, 0f), Size(sizePx * 0.25f, sizePx * 0.25f))
                                    drawRect(Color.White, Offset(sizePx * 0.75f + strokeW, strokeW), Size(sizePx * 0.25f - strokeW * 2, sizePx * 0.25f - strokeW * 2))
                                    drawRect(Color.Black, Offset(sizePx * 0.75f + strokeW * 2, strokeW * 2), Size(sizePx * 0.25f - strokeW * 4, sizePx * 0.25f - strokeW * 4))

                                    drawRect(Color.Black, Offset(0f, sizePx * 0.75f), Size(sizePx * 0.25f, sizePx * 0.25f))
                                    drawRect(Color.White, Offset(strokeW, sizePx * 0.75f + strokeW), Size(sizePx * 0.25f - strokeW * 2, sizePx * 0.25f - strokeW * 2))
                                    drawRect(Color.Black, Offset(strokeW * 2, sizePx * 0.75f + strokeW * 2), Size(sizePx * 0.25f - strokeW * 4, sizePx * 0.25f - strokeW * 4))

                                    // Random dots to look like a real QR
                                    val steps = 8
                                    val cellSize = sizePx / steps
                                    for (x in 2..5) {
                                        for (y in 2..5) {
                                            if ((x + y) % 2 == 0) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(x * cellSize, y * cellSize),
                                                    size = Size(cellSize, cellSize)
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Visual details
                                    drawRect(Color.Black, Offset(sizePx * 0.4f, sizePx * 0.4f), Size(sizePx * 0.2f, sizePx * 0.2f))
                                    drawRect(RoyalPurple, Offset(sizePx * 0.45f, sizePx * 0.45f), Size(sizePx * 0.1f, sizePx * 0.1f))
                                }
                            }
                            
                            Text(
                                text = "Scan or Pay to UPI ID below",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDisabled,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // UPI Information Panel
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateDark.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Official Gateway UPI ID:", fontSize = 9.sp, color = TextSecondary)
                                        Text("amiyosarkar.a-1@okaxis", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TechBlue)
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("amiyosarkar.a-1@okaxis"))
                                            errorMsg = "UPI ID Copied! Paste in your app."
                                        },
                                        modifier = Modifier.size(36.dp).testTag("copy_upi_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Payment methods badge
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Accepted Methods:", fontSize = 9.sp, color = TextSecondary)
                                listOf("UPI", "GPay", "PhonePe").forEach { method ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RoyalPurple.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(method, fontSize = 8.sp, color = RoyalPurple, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = transactionId,
                                onValueChange = { transactionId = it },
                                label = { Text("Enter Transaction ID / UTR (12 Digits)") },
                                leadingIcon = { Icon(Icons.Default.Receipt, "UTR") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_txn_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )

                            // Screenshot selector simulation (upload_payment_screenshot)
                            Text(
                                "Upload/Simulate Payment Screenshot",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 4.dp).align(Alignment.Start)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                items(screenshotOptions) { path ->
                                    val isSelected = path == selectedScreenshot
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) RoyalPurple.copy(alpha = 0.3f) else SlateMuted)
                                            .border(1.5.dp, if (isSelected) RoyalPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable { selectedScreenshot = path }
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Image, "Pic", modifier = Modifier.size(12.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(path, fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { currentSignUpStep = 2 },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateMuted),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Back")
                                }

                                GradientButton(
                                    text = "Submit & Sign Up ✔",
                                    onClick = {
                                        if (transactionId.trim().length < 6) {
                                            errorMsg = "Please enter a valid Transaction ID."
                                        } else {
                                            errorMsg = null
                                            // Register the user!
                                            val regErr = viewModel.signUpUser(
                                                firstName = firstName,
                                                lastName = lastName,
                                                email = signupEmail,
                                                mobileNumber = signupMobile,
                                                address = address,
                                                district = district,
                                                state = state,
                                                pinCode = pinCode,
                                                passwordEntered = signupPassword,
                                                profilePhotoUri = selectedAvatar
                                            )
                                            if (regErr != null) {
                                                errorMsg = regErr
                                            } else {
                                                // Automatically submit the registration payment!
                                                viewModel.submitRegistrationPayment(
                                                    transactionId = transactionId.trim(),
                                                    screenshotPath = selectedScreenshot
                                                )
                                                // Completed!
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(2f),
                                    testTagStr = "submit_signup_btn"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = "Login",
                            color = TechBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    isSignUp = false
                                    errorMsg = null
                                    successMsg = null
                                }
                        )
                    }
                }
            }
        }
    }
}
