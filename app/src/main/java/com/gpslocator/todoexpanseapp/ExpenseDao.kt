package com.gpslocator.todoexpanseapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE date(timestamp / 1000, 'unixepoch', 'localtime') = date('now', 'localtime') ORDER BY id DESC")
    suspend fun getTodayExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE strftime('%W', timestamp / 1000, 'unixepoch', 'localtime') = strftime('%W', 'now', 'localtime') AND strftime('%Y', timestamp / 1000, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') ORDER BY id DESC")
    suspend fun getThisWeekExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE strftime('%m', timestamp / 1000, 'unixepoch', 'localtime') = strftime('%m', 'now', 'localtime') AND strftime('%Y', timestamp / 1000, 'unixepoch', 'localtime') = strftime('%Y', 'now', 'localtime') ORDER BY id DESC")
    suspend fun getThisMonthExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE isSynced = 0 AND userId IS NOT NULL")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>

    @Query("UPDATE expenses SET isSynced = 1, firestoreId = :firestoreId WHERE id = :localId")
    suspend fun markSynced(localId: Int, firestoreId: String)
}
