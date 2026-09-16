package com.example.credittrackph.data.db.dao

import androidx.room.*
import com.example.credittrackph.data.db.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY isMainUser DESC, name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY isMainUser DESC, name ASC")
    suspend fun getAllProfilesSync(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE isMainUser = 1 LIMIT 1")
    fun getMainUser(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE isMainUser = 1 LIMIT 1")
    suspend fun getMainUserSync(): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileByIdSync(profileId: Int): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)
}
