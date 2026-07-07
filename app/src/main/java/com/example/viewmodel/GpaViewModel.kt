package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.db.GradeRecord
import com.example.db.UserProfile
import com.example.repository.GpaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PredefinedModule(
    val code: String,
    val name: String,
    val credits: Int,
    val semester: Int,
    val degrees: List<String> = emptyList()
)

data class EligibilityResult(
    val isSafe: Boolean,
    val reasons: List<String>,
    val achievements: List<String>
)

class GpaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GpaRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GpaRepository(db.gpaDao())
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allGrades: StateFlow<List<GradeRecord>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faculties = listOf(
        "Computing and IT",
        "Engineering",
        "Science",
        "Business",
        "Music"
    )

    val degreesByFaculty = mapOf(
        "Computing and IT" to listOf(
            "BSc (Hons) in Software Engineering",
            "BSc (Hons) in Information Technology",
            "BSc (Hons) in Computer Science",
            "BSc (Hons) in Cyber Security",
            "BSc (Hons) in Data Science",
            "BSc (Hons) in Cloud Computing"
        ),
        "Engineering" to listOf(
            "BSc (Hons) in Engineering (Electronics & Power)",
            "BSc (Hons) in Engineering (Telecommunication)",
            "BSc (Hons) in Engineering (Civil)",
            "BSc (Hons) in Engineering (Mechatronics)"
        ),
        "Science" to listOf(
            "BSc (Hons) in Applied Science (Physical)",
            "BSc (Hons) in Applied Science (Biological)"
        ),
        "Business" to listOf(
            "BBA (Hons) in Human Resource Management",
            "BBA (Hons) in Marketing",
            "BBA (Hons) in Accounting & Finance"
        ),
        "Music" to listOf(
            "BBM (Hons) in Commercial Music"
        )
    )

    val availableDegrees = degreesByFaculty.values.flatten()

    val degreeToTypeMap = mapOf(
        "BSc (Hons) in Software Engineering" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Information Technology" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Computer Science" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Cyber Security" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Data Science" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Cloud Computing" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Engineering (Electronics & Power)" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Engineering (Telecommunication)" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Engineering (Civil)" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Engineering (Mechatronics)" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Applied Science (Physical)" to "Honours (Engineering/Tech/IT/Music)",
        "BSc (Hons) in Applied Science (Biological)" to "Honours (Engineering/Tech/IT/Music)",
        "BBA (Hons) in Human Resource Management" to "Honours (Management/Tourism/Hospitality)",
        "BBA (Hons) in Marketing" to "Honours (Management/Tourism/Hospitality)",
        "BBA (Hons) in Accounting & Finance" to "Honours (Management/Tourism/Hospitality)",
        "BBM (Hons) in Commercial Music" to "Honours (Engineering/Tech/IT/Music)"
    )

    // List of predefined modules for Computing & IT
    val predefinedModules: List<PredefinedModule> = run {
        val SE = "BSc (Hons) in Software Engineering"
        val IT = "BSc (Hons) in Information Technology"
        val CS = "BSc (Hons) in Computer Science"
        val CY = "BSc (Hons) in Cyber Security"
        val DS = "BSc (Hons) in Data Science"
        val CL = "BSc (Hons) in Cloud Computing"
        val ALL = listOf(SE, IT, CS, CY, DS, CL)

        listOf(
            // Semester 1
            PredefinedModule("SPH0201", "Introductory Physics", 3, 1, listOf(CS, IT)),
            PredefinedModule("ICO1301", "Programming Fundamentals with Python", 3, 1, ALL),
            PredefinedModule("CCS102", "Internet Technologies", 3, 1, ALL),
            PredefinedModule("CCS101", "Computer Systems", 3, 1, ALL),
            PredefinedModule("SMA0301", "Pre-Calculus", 3, 1, ALL),
            PredefinedModule("CSD0001", "Foundation in English Language Skills", 1, 1, ALL),

            // Semester 2
            PredefinedModule("SMA0302", "Introductory Calculus", 3, 2, listOf(SE, CS, DS, CL)),
            PredefinedModule("CCS1306", "Information Security", 3, 2, listOf(CY, IT, CS, CL)),
            PredefinedModule("CCS1305", "Communication Models and Protocols", 3, 2, listOf(CS, CY, CL, IT)),
            PredefinedModule("CCS1304", "Data Technologies/Essentials of Database", 3, 2, ALL),
            PredefinedModule("CSD1001", "Introduction to Academic Composition and Communication", 1, 2, ALL),
            PredefinedModule("CCS1303", "Object Oriented Programming", 3, 2, ALL),

            // Semester 3
            PredefinedModule("SCH0201", "Introductory Chemistry", 0, 3, listOf(CS, SE)),
            PredefinedModule("IMA1401", "Introductory Mathematical Analysis", 0, 3, listOf(CS, DS, SE)),
            PredefinedModule("SEC0301", "Introduction to Principles of Economics", 3, 3, listOf(SE, IT, CL)),
            PredefinedModule("CCS2300", "Data Structures and Algorithms", 3, 3, ALL),
            PredefinedModule("CCS2301", "Business Analysis and Software Design", 3, 3, listOf(SE, IT)),
            PredefinedModule("CCS2302", "Cloud Computing Fundamentals", 3, 3, listOf(CL, IT, SE, CY)),

            // Semester 4
            PredefinedModule("SMA2306", "Probability and Statistics", 3, 4, ALL),
            PredefinedModule("SMA2301", "Linear Algebra", 3, 4, listOf(CS, DS, SE)),
            PredefinedModule("CCS2303", "Operating Systems and Platforms", 3, 4, listOf(CS, SE, IT, CY, CL)),
            PredefinedModule("CCS2311", "Human Factors in Computer Systems", 3, 4, listOf(SE, IT, CS)),
            PredefinedModule("CCS2313", "Project Management", 3, 4, listOf(SE, IT, CL, CY, DS)),
            PredefinedModule("CCS2360", "Technology Challenge Competition 1", 3, 4, ALL),

            // Semester 5
            PredefinedModule("CCS3356", "Natural Language Processing", 3, 5, listOf(CS, DS)),
            PredefinedModule("CCS3311", "Software Quality Assuarance", 3, 5, listOf(SE, IT)),
            PredefinedModule("CCS3308", "Virtualization and Containers", 3, 5, listOf(CL, CY, IT)),
            PredefinedModule("CCS3360", "Technology Challenge Competition 2", 2, 5, ALL),
            PredefinedModule("CCS3310", "Software Engineering Methods", 3, 5, listOf(SE, CS, IT)),
            PredefinedModule("CCS3300", "Software Architecture", 3, 5, listOf(SE, CS)),
            PredefinedModule("SMA2307", "Discrete Mathematics", 3, 5, listOf(CS, SE, DS, CY)),

            // Semester 6
            PredefinedModule("CCS3351", "Mobile Application Development", 3, 6, listOf(SE, IT, CS)),
            PredefinedModule("CCS3302", "Introduction to Research Methods", 3, 6, ALL),
            PredefinedModule("CCS3312", "Cloud Application Development", 3, 6, listOf(CL, SE, IT)),
            PredefinedModule("CCS3316", "Cloud Infrastructure Design", 3, 6, listOf(CL, CY)),
            PredefinedModule("CCS3313", "Advanced Software Design", 3, 6, listOf(SE, CS)),
            PredefinedModule("CCS3440", "Artificial Intelligence", 4, 6, listOf(CS, DS, SE)),
            PredefinedModule("CCS3301", "Capstone Project Part 1", 3, 6, ALL),

            // Semester 7
            PredefinedModule("IHM1301", "Human Behavior and Ethics", 3, 7, ALL),
            PredefinedModule("CCS3341", "SOA and Microservices", 3, 7, listOf(SE, IT, CL)),
            PredefinedModule("SMA2305", "Numerical Analysis", 3, 7, listOf(CS, DS)),
            PredefinedModule("CCS4340", "Machine Learning", 3, 7, listOf(CS, DS, SE)),
            PredefinedModule("CCS4351", "Functional Programming", 3, 7, listOf(CS, SE)),
            PredefinedModule("CCS3301", "Capstone Project Part 2", 3, 7, ALL)
        )
    }

    val gradePoints = mapOf(
        "A+" to 4.00,
        "A" to 4.00,
        "A-" to 3.70,
        "B+" to 3.30,
        "B" to 3.00,
        "B-" to 2.70,
        "C+" to 2.30,
        "C" to 2.00,
        "C-" to 1.70,
        "D+" to 1.30,
        "D" to 1.00,
        "E" to 0.00,
        "I" to 0.00,
        "P" to 0.00 // Non-GPA pass
    )

    fun getGradePointValue(grade: String): Double {
        return gradePoints[grade] ?: 0.0
    }

    // Combined state for GPA and Eligibility
    val eligibilityState: StateFlow<EligibilityResult> = combine(userProfile, allGrades) { profile, grades ->
        if (profile == null) {
            EligibilityResult(true, emptyList(), emptyList())
        } else {
            validateEligibility(profile, grades)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EligibilityResult(true, emptyList(), emptyList())
    )

    fun onboardUser(degreeName: String, degreeType: String, indexNumber: String, faculty: String, fullName: String) {
        viewModelScope.launch {
            val profile = UserProfile(
                degreeName = degreeName,
                degreeType = degreeType,
                indexNumber = indexNumber,
                faculty = faculty,
                fullName = fullName,
                isOnboarded = true
            )
            repository.saveUserProfile(profile)
        }
    }

    fun updateProfile(degreeName: String, degreeType: String, indexNumber: String, faculty: String, themeMode: String, fullName: String = "") {
        viewModelScope.launch {
            val profile = UserProfile(
                degreeName = degreeName,
                degreeType = degreeType,
                indexNumber = indexNumber,
                faculty = faculty,
                fullName = fullName,
                themeMode = themeMode,
                isOnboarded = true
            )
            repository.saveUserProfile(profile)
        }
    }

    fun addGrade(moduleCode: String, moduleName: String, credits: Int, semester: Int, grade: String) {
        viewModelScope.launch {
            val gradePoint = getGradePointValue(grade)
            val record = GradeRecord(
                moduleCode = moduleCode,
                moduleName = moduleName,
                credits = credits,
                semester = semester,
                grade = grade,
                gradePoint = gradePoint
            )
            repository.insertGrade(record)
        }
    }

    fun updateGrade(id: Int, moduleCode: String, moduleName: String, credits: Int, semester: Int, grade: String) {
        viewModelScope.launch {
            val gradePoint = getGradePointValue(grade)
            val record = GradeRecord(
                id = id,
                moduleCode = moduleCode,
                moduleName = moduleName,
                credits = credits,
                semester = semester,
                grade = grade,
                gradePoint = gradePoint
            )
            repository.insertGrade(record)
        }
    }

    fun deleteGrade(id: Int) {
        viewModelScope.launch {
            repository.deleteGrade(id)
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            repository.resetApp()
        }
    }

    // Main Validation Engine based on official rules
    private fun validateEligibility(profile: UserProfile, grades: List<GradeRecord>): EligibilityResult {
        val reasons = mutableListOf<String>()
        val achievements = mutableListOf<String>()

        val gpaRecords = grades.filter { it.credits > 0 && it.grade != "I" && it.grade != "P" }
        val totalCreditsEarned = grades.filter { getGradePointValue(it.grade) >= 1.00 || it.grade == "P" }.sumOf { it.credits }
        val overallGpa = if (gpaRecords.isNotEmpty()) {
            gpaRecords.sumOf { it.credits * it.gradePoint } / gpaRecords.sumOf { it.credits }
        } else {
            0.0
        }

        achievements.add("Accumulated $totalCreditsEarned credits (of 120 required for graduation)")
        achievements.add("Overall Cumulative GPA: ${"%.2f".format(overallGpa)}")

        // Helper to compute GPA for a set of grades
        fun computeGpa(list: List<GradeRecord>): Double {
            val subset = list.filter { it.credits > 0 && it.grade != "I" && it.grade != "P" }
            if (subset.isEmpty()) return 0.0
            return subset.sumOf { it.credits * it.gradePoint } / subset.sumOf { it.credits }
        }

        // Helper to categorize level based on semester
        fun getLevelGrades(level: Int): List<GradeRecord> {
            return when (level) {
                1 -> grades.filter { it.semester in 1..2 }
                2 -> grades.filter { it.semester in 3..4 }
                3 -> grades.filter { it.semester in 5..6 }
                4 -> grades.filter { it.semester >= 7 }
                else -> emptyList()
            }
        }

        when (profile.degreeType) {
            "Honours (Engineering/Tech/IT/Music)" -> {
                // Rule 1: Accumulate D or better in subjects aggregating min 120 credits
                for (level in 1..4) {
                    val levelGrades = getLevelGrades(level)
                    val levelCredits = levelGrades.filter { getGradePointValue(it.grade) >= 1.00 || it.grade == "P" }.sumOf { it.credits }
                    if (levelGrades.isNotEmpty()) {
                        achievements.add("Level $level credits accumulated: $levelCredits")
                    }
                }

                // Rule 2: Obtain GPA 2.00 or above in Level IV
                val levelIVGrades = getLevelGrades(4)
                if (levelIVGrades.isNotEmpty()) {
                    val levelIVGpa = computeGpa(levelIVGrades)
                    if (levelIVGpa < 2.00) {
                        reasons.add("Level IV GPA is ${"%.2f".format(levelIVGpa)} (Minimum 2.00 required)")
                    } else {
                        achievements.add("✓ Level IV GPA is Safe: ${"%.2f".format(levelIVGpa)}")
                    }
                }

                // Rule 3: Maintain GPA 2.00 or above in each level
                for (level in 1..4) {
                    val levelGrades = getLevelGrades(level)
                    if (levelGrades.isNotEmpty()) {
                        val levelGpa = computeGpa(levelGrades)
                        if (levelGpa < 2.00) {
                            reasons.add("Level $level GPA is ${"%.2f".format(levelGpa)} (Minimum 2.00 required)")
                        } else {
                            achievements.add("✓ Level $level GPA is Safe: ${"%.2f".format(levelGpa)}")
                        }
                    }
                }

                // Rule 4: Minimum overall GPA of 2.00
                if (grades.isNotEmpty() && overallGpa < 2.00) {
                    reasons.add("Overall Cumulative GPA is ${"%.2f".format(overallGpa)} (Minimum 2.00 required)")
                }

                // Rule 5: Should obtain at least a "D" grade for all GPA Courses
                val lowGpaCourses = gpaRecords.filter { getGradePointValue(it.grade) < 1.00 }
                if (lowGpaCourses.isNotEmpty()) {
                    val list = lowGpaCourses.joinToString { it.moduleCode }
                    reasons.add("Failed courses present: $list must be D or better")
                }
            }

            "Honours (Management/Tourism/Hospitality)" -> {
                // Rule 1: Overall GPA of 2.00
                if (grades.isNotEmpty() && overallGpa < 2.00) {
                    reasons.add("Overall Cumulative GPA is ${"%.2f".format(overallGpa)} (Minimum 2.00 required)")
                }

                // Rule 2: Minimum semester GPA of 2.00
                val semestersPresent = grades.map { it.semester }.distinct()
                for (sem in semestersPresent) {
                    val semGrades = grades.filter { it.semester == sem }
                    val semGpa = computeGpa(semGrades)
                    if (semGpa < 2.00) {
                        reasons.add("Semester $sem GPA is ${"%.2f".format(semGpa)} (Minimum 2.00 required)")
                    }
                }

                // Rule 3: No fail grade (E)
                val failGrades = grades.filter { it.grade == "E" }
                if (failGrades.isNotEmpty()) {
                    reasons.add("Fail grade (E) present in: ${failGrades.joinToString { it.moduleCode }}")
                }

                // Rule 4: Not more than one poor grade (C-, D+, D) per semester and total <= 4
                val poorGrades = grades.filter { it.grade in listOf("C-", "D+", "D") }
                if (poorGrades.size > 4) {
                    reasons.add("Total poor grades (C-, D+, D) is ${poorGrades.size} (Maximum 4 allowed for the program)")
                }

                for (sem in semestersPresent) {
                    val semPoor = grades.filter { it.semester == sem && it.grade in listOf("C-", "D+", "D") }
                    if (semPoor.size > 1) {
                        reasons.add("Semester $sem has ${semPoor.size} poor grades: ${semPoor.joinToString { it.moduleCode }} (Max 1 allowed per semester)")
                    }
                }
            }

            "General (Except Tourism/Hospitality)" -> {
                // Rule 1: Track credits from Level I, II, III
                for (level in 1..3) {
                    val levelGrades = getLevelGrades(level)
                    val levelCredits = levelGrades.filter { getGradePointValue(it.grade) >= 1.00 || it.grade == "P" }.sumOf { it.credits }
                    if (levelGrades.isNotEmpty()) {
                        achievements.add("Level $level credits accumulated: $levelCredits")
                    }
                }

                // Rule 2: Maintain GPA 2.00 or above in each level
                for (level in 1..3) {
                    val levelGrades = getLevelGrades(level)
                    if (levelGrades.isNotEmpty()) {
                        val levelGpa = computeGpa(levelGrades)
                        if (levelGpa < 2.00) {
                            reasons.add("Level $level GPA is ${"%.2f".format(levelGpa)} (Minimum 2.00 required)")
                        }
                    }
                }

                // Rule 3: Obtain GPA 2.00 or above for Level III courses
                val levelIIIGrades = getLevelGrades(3)
                if (levelIIIGrades.isNotEmpty()) {
                    val levelIIIGpa = computeGpa(levelIIIGrades)
                    if (levelIIIGpa < 2.00) {
                        reasons.add("Level III GPA is ${"%.2f".format(levelIIIGpa)} (Minimum 2.00 required)")
                    }
                }

                // Rule 4: Minimum overall GPA of 2.00
                if (grades.isNotEmpty() && overallGpa < 2.00) {
                    reasons.add("Overall Cumulative GPA is ${"%.2f".format(overallGpa)} (Minimum 2.00 required)")
                }
            }

            "General (Tourism/Hospitality)" -> {
                // Rule 1: Minimum semester GPA of 2.00
                val semestersPresent = grades.map { it.semester }.distinct()
                for (sem in semestersPresent) {
                    val semGrades = grades.filter { it.semester == sem }
                    val semGpa = computeGpa(semGrades)
                    if (semGpa < 2.00) {
                        reasons.add("Semester $sem GPA is ${"%.2f".format(semGpa)} (Minimum 2.00 required)")
                    }
                }

                // Rule 2: Minimum overall GPA of 2.00
                if (grades.isNotEmpty() && overallGpa < 2.00) {
                    reasons.add("Overall Cumulative GPA is ${"%.2f".format(overallGpa)} (Minimum 2.00 required)")
                }

                // Rule 3: No fail grade (E)
                val failGrades = grades.filter { it.grade == "E" }
                if (failGrades.isNotEmpty()) {
                    reasons.add("Fail grade (E) present in: ${failGrades.joinToString { it.moduleCode }}")
                }

                // Rule 4: Not more than one poor grade per semester and total <= 4
                val poorGrades = grades.filter { it.grade in listOf("C-", "D+", "D") }
                if (poorGrades.size > 4) {
                    reasons.add("Total poor grades (C-, D+, D) is ${poorGrades.size} (Maximum 4 allowed for the program)")
                }

                for (sem in semestersPresent) {
                    val semPoor = grades.filter { it.semester == sem && it.grade in listOf("C-", "D+", "D") }
                    if (semPoor.size > 1) {
                        reasons.add("Semester $sem has ${semPoor.size} poor grades: ${semPoor.joinToString { it.moduleCode }} (Max 1 allowed per semester)")
                    }
                }
            }
        }

        // Default "Unsafe" if overall GPA is 0 but they have some courses, or check if they have some errors
        val isSafe = reasons.isEmpty()

        return EligibilityResult(isSafe, reasons, achievements)
    }
}
