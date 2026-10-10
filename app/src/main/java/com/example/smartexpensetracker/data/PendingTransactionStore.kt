package com.example.smartexpensetracker.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PendingTransactionStore {

    private val _pendingTransaction =
        MutableStateFlow<PendingTransaction?>(null)

    val pendingTransaction =
        _pendingTransaction.asStateFlow()

    fun setTransaction(transaction: PendingTransaction) {
        _pendingTransaction.value = transaction
    }

    fun clearTransaction() {
        _pendingTransaction.value = null
    }
}