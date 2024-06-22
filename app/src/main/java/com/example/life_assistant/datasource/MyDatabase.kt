package com.example.life_assistant.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.life_assistant.data.MemberEntity

@Database(
    entities = [MemberEntity::class],
    version = 2
)
abstract class MyDatabase :RoomDatabase(){
    abstract val dao: MyDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "MyDataBase"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE MemberEntity ADD COLUMN wake_time TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE MemberEntity ADD COLUMN sleep_time TEXT NOT NULL DEFAULT ''")
    }
}