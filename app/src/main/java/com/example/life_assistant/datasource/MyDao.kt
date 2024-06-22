package com.example.life_assistant.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.life_assistant.data.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memberEntity: MemberEntity)

    @Delete
    suspend fun delete(memberEntity: MemberEntity)

    @Update
    suspend fun update(memberEntity: MemberEntity)

    @Query("SELECT * FROM MemberEntity")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM MemberEntity WHERE uid = :uid LIMIT 1")
    fun getMemberByUid(uid: String): MemberEntity?
}