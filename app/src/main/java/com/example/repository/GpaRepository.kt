package com.example.repository

import com.example.db.GpaDao
import com.example.db.GradeRecord
import com.example.db.UserProfile
import kotlinx.coroutines.flow.Flow

class GpaRepository(private val gpaDao: GpaDao) {
    val userProfile: Flow<UserProfile?> = gpaDao.getUserProfile()
    val allGrades: Flow<List<GradeRecord>> = gpaDao.getAllGrades()

    suspend fun saveUserProfile(profile: UserProfile) {
        gpaDao.insertUserProfile(profile)
    }

    suspend fun insertGrade(gradeRecord: GradeRecord) {
        gpaDao.insertGrade(gradeRecord)
    }

    suspend fun deleteGrade(id: Int) {
        gpaDao.deleteGrade(id)
    }

    suspend fun resetApp() {
        gpaDao.clearAllGrades()
        gpaDao.clearUserProfile()
    }
}
