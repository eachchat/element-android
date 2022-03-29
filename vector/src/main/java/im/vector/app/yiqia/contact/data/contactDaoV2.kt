package im.vector.app.yiqia.contact.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by chengww on 2020/11/3
 * @author chengww
 */
@Dao
interface ContactDaoV2 {
    @Query("SELECT * FROM contactsV2 WHERE del!=1")
    fun getContactsLiveData(): LiveData<List<ContactsDisplayBeanV2?>?>? //在这里可以直接返回LiveData<>封装的查询结果


    @Query("SELECT * FROM contactsV2 WHERE del!=1")
    fun getContacts(): List<ContactsDisplayBeanV2>

    @Query("SELECT * FROM contactsV2 WHERE del!=1 LIMIT :limit OFFSET :offset")
    fun getContactsLimitOffset(offset: Int, limit: Int): List<ContactsDisplayBeanV2>

    @Query("SELECT count(*) FROM contactsV2 WHERE del!=1")
    fun getContactsCount(): Int

    @Query("SELECT * FROM contactsV2 WHERE del != 1 AND matrixId != '' AND matrixId IS NOT NULL ")
    fun getContactsWithNonNullMatrixId(): List<ContactsDisplayBeanV2>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContact(vararg contact: ContactsDisplayBeanV2?)

    @Update
    fun update(contact: ContactsDisplayBeanV2): Int

    @Delete
    fun delete(contact: ContactsDisplayBeanV2)

    @Query("DELETE FROM contactsV2")
    fun deleteContacts()

    @Query("SELECT * FROM contactsV2 WHERE matrixId = :matrixId AND del==1")
    fun getDelContactByMatrixId(matrixId: String): ContactsDisplayBeanV2?

    @Query("SELECT * FROM contactsV2 WHERE id = :id AND del!=1")
    fun getContactByContactId(id: String?): ContactsDisplayBeanV2?

    @Query("SELECT * FROM contactsV2 WHERE id = :id AND del!=1")
    fun getContactByContactIdLive(id: String?): LiveData<ContactsDisplayBeanV2?>

    @Query("SELECT * FROM contactsV2 WHERE matrixId = :matrixId AND del!=1")
    fun getContactByMatrixId(matrixId: String): ContactsDisplayBeanV2?

    @Query("SELECT * FROM contactsV2 WHERE matrixId = :matrixId AND del!=1")
    fun getContactByMatrixIdLive(matrixId: String): LiveData<ContactsDisplayBeanV2?>

    @Query("SELECT * FROM contactsV2 WHERE matrixId IN (:matrixIds) AND del!=1")
    fun getContactsByMatrixIds(matrixIds: List<String>): List<ContactsDisplayBeanV2>

    @Query("SELECT * FROM contactsV2 WHERE ( matrixId LIKE '%'||:keyword||'%' OR nickName LIKE '%'||:keyword||'%' OR family || given LIKE '%'||:keyword||'%' OR given || ' ' || family LIKE '%'||:keyword||'%') AND del != 1")
    fun searchContacts(keyword: String): List<ContactsDisplayBeanV2>

    @Query("UPDATE contactsV2 SET lastSeenTs = :lastSeenTs WHERE matrixId = :matrixId AND del != 1")
    fun updateLastSeen(matrixId: String, lastSeenTs: Long): Int

    @Query("SELECT * FROM contactsV2 WHERE del != 1 ORDER BY CASE WHEN :isAsc = 1 THEN lastSeenTs END ASC, CASE WHEN :isAsc = 0 THEN lastSeenTs END DESC")
    fun getRecentContacts(isAsc: Boolean = false): List<ContactsDisplayBeanV2>
}
