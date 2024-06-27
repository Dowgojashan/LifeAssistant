package com.example.life_assistant.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.life_assistant.data.EventEntity
import com.example.life_assistant.data.MemberEntity

@Database(
    entities = [MemberEntity::class,EventEntity::class],
    version = 5
)
abstract class MyDatabase :RoomDatabase(){
    abstract val dao: MyDao
    abstract val edao: EventDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 重命名EventEntity表
        db.execSQL("ALTER TABLE EventEntity RENAME TO EventEntity_old")

        // 刪除舊的EventEntity表
        db.execSQL("DROP TABLE EventEntity_old")

        // 創建新的EventEntity表
        db.execSQL("""
            CREATE TABLE EventEntity (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                Uid TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                date INTEGER NOT NULL,
                label TEXT NOT NULL,
                remind_time INTEGER NOT NULL,
                repeat INTEGER NOT NULL,
                FOREIGN KEY(memberUid) REFERENCES MemberEntity(uid) ON DELETE CASCADE
            )
        """)

        // 從舊表中插入數據到新表
        db.execSQL("""
            INSERT INTO EventEntity (id, memberUid, name, description, date, label, remind_time, repeat)
            SELECT id, Uid, name, description, date, label, remind_time, repeat
            FROM EventEntity_old
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE EventEntity RENAME TO EventEntity_old")

        db.execSQL("DROP TABLE IF EXISTS EventEntity_old")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS EventEntity (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                memberuid TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                date INTEGER NOT NULL,
                label TEXT NOT NULL,
                remind_time INTEGER NOT NULL,
                repeat INTEGER NOT NULL,
                FOREIGN KEY(memberuid) REFERENCES MemberEntity(uid) ON DELETE CASCADE
            )
        """)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE EventEntity RENAME TO EventEntity_old")

        db.execSQL("DROP TABLE IF EXISTS EventEntity_old")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS EventEntity (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                memberuid TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                date TEXT NOT NULL,
                label TEXT NOT NULL,
                remind_time INTEGER NOT NULL,
                repeat INTEGER NOT NULL,
                FOREIGN KEY(memberuid) REFERENCES MemberEntity(uid) ON DELETE CASCADE
            )
        """)
    }
}