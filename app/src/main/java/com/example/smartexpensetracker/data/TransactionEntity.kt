package com.example.smartexpensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val category: String,

    val amount: Double,

    val reason: String,

    val time: String,

    val isIncome: Boolean
)