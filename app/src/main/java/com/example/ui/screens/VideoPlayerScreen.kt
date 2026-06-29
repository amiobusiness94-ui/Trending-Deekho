package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CourseNote
import com.example.data.CourseRepository
import com.example.data.Lecture
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    viewModel: LearningViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val course by viewModel.focusedCourse.collectAsState()
    val progress by viewModel.focusedProgress.collectAsState()
    val notes by viewModel.focusedNotes.collectAsState()

    val quality by viewModel.videoQuality.collectAsState()
    val speed by viewModel.videoSpeed.collectAsState()
    val activeIdx by viewModel.activeLectureIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val posMs by viewModel.playbackPositionMs.collectAsState()
    val isPiP by viewModel.isPiP.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()

    var activeTab by remember { mutableStateOf("Notes") } // "Curriculum" or "Notes"
    var noteInput by remember { mutableStateOf("") }
    
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
    val activeLecture = lectures.getOrNull(activeIdx) ?: return

    val isPurchased = progress?.isPurchased ?: false || activeCourse.price == 0.0
    val maxDurationMs = if (isPurchased) (5 * 60 * 1000L) else (120_000L) // 5 mins simulation, 2 mins for free preview
    val isFreePreviewEnded = !isPurchased && posMs >= 120_000L

    // Auto simulated watch progress ticks
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(1000)
                val newPos = posMs + 1000L
                if (newPos >= maxDurationMs) {
                    viewModel.updateVideoPosition(maxDurationMs)
                    viewModel.isPlaying.value = false
                    break
                } else {
                    viewModel.updateVideoPosition(newPos)
                }
            }
        }
    }

    // Restore last watched lecture if entering
    LaunchedEffect(activeCourse.id) {
        progress?.let {
            viewModel.activeLectureIndex.value = it.lastWatchedLectureIndex
            viewModel.playbackPositionMs.value = it.lastWatchedPositionMs
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            if (!isFullscreen) {
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
                        text = "Lecture Room",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { viewModel.isPiP.value = !isPiP }) {
                        Icon(
                            imageVector = if (isPiP) Icons.Default.PictureInPictureAlt else Icons.Default.PictureInPicture,
                            contentDescription = "PiP",
                            tint = if (isPiP) TechBlue else Color.White
                        )
                    }
                }
            }

            // Simulated Video Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isFullscreen) 21f / 9f else 16f / 9f)
                    .background(Color.Black)
            ) {
                // Background visual simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(RoyalPurple.copy(alpha = 0.3f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayCircleOutline else Icons.Default.PauseCircleOutline,
                            contentDescription = null,
                            tint = RoyalPurple.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Streaming in ${quality} HD • Speed ${speed}x",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                // Video HUD Overlays (Controls)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top stats line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Lec ${activeIdx + 1}: ${activeLecture.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Quality & Speed Selector icons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VideoSettingBadge(quality) {
                                val nextQuality = when (quality) {
                                    "1080p" -> "360p"
                                    "360p" -> "480p"
                                    "480p" -> "720p"
                                    else -> "1080p"
                                }
                                viewModel.videoQuality.value = nextQuality
                            }
                            VideoSettingBadge("${speed}x") {
                                val nextSpeed = when (speed) {
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 0.5f
                                    else -> 1.0f
                                }
                                viewModel.videoSpeed.value = nextSpeed
                            }
                        }
                    }

                    // Middle main controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            if (!isFreePreviewEnded) {
                                viewModel.updateVideoPosition(maxOf(0L, posMs - 10000L))
                            }
                        }) {
                            Icon(Icons.Default.Replay10, "Rewind 10s", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        FloatingActionButton(
                            onClick = { 
                                if (!isFreePreviewEnded) {
                                    viewModel.triggerPlayPause() 
                                }
                            },
                            containerColor = RoyalPurple,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .testTag("play_pause_video_button")
                                .size(50.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "PlayPause"
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { 
                            if (!isFreePreviewEnded) {
                                viewModel.updateVideoPosition(minOf(posMs + 10000L, maxDurationMs))
                            }
                        }) {
                            Icon(Icons.Default.Forward10, "Fast Forward 10s", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    // Bottom progress bar HUD
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val durationText = formatDuration(minOf(posMs, maxDurationMs))
                            val totalDurationText = formatDuration(maxDurationMs)
                            Text(
                                text = "$durationText / $totalDurationText",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.isFullscreen.value = !isFullscreen },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                        Slider(
                            value = minOf(posMs, maxDurationMs).toFloat(),
                            onValueChange = { viewModel.updateVideoPosition(minOf(it.toLong(), maxDurationMs)) },
                            valueRange = 0f..maxDurationMs.toFloat(),
                            modifier = Modifier
                                .testTag("video_timeline_slider")
                                .fillMaxWidth()
                                .height(16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = RoyalPurple,
                                activeTrackColor = RoyalPurple,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                if (isFreePreviewEnded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.95f))
                            .clickable { /* Block all click propagation */ }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = GoldStar,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Free Preview Ended 🔒",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To unlock the full 6+ hours of premium lectures, notes, and certificates, please complete the lifetime access payment.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("unlock_full_course_from_preview_button")
                            ) {
                                Text("Return & Unlock Full Course (₹${activeCourse.price.toInt()})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (!isFullscreen) {
                // Lecture details, descriptions, and action checklist
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeLecture.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = activeLecture.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Complete lecture button
                        val completedList = progress?.completedLecturesCsv?.split(",") ?: emptyList()
                        val isCompleted = completedList.contains(activeIdx.toString())

                        Button(
                            onClick = {
                                viewModel.completeLecture(activeCourse.id, activeIdx)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) GrowthGreen.copy(alpha = 0.2f) else TechBlue
                            ),
                            border = if (isCompleted) BorderStroke(1.dp, GrowthGreen) else null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .testTag("complete_lecture_button")
                                .height(36.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (isCompleted) GrowthGreen else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCompleted) "Done" else "Mark Complete",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) GrowthGreen else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector row: Notes vs. Curriculum
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TabSelectorItem(
                            label = "Notes",
                            isSelected = activeTab == "Notes",
                            modifier = Modifier.weight(1f)
                        ) { activeTab = "Notes" }

                        TabSelectorItem(
                            label = "Curriculum",
                            isSelected = activeTab == "Curriculum",
                            modifier = Modifier.weight(1f)
                        ) { activeTab = "Curriculum" }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab view content switcher
                    AnimatedContent(
                        targetState = activeTab,
                        label = "tab_animation"
                    ) { tab ->
                        when (tab) {
                            "Notes" -> {
                                Column {
                                    // Add note field
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = noteInput,
                                            onValueChange = { noteInput = it },
                                            placeholder = {
                                                Text(
                                                    "Add note at ${formatDuration(posMs)}...",
                                                    fontSize = 11.sp,
                                                    color = TextDisabled
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("add_note_input_field")
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
                                        IconButton(
                                            onClick = {
                                                viewModel.addNote(activeCourse.id, noteInput)
                                                noteInput = ""
                                            },
                                            modifier = Modifier
                                                .testTag("save_note_button")
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(RoyalPurple)
                                        ) {
                                            Icon(Icons.Default.Add, "Add", tint = Color.White)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Display list of notes
                                    val activeNotes = notes.filter { it.lectureIndex == activeIdx }
                                    if (activeNotes.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No notes added for this lecture. Write your key takeaways above!",
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(activeNotes) { note ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            // Seek to note timestamp!
                                                            viewModel.updateVideoPosition(note.positionMs)
                                                        },
                                                    colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.6f)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(TechBlue.copy(alpha = 0.15f))
                                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = formatDuration(note.positionMs),
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = TechBlue
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = note.noteText,
                                                                fontSize = 11.sp,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(top = 4.dp)
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = { viewModel.deleteNote(note.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete note",
                                                                tint = Color.Red.copy(alpha = 0.8f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "Curriculum" -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(lectures.size) { idx ->
                                        val lecItem = lectures[idx]
                                        val isCurrent = idx == activeIdx
                                        val completedList = progress?.completedLecturesCsv?.split(",") ?: emptyList()
                                        val isDone = completedList.contains(idx.toString())

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrent) RoyalPurple.copy(alpha = 0.15f) else SlateCard.copy(alpha = 0.5f))
                                                .border(
                                                    1.dp,
                                                    if (isCurrent) RoyalPurple else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.selectLecture(idx) }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCurrent) RoyalPurple else SlateDark),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isCurrent) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) Color.White else TextSecondary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${idx + 1}. ${lecItem.title}",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isCurrent) RoyalPurple else Color.White
                                                )
                                                Text(
                                                    text = lecItem.duration,
                                                    fontSize = 9.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            if (isDone) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = GrowthGreen,
                                                    modifier = Modifier.size(16.dp)
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
        }

        // PiP simulation floating overlay box
        if (isPiP) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(width = 160.dp, height = 90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(2.dp, RoyalPurple, RoundedCornerShape(12.dp))
                    .clickable { viewModel.isPiP.value = false },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Exit PiP",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Tap to expand",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VideoSettingBadge(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TabSelectorItem(
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

fun formatDuration(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / (1000 * 60)) % 60
    return String.format("%02d:%02d", min, sec)
}
