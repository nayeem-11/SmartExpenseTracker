package com.example.smartexpensetracker.data

data class PendingTransaction(
    val amount: Double,
    val title: String,
    val isIncome: Boolean
)