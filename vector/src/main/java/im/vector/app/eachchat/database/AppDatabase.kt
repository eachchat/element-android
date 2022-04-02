package im.vector.app.eachchat.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import im.vector.app.eachchat.contact.api.bean.Department
import im.vector.app.eachchat.contact.data.ContactsDisplayBean
import im.vector.app.eachchat.contact.data.ContactsDisplayBeanV2
import im.vector.app.eachchat.contact.data.ContactsMatrixUser
import im.vector.app.eachchat.contact.data.ContactsRoom
import im.vector.app.eachchat.contact.data.RoomInviteDisplay
import im.vector.app.eachchat.contact.data.UpdateTime
import im.vector.app.eachchat.contact.data.User
import im.vector.app.eachchat.contact.database.ContactDaoV2
import im.vector.app.eachchat.contact.database.ContactMatrixUserDao
import im.vector.app.eachchat.contact.database.ContactRoomDao
import im.vector.app.eachchat.contact.database.DepartmentDao
import im.vector.app.eachchat.contact.database.RoomInviteDao
import im.vector.app.eachchat.contact.database.UpdateTimeDao
import im.vector.app.eachchat.contact.database.UserDao

/**
 * Created by chengww on 2020/11/3
 * @author chengww
 */
@Database(entities = [
    User::class,
    ContactsDisplayBeanV2::class,
    ContactsDisplayBean::class,
    ContactsRoom::class,
    UpdateTime::class,
    ContactsMatrixUser::class,
    RoomInviteDisplay::class,
    Department::class
], version = 1)
abstract class AppDatabase : RoomDatabase() {
    //    abstract fun contactDao(): ContactDao
    abstract fun contactDaoV2(): ContactDaoV2

    abstract fun UserDao(): UserDao

    abstract fun contactRoomDao(): ContactRoomDao

    abstract fun updateTimeDao(): UpdateTimeDao

    abstract fun contactMatrixUserDao(): ContactMatrixUserDao

    abstract fun roomInviteDao(): RoomInviteDao

    abstract fun departmentDao(): DepartmentDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context) = INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "each_chat_db")
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { INSTANCE = it }

        @VisibleForTesting
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("alter table contacts ADD COLUMN lastSeenTs INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
