package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.Yellow
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.db.GradeRecord
import com.example.db.UserProfile
import com.example.viewmodel.GpaViewModel
import com.example.viewmodel.PredefinedModule
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GpaViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val grades by viewModel.allGrades.collectAsStateWithLifecycle()
    val eligibility by viewModel.eligibilityState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) }

    val isOnboarding = profile == null || !profile!!.isOnboarded

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = isOnboarding,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "onboardingToAppTransition",
            modifier = Modifier.fillMaxSize()
        ) { showOnboarding ->
            if (showOnboarding) {
                OnboardingView(
                    faculties = viewModel.faculties,
                    degreesByFaculty = viewModel.degreesByFaculty,
                    degreeToTypeMap = viewModel.degreeToTypeMap,
                    onOnboard = { degreeName, degreeType, indexNumber, faculty, fullName ->
                        viewModel.onboardUser(degreeName, degreeType, indexNumber, faculty, fullName)
                    }
                )
            } else {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            windowInsets = NavigationBarDefaults.windowInsets
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                modifier = Modifier.testTag("dashboard_tab")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Grades") },
                                label = { Text("Grades") },
                                modifier = Modifier.testTag("grades_tab")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Grade") },
                                label = { Text("Add Grade") },
                                modifier = Modifier.testTag("add_grade_tab")
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile") },
                                modifier = Modifier.testTag("profile_tab")
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width } + fadeOut()
                                    )
                                } else {
                                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width } + fadeOut()
                                    )
                                }
                            },
                            label = "tabTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> DashboardView(
                                    profile = profile!!,
                                    grades = grades,
                                    isSafe = eligibility.isSafe,
                                    reasons = eligibility.reasons,
                                    achievements = eligibility.achievements,
                                    isDarkTheme = isDarkTheme
                                )
                                1 -> GradesListView(
                                    grades = grades,
                                    gradesList = viewModel.gradePoints.keys.toList(),
                                    isDarkTheme = isDarkTheme,
                                    onDelete = { viewModel.deleteGrade(it) },
                                    onUpdate = { id, code, name, credits, sem, grade ->
                                        viewModel.updateGrade(id, code, name, credits, sem, grade)
                                    }
                                )
                                2 -> {
                                    AddGradeView(
                                        predefinedModules = viewModel.predefinedModules,
                                        gradesList = viewModel.gradePoints.keys.toList(),
                                        onAddGrade = { code, name, credits, sem, grade ->
                                            viewModel.addGrade(code, name, credits, sem, grade)
                                        }
                                    )
                                }
                                3 -> {
                                    ProfileView(
                                        profile = profile!!,
                                        availableDegrees = viewModel.availableDegrees,
                                        degreeToTypeMap = viewModel.degreeToTypeMap,
                                        onUpdateProfile = { degreeName, degreeType, indexNum, facultyName, theme, fullName ->
                                            viewModel.updateProfile(degreeName, degreeType, indexNum, facultyName, theme, fullName)
                                        },
                                        onResetApp = { viewModel.resetApp() }
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
fun OnboardingView(
    faculties: List<String>,
    degreesByFaculty: Map<String, List<String>>,
    degreeToTypeMap: Map<String, String>,
    onOnboard: (degreeName: String, degreeType: String, indexNumber: String, faculty: String, fullName: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    // Step 1 states
    var fullName by remember { mutableStateOf("") }
    var indexNumber by remember { mutableStateOf("") }

    // Step 2 states
    var selectedFaculty by remember { mutableStateOf("") }

    // Step 3 states
    val filteredDegrees = remember(selectedFaculty) {
        (degreesByFaculty[selectedFaculty] ?: emptyList()) + "Other (Custom Degree)"
    }
    var selectedDegree by remember(selectedFaculty) {
        mutableStateOf(if (degreesByFaculty[selectedFaculty]?.isNotEmpty() == true) degreesByFaculty[selectedFaculty]!![0] else "Other (Custom Degree)")
    }
    var customDegreeName by remember { mutableStateOf("") }
    
    val degreeTypes = listOf(
        "Honours (Engineering/Tech/IT/Music)",
        "Honours (Management/Tourism/Hospitality)",
        "General (Except Tourism/Hospitality)",
        "General (Tourism/Hospitality)"
    )
    var selectedType by remember(selectedDegree) {
        mutableStateOf(degreeToTypeMap[selectedDegree] ?: "Honours (Engineering/Tech/IT/Music)")
    }

    var degreeExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section: Progress indicator & Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Step badge & Progress row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..3).forEach { step ->
                    val isCompletedOrCurrent = step <= currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isCompletedOrCurrent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Step $currentStep of 3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Middle Section: Content depending on step
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "onboardingStepTransition",
                modifier = Modifier.fillMaxWidth()
            ) { targetStep ->
                when (targetStep) {
                    1 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = "Personal Details",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Please enter your full name and index number to get started.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. John Doe") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_fullname"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = indexNumber,
                            onValueChange = { indexNumber = it },
                            label = { Text("Index Number") },
                            placeholder = { Text("e.g. CO/IT/2021/0001") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_index_number"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = "Select Faculty",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Choose the academic department or faculty you belong to.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Render beautiful interactive card selectors for faculties
                        faculties.forEach { faculty ->
                            val isSelected = selectedFaculty == faculty
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFaculty = faculty }
                                    .testTag("faculty_option_${faculty.replace(" ", "_")}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (faculty) {
                                                "Computing and IT" -> Icons.Default.School
                                                "Engineering" -> Icons.Default.Business
                                                "Science" -> Icons.Default.School
                                                "Business" -> Icons.Default.Business
                                                else -> Icons.Default.School
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = faculty,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = "Select Degree & Program",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Select your specific degree program and its corresponding type in $selectedFaculty.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dropdown Degree Name Selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedDegree,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Degree Name") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { degreeExpanded = true }
                                    .testTag("onboarding_degree_dropdown"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { degreeExpanded = true }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = degreeExpanded,
                                onDismissRequest = { degreeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                filteredDegrees.forEach { degree ->
                                    DropdownMenuItem(
                                        text = { Text(degree) },
                                        onClick = {
                                            selectedDegree = degree
                                            if (degree != "Other (Custom Degree)") {
                                                selectedType = degreeToTypeMap[degree] ?: "Honours (Engineering/Tech/IT/Music)"
                                            }
                                            degreeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // If "Other (Custom Degree)" is selected, show custom text input
                        if (selectedDegree == "Other (Custom Degree)") {
                            OutlinedTextField(
                                value = customDegreeName,
                                onValueChange = { customDegreeName = it },
                                label = { Text("Custom Degree Name") },
                                placeholder = { Text("e.g. BSc (Hons) in Electronics") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_degree_input"),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Dropdown Degree Type Selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Degree Program Type") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { typeExpanded = true }
                                    .testTag("onboarding_type_dropdown"),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { typeExpanded = true }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                degreeTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Bottom Section: Navigation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("onboarding_back"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }

            val isStepValid = when (currentStep) {
                1 -> fullName.isNotBlank() && indexNumber.isNotBlank()
                2 -> selectedFaculty.isNotBlank()
                3 -> {
                    val finalDegree = if (selectedDegree == "Other (Custom Degree)") customDegreeName else selectedDegree
                    finalDegree.isNotBlank()
                }
                else -> false
            }

            Button(
                onClick = {
                    if (isStepValid) {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            val finalDegree = if (selectedDegree == "Other (Custom Degree)") customDegreeName else selectedDegree
                            onOnboard(
                                finalDegree.trim(),
                                selectedType,
                                indexNumber.trim(),
                                selectedFaculty,
                                fullName.trim()
                            )
                        }
                    }
                },
                enabled = isStepValid,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(if (currentStep < 3) "onboarding_next_step_$currentStep" else "onboarding_submit"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentStep < 3) "Next" else "Finish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (currentStep < 3) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    profile: UserProfile,
    grades: List<GradeRecord>,
    isSafe: Boolean,
    reasons: List<String>,
    achievements: List<String>,
    isDarkTheme: Boolean
) {
    // Math to compute Cumulative GPA
    val gpaRecords = grades.filter { it.credits > 0 && it.grade != "I" && it.grade != "P" }
    val cumulativeGpa = if (gpaRecords.isNotEmpty()) {
        gpaRecords.sumOf { it.credits * it.gradePoint } / gpaRecords.sumOf { it.credits }
    } else {
        0.0
    }
    val totalCreditsEarned = grades.filter { viewModel ->
        val gp = when (viewModel.grade) {
            "A+", "A" -> 4.0
            "A-" -> 3.7
            "B+" -> 3.3
            "B" -> 3.0
            "B-" -> 2.7
            "C+" -> 2.3
            "C" -> 2.0
            "C-" -> 1.7
            "D+" -> 1.3
            "D" -> 1.0
            else -> 0.0
        }
        gp >= 1.00 || viewModel.grade == "P"
    }.sumOf { it.credits }

    // Find current Level based on added semesters
    val maxSemester = grades.maxOfOrNull { it.semester } ?: 1
    val currentLevelName = when (maxSemester) {
        in 1..2 -> "LEVEL I"
        in 3..4 -> "LEVEL II"
        in 5..6 -> "LEVEL III"
        else -> "LEVEL IV"
    }

    // Level GPA calculation for current active level
    val currentLevelInt = when (maxSemester) {
        in 1..2 -> 1
        in 3..4 -> 2
        in 5..6 -> 3
        else -> 4
    }

    fun getLevelGpaValue(lvl: Int): Double {
        val semRange = when (lvl) {
            1 -> 1..2
            2 -> 3..4
            3 -> 5..6
            else -> 7..8
        }
        val lvlGrades = grades.filter { it.semester in semRange && it.credits > 0 && it.grade != "I" && it.grade != "P" }
        if (lvlGrades.isEmpty()) return 0.0
        return lvlGrades.sumOf { it.credits * it.gradePoint } / lvlGrades.sumOf { it.credits }
    }

    val activeLevelGpa = getLevelGpaValue(currentLevelInt)

    // Evaluate some simple compliance checks for visual badges
    val hasEGrades = grades.any { it.grade == "E" }
    val poorGradesCount = grades.count { it.grade in listOf("C-", "D+", "D") }
    val overallGpaSafe = cumulativeGpa >= 2.00 || grades.isEmpty()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(1500)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome and Track Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ACADEMIC RADAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = profile.degreeName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Rule Class: ${profile.degreeType}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // BENTO GRID ROW 1: GPA & Status Bento
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PRIMARY GPA BENTO (col-span-4 equivalent)
                Card(
                    modifier = Modifier
                        .weight(1.8f)
                        .height(150.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF27337B) // Always deep blue Blue1 from prototype
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CURRENT CUMULATIVE GPA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "%.2f".format(cumulativeGpa),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE5A949), // Primary Gold
                                letterSpacing = (-1).sp,
                                modifier = Modifier.testTag("gpa_display")
                            )
                            Text(
                                text = " / 4.00",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Beautiful GPA Progress Bar
                        val progressFraction = (cumulativeGpa / 4.0).toFloat().coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .background(Color(0xFFE5A949), CircleShape)
                                )
                            }
                            Text(
                                text = "${gpaRecords.sumOf { it.credits }} GPA Credits factored",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // STATUS BENTO (col-span-2 equivalent)
                val statusBg = MaterialTheme.colorScheme.surfaceVariant
                val statusText = if (isSafe) "SAFE" else "UNSAFE"
                val statusColor = if (isSafe) Color(0xFF4CAF50) else Color(0xFFF44336)
                val statusIcon = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning

                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(150.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = statusText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            modifier = Modifier.testTag("status_pill")
                        )
                    }
                }
            }
        }

        // BENTO GRID ROW 2: Credits & Level GPA Bento
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CREDITS PROGRESS BENTO
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CREDITS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentLevelName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$totalCreditsEarned",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " / 120",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                            Text(
                                text = "Credits tracked per level",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // LEVEL GPA BENTO
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$currentLevelName GPA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )

                        Column {
                            Text(
                                text = if (activeLevelGpa > 0.0) "%.2f".format(activeLevelGpa) else "0.00",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2294D3) // Blue2 accent
                            )
                            Text(
                                text = "Threshold: 2.00 Min",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // BENTO GRID ROW 3: Degree Compliance checklist Bento
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DEGREE COMPLIANCE CHECKLIST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FactCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2x2 Compliance Checklist Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Check 1: E grades
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (!hasEGrades) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (!hasEGrades) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "No E Fail Grades",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Check 2: Poor Grades Count
                            val poorGradesSafe = poorGradesCount <= 4
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (poorGradesSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (poorGradesSafe) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Poor Grades ($poorGradesCount/4)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Check 3: Cumulative GPA
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (overallGpaSafe) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (overallGpaSafe) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Overall GPA >= 2.00",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Check 4: 120 Credits progress
                            val creditsTargetMet = totalCreditsEarned >= 120
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (creditsTargetMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (creditsTargetMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (creditsTargetMet) "120 Cr. Reached" else "120 Cr. Target",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Compliancy warnings list if Unsafe
        if (!isSafe && reasons.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Graduation Roadblocks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        reasons.forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = reason,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Achievements list
        if (achievements.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Academic Milestones",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        achievements.forEach { ach ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Met",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ach,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // LEVEL BREAKDOWNS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "LEVEL BREAKDOWNS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Compute breakdowns dynamically
                    for (level in 1..4) {
                        val semRange = when (level) {
                            1 -> 1..2
                            2 -> 3..4
                            3 -> 5..6
                            else -> 7..8
                        }
                        val levelGrades = grades.filter { it.semester in semRange }
                        val levelGpaRecs = levelGrades.filter { it.credits > 0 && it.grade != "I" && it.grade != "P" }
                        val levelGpa = if (levelGpaRecs.isNotEmpty()) {
                            levelGpaRecs.sumOf { it.credits * it.gradePoint } / levelGpaRecs.sumOf { it.credits }
                        } else {
                            0.0
                        }
                        val levelCredits = levelGrades.sumOf { it.credits }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Level $level (Semesters ${semRange.first}-${semRange.last})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$levelCredits total credits added",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Text(
                                text = if (levelGpaRecs.isNotEmpty()) "GPA: ${"%.2f".format(levelGpa)}" else "N/A",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (levelGpaRecs.isNotEmpty() && levelGpa >= 2.00) Color(0xFF4CAF50) else if (levelGpaRecs.isNotEmpty()) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (level < 4) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun GradesListView(
    grades: List<GradeRecord>,
    gradesList: List<String>,
    isDarkTheme: Boolean,
    onDelete: (Int) -> Unit,
    onUpdate: (Int, String, String, Int, Int, String) -> Unit
) {
    var editingRecord by remember { mutableStateOf<GradeRecord?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (grades.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Grades Added Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Switch to 'Add Grade' tab to input your course results and monitor your academic safety.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Results (${grades.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap Edit to modify or delete",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                items(grades, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Line 1: Module Code + Name, and Edit button (larger)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${record.moduleCode} - ${record.moduleName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Yellow,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                IconButton(
                                    onClick = { editingRecord = record },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("edit_grade_button_${record.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            // Line 2: Semester, Credits, and Grade as Chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val chipBg = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color(0xFFE5EDF6)
                                val chipText = if (isDarkTheme) Color.White else Color(0xFF27337B)
                                
                                Box(
                                    modifier = Modifier
                                        .background(chipBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Semester ${record.semester}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipText
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(chipBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    val creditsText = if (record.credits == 1) "Credit" else "Credits"
                                    Text(
                                        text = "${record.credits} $creditsText",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipText
                                    )
                                }

                                val gradeColor = if (record.gradePoint >= 2.00) Color(0xFF4CAF50) else Color(0xFFF44336)
                                Box(
                                    modifier = Modifier
                                        .background(gradeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Grade: ${record.grade}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gradeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Edit Dialog overlay
        editingRecord?.let { record ->
            EditGradeDialog(
                record = record,
                gradesList = gradesList,
                onDismiss = { editingRecord = null },
                onSave = { updated ->
                    onUpdate(updated.id, updated.moduleCode, updated.moduleName, updated.credits, updated.semester, updated.grade)
                    editingRecord = null
                },
                onDelete = {
                    onDelete(record.id)
                    editingRecord = null
                }
            )
        }
    }
}

@Composable
fun EditGradeDialog(
    record: GradeRecord,
    gradesList: List<String>,
    onDismiss: () -> Unit,
    onSave: (GradeRecord) -> Unit,
    onDelete: () -> Unit
) {
    var moduleCode by remember { mutableStateOf(record.moduleCode) }
    var moduleName by remember { mutableStateOf(record.moduleName) }
    var creditsStr by remember { mutableStateOf(record.credits.toString()) }
    var semesterStr by remember { mutableStateOf(record.semester.toString()) }
    var selectedGrade by remember { mutableStateOf(record.grade) }

    var gradeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Course Result",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = moduleCode,
                    onValueChange = {},
                    label = { Text("Module Code") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_module_code"),
                    singleLine = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = moduleName,
                    onValueChange = {},
                    label = { Text("Module Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_module_name"),
                    singleLine = true,
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = creditsStr,
                    onValueChange = { creditsStr = it },
                    label = { Text("Credits") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_module_credits"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = semesterStr,
                    onValueChange = { semesterStr = it },
                    label = { Text("Semester") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_module_semester"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Grade Select
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { gradeDropdownExpanded = true }
                            .testTag("edit_module_grade"),
                        trailingIcon = {
                            IconButton(onClick = { gradeDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(
                        expanded = gradeDropdownExpanded,
                        onDismissRequest = { gradeDropdownExpanded = false }
                    ) {
                        gradesList.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    selectedGrade = grade
                                    gradeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val credits = creditsStr.toIntOrNull() ?: 0
            val semester = semesterStr.toIntOrNull() ?: 1
            val isValid = moduleCode.isNotBlank() && moduleName.isNotBlank() && credits >= 0 && semester in 1..8

            Button(
                onClick = {
                    if (isValid) {
                        onSave(
                            record.copy(
                                moduleCode = moduleCode.trim().uppercase(),
                                moduleName = moduleName.trim(),
                                credits = credits,
                                semester = semester,
                                grade = selectedGrade
                            )
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier.testTag("save_edit_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_grade_button_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_edit_button")
                ) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddGradeView(
    predefinedModules: List<PredefinedModule>,
    gradesList: List<String>,
    onAddGrade: (String, String, Int, Int, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var moduleCodeSearch by remember { mutableStateOf("") }
    var moduleName by remember { mutableStateOf("") }
    var creditsStr by remember { mutableStateOf("") }
    var semesterStr by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("A") }

    var codeDropdownExpanded by remember { mutableStateOf(false) }
    var gradeDropdownExpanded by remember { mutableStateOf(false) }

    val isPredefined = remember(moduleCodeSearch, predefinedModules) {
        predefinedModules.any { it.code.equals(moduleCodeSearch.trim(), ignoreCase = true) }
    }

    // Filter matching predefined modules (fuzzy find by code or name using term tokens)
    val matchingPredefined = remember(moduleCodeSearch) {
        if (moduleCodeSearch.isBlank()) emptyList()
        else {
            val terms = moduleCodeSearch.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (terms.isEmpty()) emptyList()
            else {
                predefinedModules.filter { pm ->
                    val codeLower = pm.code.lowercase()
                    val nameLower = pm.name.lowercase()
                    terms.all { term -> codeLower.contains(term) || nameLower.contains(term) }
                }
            }
        }
    }

    // React to matchingPredefined changes to auto-populate if exact match
    LaunchedEffect(moduleCodeSearch) {
        val exactMatch = predefinedModules.find { it.code.equals(moduleCodeSearch.trim(), ignoreCase = true) }
        if (exactMatch != null) {
            moduleName = exactMatch.name
            creditsStr = exactMatch.credits.toString()
            semesterStr = exactMatch.semester.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Add Course Result",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Search a predefined module code or name to auto-populate, or type a custom module.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Module Code Autocomplete TextField
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = moduleCodeSearch,
                    onValueChange = {
                        if (!isPredefined) {
                            moduleCodeSearch = it
                            codeDropdownExpanded = true
                        }
                    },
                    readOnly = isPredefined,
                    label = { Text("Search Module (Code or Name)") },
                    placeholder = { Text("e.g. SPH0201 or Physics") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("module_code_input"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (moduleCodeSearch.isNotEmpty()) {
                            IconButton(onClick = {
                                moduleCodeSearch = ""
                                moduleName = ""
                                creditsStr = ""
                                semesterStr = ""
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                // Show suggestion dropdown
                if (codeDropdownExpanded && matchingPredefined.isNotEmpty()) {
                    DropdownMenu(
                        expanded = codeDropdownExpanded,
                        onDismissRequest = { codeDropdownExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 220.dp)
                    ) {
                        matchingPredefined.forEach { pm ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${pm.code} - ${pm.name}", fontWeight = FontWeight.Bold)
                                        Text("Semester ${pm.semester} • ${pm.credits} Credits", fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    moduleCodeSearch = pm.code
                                    moduleName = pm.name
                                    creditsStr = pm.credits.toString()
                                    semesterStr = pm.semester.toString()
                                    codeDropdownExpanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Module Name
        item {
            OutlinedTextField(
                value = moduleName,
                onValueChange = { if (!isPredefined) moduleName = it },
                readOnly = isPredefined,
                label = { Text("Module Name") },
                placeholder = { Text("e.g. Introductory Physics") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("module_name_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Credits & Semester side by side
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = creditsStr,
                    onValueChange = { if (!isPredefined) creditsStr = it },
                    readOnly = isPredefined,
                    label = { Text("Credits") },
                    placeholder = { Text("e.g. 3") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("module_credits_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = semesterStr,
                    onValueChange = { if (!isPredefined) semesterStr = it },
                    readOnly = isPredefined,
                    label = { Text("Semester") },
                    placeholder = { Text("e.g. 1") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("module_semester_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Grade dropdown
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Earned Grade") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { gradeDropdownExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { gradeDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Grade")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = gradeDropdownExpanded,
                    onDismissRequest = { gradeDropdownExpanded = false }
                ) {
                    gradesList.forEach { grade ->
                        DropdownMenuItem(
                            text = { Text(grade) },
                            onClick = {
                                selectedGrade = grade
                                gradeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(16.dp))

            val isFormValid = moduleCodeSearch.isNotBlank() &&
                    moduleName.isNotBlank() &&
                    creditsStr.toIntOrNull() != null &&
                    semesterStr.toIntOrNull() != null

            Button(
                onClick = {
                    if (isFormValid) {
                        onAddGrade(
                            moduleCodeSearch.trim().uppercase(),
                            moduleName.trim(),
                            creditsStr.toInt(),
                            semesterStr.toInt(),
                            selectedGrade
                        )
                        // Reset form
                        moduleCodeSearch = ""
                        moduleName = ""
                        creditsStr = ""
                        semesterStr = ""
                        selectedGrade = "A"
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("add_grade_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Add Result",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    profile: UserProfile,
    availableDegrees: List<String>,
    degreeToTypeMap: Map<String, String>,
    onUpdateProfile: (degreeName: String, degreeType: String, indexNumber: String, faculty: String, themeMode: String, fullName: String) -> Unit,
    onResetApp: () -> Unit
) {
    var fullName by remember(profile) { mutableStateOf(profile.fullName) }
    var indexNumber by remember(profile) { mutableStateOf(profile.indexNumber) }
    var faculty by remember(profile) { mutableStateOf(profile.faculty) }
    var selectedDegree by remember(profile) { mutableStateOf(profile.degreeName) }
    var selectedType by remember(profile) { mutableStateOf(profile.degreeType) }
    var themeMode by remember(profile) { mutableStateOf(profile.themeMode) }

    var degreeExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var customDegreeName by remember { mutableStateOf("") }
    
    val actualAvailableDegrees = remember { availableDegrees + "Other (Custom Degree)" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Avatar Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (fullName.isNotEmpty()) fullName else "No Name",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (indexNumber.isNotEmpty()) indexNumber else "No Index Number",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (faculty.isNotEmpty()) faculty else "No Faculty Specified",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = selectedDegree,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: Academic Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Academic Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_fullname_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = indexNumber,
                        onValueChange = { indexNumber = it },
                        label = { Text("Index Number") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_index_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_faculty_input"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Degree Selection Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDegree,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Degree Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { degreeExpanded = true }
                                .testTag("profile_degree_dropdown"),
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = degreeExpanded,
                            onDismissRequest = { degreeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            actualAvailableDegrees.forEach { degree ->
                                DropdownMenuItem(
                                    text = { Text(degree) },
                                    onClick = {
                                        selectedDegree = degree
                                        degreeExpanded = false
                                        val mappedType = degreeToTypeMap[degree]
                                        if (mappedType != null) {
                                            selectedType = mappedType
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (selectedDegree == "Other (Custom Degree)") {
                        OutlinedTextField(
                            value = customDegreeName,
                            onValueChange = { customDegreeName = it },
                            label = { Text("Custom Degree Name") },
                            modifier = Modifier.fillMaxWidth().testTag("profile_custom_degree_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Degree Program Type Selection
                    val degreeTypes = listOf(
                        "Honours (Engineering/Tech/IT/Music)",
                        "Honours (Management/Tourism/Hospitality)",
                        "General (Except Tourism/Hospitality)",
                        "General (Tourism/Hospitality)"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Degree Program Type") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { typeExpanded = true }
                                .testTag("profile_type_dropdown"),
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            degreeTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedType = type
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Preferences (Theme Selector)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "App Theme Preference",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple("light", "Light", Icons.Default.LightMode),
                            Triple("dark", "Dark", Icons.Default.DarkMode),
                            Triple("auto", "Auto", Icons.Default.Settings)
                        )
                        
                        modes.forEach { (modeKey, modeName, icon) ->
                            val isSelected = themeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        themeMode = modeKey
                                        // Save immediately for dynamic reactive theme changes!
                                        val finalDegree = if (selectedDegree == "Other (Custom Degree)") {
                                            customDegreeName.ifEmpty { "Custom Degree" }
                                        } else {
                                            selectedDegree
                                        }
                                        onUpdateProfile(finalDegree, selectedType, indexNumber, faculty, modeKey, fullName)
                                    }
                                    .testTag("theme_option_$modeKey"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = modeName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Save & Reset
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val finalDegree = if (selectedDegree == "Other (Custom Degree)") {
                            customDegreeName.ifEmpty { "Custom Degree" }
                        } else {
                            selectedDegree
                        }
                        onUpdateProfile(finalDegree, selectedType, indexNumber, faculty, themeMode, fullName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_save_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                var showResetDialog by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_reset_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Application", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("Reset Entire App?") },
                        text = { Text("This will delete your user profile and all entered grades permanently. This action cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showResetDialog = false
                                    onResetApp()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reset")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}
