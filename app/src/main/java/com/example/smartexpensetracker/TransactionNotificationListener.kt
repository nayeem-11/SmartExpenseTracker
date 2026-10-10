package com.example.smartexpensetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.smartexpensetracker.data.AppDatabase
import com.example.smartexpensetracker.data.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.app.RemoteInput

class TransactionNotificationListener : NotificationListenerService() {

    private val allowedPackage =
        "com.google.android.apps.messaging"

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        // Only process Google Messages
        if (sbn.packageName != allowedPackage) return

        val extras = sbn.notification.extras

        val title = extras.getString("android.title") ?: ""
        val text =
            extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText =
            extras.getCharSequence("android.bigText")?.toString() ?: ""

        val notificationText = "$title $text $bigText"

        // Ignore OTP messages
        if (
            notificationText.contains("OTP", ignoreCase = true) ||
            notificationText.contains(
                "one time password",
                ignoreCase = true
            ) ||
            notificationText.contains(
                "verification code",
                ignoreCase = true
            )
        ) {
            return
        }

        // Only process debit transactions
        if (!notificationText.contains("debited", ignoreCase = true)) {
            return
        }

        // Extract amount
        val regex = Regex(
            """(?:debited\s*(?:by|with)?\s*(?:rs\.?|₹)?\s*([\d,]+(?:\.\d{1,2})?)|(?:rs\.?|₹)\s*([\d,]+(?:\.\d{1,2})?)\s*debited)""",
            RegexOption.IGNORE_CASE
        )

        val match = regex.find(notificationText) ?: return

        val amountText =
            (match.groupValues[1]
                .ifBlank { match.groupValues[2] })
                .replace(",", "")

        val amount = amountText.toDoubleOrNull() ?: return

        // Save transaction immediately
        CoroutineScope(Dispatchers.IO).launch {

            val database = AppDatabase.getDatabase(this@TransactionNotificationListener)

            val transaction = TransactionEntity(
                title = "UPI Transaction",
                category = "Uncategorized",
                amount = amount,
                reason = "Not added",
                time = "Just now",
                isIncome = false
            )

            // IMPORTANT:
            // Room returns the ID of this exact transaction
            val transactionId =
                database.transactionDao()
                    .insertTransaction(transaction)
                    .toInt()

            // Show notification using this transaction ID
            showTransactionNotification(
                amount = amount,
                transactionId = transactionId
            )
        }
    }
    private fun showTransactionNotification(
        amount: Double,
        transactionId: Int
    ) {

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val channelId = "transaction_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description =
                "Transaction detection alerts"

            notificationManager.createNotificationChannel(channel)
        }

        // Category actions
        val foodAction = createAction(
            transactionId,
            TransactionNotificationReceiver.ACTION_FOOD,
            "🍔 Food"
        )

        val travelAction = createAction(
            transactionId,
            TransactionNotificationReceiver.ACTION_TRAVEL,
            "🚗 Travel"
        )

        val shoppingAction = createAction(
            transactionId,
            TransactionNotificationReceiver.ACTION_SHOPPING,
            "🛍 Shopping"
        )

        val billsAction = createAction(
            transactionId,
            TransactionNotificationReceiver.ACTION_BILLS,
            "💡 Bills"
        )

        val ignoreAction = createAction(
            transactionId,
            TransactionNotificationReceiver.ACTION_IGNORE,
            "Ignore"
        )


        // Remote input for reason
        val remoteInput =
            RemoteInput.Builder(
                TransactionNotificationReceiver.KEY_REASON
            )
                .setLabel("What was this payment for?")
                .build()


        // Intent for saving reason
        val reasonIntent =
            Intent(
                this,
                TransactionNotificationReceiver::class.java
            ).apply {

                action =
                    TransactionNotificationReceiver.ACTION_SAVE_REASON

                putExtra(
                    TransactionNotificationReceiver.EXTRA_TRANSACTION_ID,
                    transactionId
                )
            }


        val reasonPendingIntent =
            PendingIntent.getBroadcast(
                this,
                transactionId + 50000,
                reasonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_MUTABLE
            )


        val reasonAction =
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_edit,
                "✏️ Enter reason",
                reasonPendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()


        // Open app when notification itself is tapped
        val openAppIntent =
            Intent(this, MainActivity::class.java)

        val openAppPendingIntent =
            PendingIntent.getActivity(
                this,
                transactionId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )


        val notification =
            NotificationCompat.Builder(
                this,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )

                .setContentTitle(
                    "Transaction Detected"
                )

                .setContentText(
                    "₹$amount was debited • Reason not added"
                )

                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "₹$amount was debited.\n" +
                                    "Category: Uncategorized\n" +
                                    "Reason: Not added"
                        )
                )

                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                .setAutoCancel(true)

                // Tap notification → open app
                .setContentIntent(
                    openAppPendingIntent
                )

                // Actions
                .addAction(foodAction)
                .addAction(travelAction)
                .addAction(shoppingAction)
                .addAction(billsAction)
                .addAction(reasonAction)
                .addAction(ignoreAction)

                .build()


        notificationManager.notify(
            transactionId,
            notification
        )
    }



    private fun createAction(
        transactionId: Int,
        action: String,
        label: String
    ): NotificationCompat.Action {

        val intent =
            Intent(
                this,
                TransactionNotificationReceiver::class.java
            ).apply {

                this.action = action

                // Send the exact transaction ID
                putExtra(
                    TransactionNotificationReceiver.EXTRA_TRANSACTION_ID,
                    transactionId
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                transactionId + action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat.Action.Builder(
            0,
            label,
            pendingIntent
        ).build()
    }
}