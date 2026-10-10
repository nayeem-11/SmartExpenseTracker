package com.example.smartexpensetracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartexpensetracker.data.TransactionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: TransactionEntity,
    onSave: (TransactionEntity) -> Unit,
    onCancel: () -> Unit
) {
    var reason by remember {
        mutableStateOf(transaction.reason)
    }

    var category by remember {
        mutableStateOf(transaction.category)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Food",
        "Travel",
        "Shopping",
        "Bills",
        "Education",
        "Health",
        "Other",
        "Uncategorized"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Edit Transaction"
        )

        Text(
            text = "₹${transaction.amount}"
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Category")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                categories.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {
                            category = item
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = reason,
            onValueChange = {
                reason = it
            },
            label = {
                Text("Reason")
            },
            placeholder = {
                Text("What was this payment for?")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSave(
                        transaction.copy(
                            category = category,
                            reason =
                                if (reason.isBlank())
                                    "Not added"
                                else
                                    reason.trim()
                        )
                    )
                }
            ) {
                Text("Save")
            }
        }
    }
}