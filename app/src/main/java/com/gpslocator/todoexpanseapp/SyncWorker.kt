package com.gpslocator.todoexpanseapp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val db = AppDatabase.getDatabase(applicationContext)
        val firestore = FirebaseFirestore.getInstance()
+-
        return try {
            val unsyncedExpenses = db.expenseDao().getUnsyncedExpenses()

            if (unsyncedExpenses.isEmpty()) {
                Log.d("SyncWorker", "No new expenses to sync.")
                return Result.success()
            }

            Log.d("SyncWorker", "Starting sync for ${unsyncedExpenses.size} items...")

            for (expense in unsyncedExpenses) {
                val data = hashMapOf(
                    "title" to expense.title,
                    "amount" to expense.amount,
                    "category" to expense.category,
                    "timestamp" to expense.timestamp,
                    "userId" to user.uid,
                    "localId" to expense.id // helps tracking back
                )

                // Upload to Firestore
                val docRef = firestore.collection("users")
                    .document(user.uid)
                    .collection("expenses")
                    .add(data)
                    .await()

                // Update local Room database
                db.expenseDao().markSynced(expense.id, docRef.id)
                Log.d("SyncWorker", "Synced: ${expense.title}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}")
            Result.retry() // WorkManager will try again later
        }
    }
}
