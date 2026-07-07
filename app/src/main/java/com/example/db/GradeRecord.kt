package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_records")
data class GradeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleCode: String,
    val moduleName: String,
    val credits: Int,
    val semester: Int,
    val grade: String,
    val gradePoint: Double
)
