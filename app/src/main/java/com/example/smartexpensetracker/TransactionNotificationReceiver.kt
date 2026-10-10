package com.example.smartexpensetracker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.smartexpensetracker.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val action = intent.action ?: return

        val transactionId =
            intent.getIntExtra(EXTRA_TRANSACTION_ID, -1)

        if (transactionId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {

            val database = AppDatabase.getDatabase(context)
            val dao = database.transactionDao()

            // Get the transaction that was already saved
            val transaction =
                dao.getTransactionById(transactionId)

            if (transaction == null) {
                pendingResult.finish()
                return@launch
            }

            // Ignore → delete this transaction
            if (action == ACTION_IGNORE) {

                dao.deleteTransactionById(transactionId)

                cancelNotification(
                    context,
                    transactionId
                )

                pendingResult.finish()
                return@launch
            }

            // Category selected
            val category = when (action) {

                ACTION_FOOD -> "Food"

                ACTION_TRAVEL -> "Travel"

                ACTION_SHOPPING -> "Shopping"

                ACTION_BILLS -> "Bills"

                ACTION_EDUCATION -> "Education"

                ACTION_HEALTH -> "Health"

                ACTION_OTHER -> "Other"

                else -> null
            }

            // Reason entered using notification text box
            val reason =
                RemoteInput
                    .getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_REASON)
                    ?.toString()
                    ?.trim()

            val updatedCategory =
                category ?: transaction.category

            val updatedReason =
                if (!reason.isNullOrBlank()) {
                    reason
                } else {
                    transaction.reason
                }

            // Update SAME transaction
            dao.updateTransaction(
                transaction.copy(
                    category = updatedCategory,
                    reason = updatedReason
                )
            )

            cancelNotification(
                context,
                transactionId
            )

            pendingResult.finish()
        }
    }

    private fun cancelNotification(
        context: Context,
        notificationId: Int
    ) {
        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.cancel(notificationId)
    }

    companion object {

        const val ACTION_FOOD =
            "com.example.smartexpensetracker.FOOD"

        const val ACTION_TRAVEL =
            "com.example.smartexpensetracker.TRAVEL"

        const val ACTION_SHOPPING =
            "com.example.smartexpensetracker.SHOPPING"

        const val ACTION_BILLS =
            "com.example.smartexpensetracker.BILLS"

        const val ACTION_EDUCATION =
            "com.example.smartexpensetracker.EDUCATION"

        const val ACTION_HEALTH =
            "com.example.smartexpensetracker.HEALTH"

        const val ACTION_OTHER =
            "com.example.smartexpensetracker.OTHER"

        const val ACTION_IGNORE =
            "com.example.smartexpensetracker.IGNORE"

        const val ACTION_SAVE_REASON =
            "com.example.smartexpensetracker.SAVE_REASON"

        const val KEY_REASON =
            "transaction_reason"

        const val EXTRA_AMOUNT =
            "transaction_amount"

        const val EXTRA_TRANSACTION_ID =
            "transaction_id"

        const val TRANSACTION_NOTIFICATION_ID =
            1001
    }
}