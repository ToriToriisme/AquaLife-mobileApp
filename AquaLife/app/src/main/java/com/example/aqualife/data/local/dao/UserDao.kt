package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE user_table SET displayName = :name WHERE uid = :uid")
    suspend fun updateDisplayName(uid: String, name: String)

    @Query("UPDATE user_table SET bio = :bio WHERE uid = :uid")
    suspend fun updateBio(uid: String, bio: String)

    @Query("UPDATE user_table SET avatarUrl = :avatarUrl WHERE uid = :uid")
    suspend fun updateAvatar(uid: String, avatarUrl: String)

    @Query("UPDATE user_table SET isEmailVerified = :verified WHERE uid = :uid")
    suspend fun updateEmailVerification(uid: String, verified: Boolean)

    @Query("DELETE FROM user_table WHERE uid = :uid")
    suspend fun deleteUser(uid: String)
}

