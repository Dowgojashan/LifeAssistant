package com.example.life_assistant.Repository

import com.example.life_assistant.data.MemberEntity
import com.example.life_assistant.datasource.MyDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface MemberRepository {

    suspend fun insert(memberEntity: MemberEntity)

    suspend fun delete(memberEntity: MemberEntity)

    suspend fun update(memberEntity: MemberEntity)

    suspend fun getAllMembers(): Flow<List<MemberEntity>>
    suspend fun getMemberByUid(uid: String): MemberEntity

    suspend fun getUid(uid: String): String?
}

class RepositoryImpl @Inject constructor(
    private val dao: MyDao,
) : MemberRepository{
    override suspend fun insert(memberEntity: MemberEntity) {
        withContext(IO) {
            dao.insert(memberEntity)
        }
    }

    override suspend fun delete(memberEntity: MemberEntity) {
        withContext(Dispatchers.IO) {
            dao.delete(memberEntity)
        }
    }

    override suspend fun update(memberEntity: MemberEntity) {
        withContext(Dispatchers.IO) {
            dao.update(memberEntity)
        }
    }

    override suspend fun getAllMembers(): Flow<List<MemberEntity>> {
        return withContext(Dispatchers.IO) {
            dao.getAllMembers()
        }
    }

    override suspend fun getMemberByUid(uid: String): MemberEntity {
        return dao.getMemberByUid(uid)
    }

    override suspend fun getUid(uid: String): String? {
        return dao.getUid(uid)
    }
}