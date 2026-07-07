package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GpaDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("SELECT * FROM grade_records ORDER BY semester ASC, moduleCode ASC")
    fun getAllGrades(): Flow<List<GradeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(gradeRecord: GradeRecord)

    @Query("DELETE FROM grade_records WHERE id = :id")
    suspend fun deleteGrade(id: Int)

    @Query("DELETE FROM grade_records")
    suspend fun clearAllGrades()

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()
}
