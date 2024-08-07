package com.example.life_assistant.Main

import android.app.Application
import androidx.room.Room
import com.example.life_assistant.Repository.EventRepository
import com.example.life_assistant.Repository.EventRepositoryImpl
import com.example.life_assistant.Repository.MemberRepository
import com.example.life_assistant.Repository.RepositoryImpl
import com.example.life_assistant.datasource.MIGRATION_1_2
import com.example.life_assistant.datasource.MIGRATION_2_3
import com.example.life_assistant.datasource.MIGRATION_3_4
import com.example.life_assistant.datasource.MIGRATION_4_5
import com.example.life_assistant.datasource.MyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
    @Module
    object AppModule {


        @Provides
        @Singleton
        fun provideMyDataBase(app: Application): MyDatabase {
            return Room.databaseBuilder(
                app,
                MyDatabase::class.java,
                "MyDataBase"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
        }



    @Provides
    @Singleton
    fun provideMyRepository(mydb: MyDatabase) : MemberRepository {
        return RepositoryImpl(mydb.dao)
    }

    @Provides
    @Singleton
    fun provideEventRepository(mydb: MyDatabase) : EventRepository {
        return EventRepositoryImpl(mydb.edao)
    }
}