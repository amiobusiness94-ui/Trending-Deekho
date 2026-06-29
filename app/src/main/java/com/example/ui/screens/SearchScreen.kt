package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.theme.*
import com.example.viewmodel.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: LearningViewModel,
    onNavigateToCourseDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val progressList by viewModel.progressList.collectAsState()
    val settings by viewModel.adminSettings.collectAsState()

    val categories = listOf(
        "All",
        "Instagram Mastery",
        "YouTube Mastery",
        "Facebook Growth",
        "AI Tools",
        "Video Editing",
        "Thumbnail Design",
        "Freelancing & Online Business"
    )

    // Filter courses locally based on query + category
    val filteredCourses = courses.filter { course ->
        val matchesQuery = course.title.contains(query, ignoreCase = true) ||
                course.description.contains(query, ignoreCase = true) ||
                course.instructor.contains(query, ignoreCase = true)
        val matchesCategory = activeCategory == null || activeCategory == "All" || course.category == activeCategory
        matchesQuery && matchesCategory
    }

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
                text = "Explore Courses",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search topics, tools, skills...", color = TextDisabled) },
                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TechBlue) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, "Clear", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("search_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard,
                    focusedBorderColor = RoyalPurple,
                    unfocusedBorderColor = SlateMuted
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                categories.forEach { cat ->
                    val isSelected = (cat == "All" && activeCategory == null) || (activeCategory == cat)
                    val bgBrush = if (isSelected) {
                        Brush.linearGradient(colors = listOf(RoyalPurple, TechBlue))
                    } else {
                        Brush.linearGradient(colors = listOf(SlateCard, SlateCard))
                    }
                    val borderStroke = if (isSelected) Color.Transparent else SlateMuted

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgBrush)
                            .border(1.dp, borderStroke, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.selectedCategory.value = if (cat == "All") null else cat
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
            }

            // Results Section
            if (filteredCourses.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No courses",
                        tint = TextDisabled,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Courses Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "We couldn't find any courses matching your search. Try resetting filters or testing different keywords.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.searchQuery.value = ""
                            viewModel.selectedCategory.value = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SlateMuted)
                    ) {
                        Text("Reset Filters", fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCourses) { course ->
                        val progress = progressList.find { it.courseId == course.id }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToCourseDetail(course.id) },
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(95.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(RoyalPurple.copy(alpha = 0.7f), TechBlue.copy(alpha = 0.7f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    
                                    // Price Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (course.price == 0.0) GrowthGreen else TechBlue)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (course.price == 0.0) "FREE" else "$${course.price}",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = course.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 14.sp
                                    )
                                    Text(
                                        text = "By ${course.instructor}",
                                        fontSize = 9.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = GoldStar,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${course.rating}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                        Text(
                                            text = course.duration,
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    // Progress state
                                    if (progress != null && progress.percentCompleted > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress.percentCompleted / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
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

            // Bottom Ad in Search Screen
            if (settings.adsEnabled && settings.adPosition == "Bottom Banner") {
                AdMobBanner(
                    positionName = "Search Bottom",
                    adsEnabled = settings.adsEnabled,
                    adUnitId = settings.admobBannerId,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}
