package com.gpslocator.todoexpanseapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val timestamp: Long,
    val amount: Double,
    val category: String,
    val userId: String? = null, // null for guest, uid for logged-in users
    val isSynced: Boolean = false, // track if synced with Firestore
    val firestoreId: String? = null // store Firestore document ID
)