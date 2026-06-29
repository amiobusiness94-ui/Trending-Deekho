package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassBg,
    borderColor: Color = SlateMuted.copy(alpha = 0.5f),
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(RoyalPurple, TechBlue),
    enabled: Boolean = true,
    testTagStr: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = modifier
            .testTag(testTagStr)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (enabled) colors else listOf(
                        SlateMuted,
                        SlateMuted.copy(alpha = 0.6f)
                    )
                )
            ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else TextDisabled,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun AdMobBanner(
    positionName: String,
    adsEnabled: Boolean,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
) {
    if (!adsEnabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "ad_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, TechBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(TechBlue.copy(alpha = alpha), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AdMob Sponsored",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Google AI Studio Workshop - Free Enrollment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Learn to integrate Gemini API and build rich Apps. Click to join now!",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Open", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val step = 40.dp.toPx()
                val width = size.width
                val height = size.height
                
                // Draw vertical grid lines
                var x = 0f
                while (x < width) {
                    drawLine(
                        color = SlateMuted.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += step
                }
                
                // Draw horizontal grid lines
                var y = 0f
                while (y < height) {
                    drawLine(
                        color = SlateMuted.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += step
                }
            }
    ) {
        // Decorative blobs
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoyalPurple.copy(alpha = 0.15f), Color.Transparent),
                radius = 350.dp.toPx()
            ),
            center = Offset(0f, 100.dp.toPx())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TechBlue.copy(alpha = 0.12f), Color.Transparent),
                radius = 450.dp.toPx()
            ),
            center = Offset(size.width, size.height / 2)
        )
    }
}

@Composable
fun CertificateDialog(
    user: com.example.viewmodel.UserProfile,
    courseTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val certId = remember { "TD-CERT-${(10000..99999).random()}" }
    val regNum = if (user.registrationNumber.isNotEmpty()) user.registrationNumber else "TD-2026-REG-8821"
    val issueDate = "June 29, 2026"
    
    var isDownloading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .border(2.dp, GoldStar, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top controls row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STUDENT CERTIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldStar,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Premium Gold Certificate with Blue Theme Canvas Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            BorderStroke(3.dp, Brush.sweepGradient(listOf(GoldStar, Color(0xFFFBBF24), GoldStar))),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(2.dp)
                        .border(BorderStroke(1.dp, GoldStar.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Thin gold border frame
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Branding logo
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldStar),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, "Seal", tint = SlateDark, modifier = Modifier.size(12.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRENDING DEEKHO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldStar,
                                letterSpacing = 2.sp
                            )
                        }

                        Text(
                            text = "HONORARY CERTIFICATE OF COMPLETION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Student Profile Photo inside Certificate (glowing gold frame)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .border(1.5.dp, GoldStar, CircleShape)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SlateCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (user.profilePhotoUri.isNotEmpty()) user.profilePhotoUri.take(2) else "🎓",
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Text(
                            text = "PROUDLY AWARDED TO",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = user.name.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = "for successfully executing, completing, and passing all levels of the professional course:",
                            fontSize = 8.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            lineHeight = 10.sp
                        )

                        Text(
                            text = courseTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldStar,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Meta details: IDs
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Reg No: $regNum", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("ID: $certId", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Verification QR Code & Signatures Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Instructor Signature
                            Column(horizontalAlignment = Alignment.Start) {
                                // Handwritten calligraphic representation using typography and line brush
                                Canvas(modifier = Modifier.size(50.dp, 16.dp)) {
                                    // Custom signature stroke path (Amiyojit Sarkar signature simulation)
                                    drawLine(
                                        color = Color(0xFF1E40AF),
                                        start = Offset(4f, 12f),
                                        end = Offset(46f, 4f),
                                        strokeWidth = 3f
                                    )
                                    drawLine(
                                        color = Color(0xFF1E40AF),
                                        start = Offset(10f, 6f),
                                        end = Offset(42f, 14f),
                                        strokeWidth = 2f
                                    )
                                }
                                HorizontalDivider(color = GoldStar.copy(alpha = 0.5f), modifier = Modifier.width(60.dp))
                                Text("Amiyojit Sarkar", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("INSTRUCTOR", fontSize = 6.sp, color = TextSecondary)
                            }

                            // Center Official Seal & Verification QR Code
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Golden Official Seal
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(GoldStar, CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = SlateDark)
                                        Text("SEAL", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                                    }
                                }

                                // Canvas QR Code Verification
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.White)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val steps = 5
                                        val cellSize = size.width / steps
                                        // Draw corner anchor blocks
                                        drawRect(Color.Black, Offset(0f, 0f), Size(cellSize * 1.5f, cellSize * 1.5f))
                                        drawRect(Color.Black, Offset(size.width - cellSize * 1.5f, 0f), Size(cellSize * 1.5f, cellSize * 1.5f))
                                        drawRect(Color.Black, Offset(0f, size.height - cellSize * 1.5f), Size(cellSize * 1.5f, cellSize * 1.5f))
                                        
                                        // Fake data pattern
                                        for (x in 1..3) {
                                            for (y in 1..3) {
                                                if ((x + y) % 2 == 0) {
                                                    drawRect(Color.Black, Offset(x * cellSize, y * cellSize), Size(cellSize, cellSize))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Director Signature
                            Column(horizontalAlignment = Alignment.End) {
                                Canvas(modifier = Modifier.size(50.dp, 16.dp)) {
                                    // Custom signature stroke path (D.K. Bose signature simulation)
                                    drawLine(
                                        color = Color(0xFF1E40AF),
                                        start = Offset(4f, 4f),
                                        end = Offset(46f, 12f),
                                        strokeWidth = 3f
                                    )
                                    drawLine(
                                        color = Color(0xFF1E40AF),
                                        start = Offset(8f, 14f),
                                        end = Offset(38f, 6f),
                                        strokeWidth = 2f
                                    )
                                }
                                HorizontalDivider(color = GoldStar.copy(alpha = 0.5f), modifier = Modifier.width(60.dp))
                                Text("D.K. Bose", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("DIRECTOR", fontSize = 6.sp, color = TextSecondary)
                            }
                        }

                        // Dates row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ISSUE DATE: $issueDate", fontSize = 6.sp, color = TextSecondary)
                            Text("VERIFIED: trendingdeekho.com/verify", fontSize = 6.sp, color = GoldStar)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons (simulated download PDF, share, done)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isDownloading = true
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateMuted)
                    ) {
                        Icon(Icons.Default.FileDownload, "Download", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "My Professional Learning Certification")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "I completed the video course \"$courseTitle\" on Trending Deekho with certificate ID $certId! 🎓🏆"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Certificate"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateMuted)
                    ) {
                        Icon(Icons.Default.Share, "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Done", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // PDF Download Progress Simulation Dialog
    if (isDownloading) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                var progressVal by remember { mutableStateOf(0.0f) }
                
                LaunchedEffect(Unit) {
                    while (progressVal < 1.0f) {
                        kotlinx.coroutines.delay(100)
                        progressVal += 0.1f
                    }
                    isDownloading = false
                    // Trigger native notification or alert
                    android.widget.Toast.makeText(
                        context,
                        "Saved successfully to /Downloads/TD-CERT-8182.pdf",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(progress = { progressVal }, color = GoldStar)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generating High Fidelity PDF...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Embedding security hash and seal.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
