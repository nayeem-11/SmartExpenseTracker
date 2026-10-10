package com.example.smartexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.smartexpensetracker.data.AppDatabase
import com.example.smartexpensetracker.data.TransactionEntity
import com.example.smartexpensetracker.ui.theme.SmartExpenseTrackerTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.smartexpensetracker.data.PendingTransactionStore
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= 33) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ),
                100
            )
        }

        setContent {
            SmartExpenseTrackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}
@Composable
fun ExpenseTrackerApp() {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var showAddTransaction by remember {
        mutableStateOf(false)
    }

    var editingTransaction by remember {
        mutableStateOf<TransactionEntity?>(null)
    }

    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val transactionDao = remember {
        database.transactionDao()
    }

    val transactions by transactionDao
        .getAllTransactions()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()


    // Manual Add Transaction screen
    if (showAddTransaction) {

        AddTransactionScreen(

            onBack = {
                showAddTransaction = false
            },

            onSave = { transaction ->

                scope.launch {

                    transactionDao.insertTransaction(transaction)

                    showAddTransaction = false
                }
            }
        )

        return
    }


    // Edit Transaction screen
    if (editingTransaction != null) {

        EditTransactionScreen(

            transaction = editingTransaction!!,

            onSave = { updatedTransaction ->

                scope.launch {

                    transactionDao.updateTransaction(
                        updatedTransaction
                    )

                    editingTransaction = null
                }
            },

            onCancel = {
                editingTransaction = null
            }
        )

        return
    }


    Scaffold(

        modifier = Modifier.fillMaxSize(),

        bottomBar = {

            NavigationBar(
                modifier = Modifier.navigationBarsPadding()
            ) {

                NavigationBarItem(

                    selected = selectedTab == 0,

                    onClick = {
                        selectedTab = 0
                    },

                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },

                    label = {
                        Text("Home")
                    }
                )


                NavigationBarItem(

                    selected = selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    icon = {
                        Text("📊")
                    },

                    label = {
                        Text("Analytics")
                    }
                )


                NavigationBarItem(

                    selected = selectedTab == 2,

                    onClick = {
                        selectedTab = 2
                    },

                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },

                    label = {
                        Text("Settings")
                    }
                )
            }
        }

    ) { innerPadding ->

        when (selectedTab) {

            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                transactions = transactions,
                onAddTransaction = { showAddTransaction = true },
                onEditTransaction = { transaction ->
                    editingTransaction = transaction
                },
                onDeleteTransaction = { transaction ->
                    scope.launch {
                        transactionDao.deleteTransaction(transaction)
                    }
                }
            )


            1 -> AnalyticsScreen(

                modifier = Modifier.padding(innerPadding),

                transactions = transactions
            )


            2 -> SettingsScreen(

                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Smart Expense Tracker",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddTransaction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Transaction")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Transactions",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (transactions.isEmpty()) {

            Text(
                text = "No transactions yet.",
                modifier = Modifier.padding(top = 20.dp)
            )

        } else {

            transactions.forEach { transaction ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${transaction.amount}"
                        )

                        Text(
                            text = "Category: ${transaction.category}"
                        )

                        Text(
                            text = "Reason: ${transaction.reason}"
                        )

                        Text(
                            text = transaction.time,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Button(
                                onClick = {
                                    onEditTransaction(transaction)
                                }
                            ) {
                                Text("✏️ Edit")
                            }

                            Button(
                                onClick = {
                                    onDeleteTransaction(transaction)
                                }
                            ) {
                                Text("🗑️ Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LazyHomeContent(
    modifier: Modifier,
    transactions: List<TransactionEntity>,
    totalIncome: Double,
    totalExpenses: Double,
    balance: Double,
    onAddTransaction: () -> Unit
) {

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "Good evening 👋",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Nayeem",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )
        }

        item {

            BalanceCard(
                balance = balance
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SummaryCard(
                    title = "Income",
                    amount = formatAmount(totalIncome),
                    icon = "↓",
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Expenses",
                    amount = formatAmount(totalExpenses),
                    icon = "↑",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {

            Text(
                text = "Spending overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            SpendingOverview(
                transactions = transactions
            )
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Recent transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${transactions.size} total",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (transactions.isEmpty()) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "No transactions yet",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "Add your first transaction below."
                        )
                    }
                }
            }

        } else {

            items(
                count = transactions.size
            ) { index ->

                TransactionItem(
                    transaction = transactions[index]
                )
            }
        }

        item {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = onAddTransaction,

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp)
            ) {

                Text(
                    text = "+ Add Transaction"
                )
            }

            Spacer(
                modifier = Modifier.height(70.dp)
            )
        }
    }
}


@Composable
fun BalanceCard(
    balance: Double
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = "Total Balance",
                color = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.8f
                )
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = formatAmount(balance),

                style = MaterialTheme.typography.headlineLarge,

                fontWeight = FontWeight.Bold,

                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Income − Expenses",

                color = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.85f
                )
            )
        }
    }
}


@Composable
fun SummaryCard(
    title: String,
    amount: String,
    icon: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,

        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = amount,

                style = MaterialTheme.typography.titleLarge,

                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun SpendingOverview(
    transactions: List<TransactionEntity>
) {

    val categories = listOf(
        "Food" to "🍔",
        "Travel" to "🚗",
        "Shopping" to "🛒",
        "Bills" to "💡"
    )

    val expenses = transactions.filter {
        !it.isIncome
    }

    val totalExpenses = expenses.sumOf {
        it.amount
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp)
    ) {

        if (expenses.isEmpty()) {

            Text(
                text = "Spending data will appear here.",

                modifier = Modifier.padding(20.dp)
            )

        } else {

            Column(
                modifier = Modifier.padding(20.dp),

                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                categories.forEach { (category, emoji) ->

                    val categoryAmount = expenses
                        .filter {
                            it.category.equals(
                                category,
                                ignoreCase = true
                            )
                        }
                        .sumOf {
                            it.amount
                        }

                    if (categoryAmount > 0) {

                        val progress = if (totalExpenses > 0) {
                            (categoryAmount / totalExpenses)
                                .toFloat()
                                .coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        SpendingRow(
                            emoji = emoji,
                            category = category,
                            amount = formatAmount(categoryAmount),
                            progress = progress
                        )
                    }
                }

                val otherAmount = expenses
                    .filter {
                        categories.none { category ->
                            it.category.equals(
                                category.first,
                                ignoreCase = true
                            )
                        }
                    }
                    .sumOf {
                        it.amount
                    }

                if (otherAmount > 0) {

                    SpendingRow(
                        emoji = "💳",
                        category = "Other",
                        amount = formatAmount(otherAmount),
                        progress = (otherAmount / totalExpenses)
                            .toFloat()
                            .coerceIn(0f, 1f)
                    )
                }
            }
        }
    }
}


@Composable
fun SpendingRow(
    emoji: String,
    category: String,
    amount: String,
    progress: Float
) {

    Column {

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = category
                )
            }

            Text(
                text = amount,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(7.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}


@Composable
fun TransactionItem(
    transaction: TransactionEntity
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(18.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(46.dp),

                shape = CircleShape,

                color = MaterialTheme.colorScheme.surfaceVariant
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = when (
                            transaction.category.lowercase()
                        ) {

                            "food" -> "🍔"
                            "travel" -> "🚗"
                            "shopping" -> "🛒"
                            "bills" -> "💡"
                            else -> "💳"
                        }
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${transaction.reason} • ${transaction.time}",

                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = if (transaction.isIncome) {
                    "+${formatAmount(transaction.amount)}"
                } else {
                    "-${formatAmount(transaction.amount)}"
                },

                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {

    var transactionType by remember { mutableStateOf("Expense") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food",
        "Travel",
        "Shopping",
        "Bills",
        "Education",
        "Health",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("←")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Transaction type",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (transactionType == "Expense") {
                Button(
                    onClick = { transactionType = "Expense" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expense")
                }
            } else {
                OutlinedButton(
                    onClick = { transactionType = "Expense" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expense")
                }
            }

            if (transactionType == "Income") {
                Button(
                    onClick = { transactionType = "Income" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Income")
                }
            } else {
                OutlinedButton(
                    onClick = { transactionType = "Income" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Income")
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Amount",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter amount") },
            placeholder = { Text("₹ 0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Category",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedButton(
                onClick = {
                    categoryExpanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (category.isBlank()) {
                        "Select category"
                    } else {
                        category
                    },
                    modifier = Modifier.weight(1f)
                )

                Text("▼")
            }

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = {
                    categoryExpanded = false
                }
            ) {

                categories.forEach { item ->

                    DropdownMenuItem(
                        text = {
                            Text(item)
                        },
                        onClick = {
                            category = item
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Reason",
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = { Text("What was this transaction for?") }
        )

        if (errorMessage.isNotEmpty()) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val amountValue = amount.toDoubleOrNull()

                when {

                    amountValue == null || amountValue <= 0 -> {
                        errorMessage = "Please enter a valid amount."
                    }

                    category.isBlank() -> {
                        errorMessage = "Please select a category."
                    }

                    reason.isBlank() -> {
                        errorMessage = "Please enter a reason."
                    }

                    else -> {

                        val transaction = TransactionEntity(
                            title = category,
                            category = category,
                            amount = amountValue,
                            reason = reason,
                            time = getCurrentTime(),
                            isIncome = transactionType == "Income"
                        )

                        onSave(transaction)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Save Transaction")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>
) {

    val totalSpent = transactions
        .filter { !it.isIncome }
        .sumOf { it.amount }

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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Total Spending",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "₹${"%.2f".format(totalSpent)}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        categories.forEach { category ->

            val categoryTotal = transactions
                .filter {
                    !it.isIncome &&
                            it.category == category
                }
                .sumOf {
                    it.amount
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Text(
                        text = category
                    )

                    Text(
                        text =
                            "₹${"%.2f".format(categoryTotal)}"
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Notification Access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Allow Smart Expense Tracker to read transaction notifications."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Button(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Enable Notification Access")
        }
    }
}


fun formatAmount(
    amount: Double
): String {

    val formatter = NumberFormat.getCurrencyInstance(
        Locale("en", "IN")
    )

    formatter.maximumFractionDigits = 0

    return formatter.format(amount)
}


fun getCurrentTime(): String {

    val formatter = java.text.SimpleDateFormat(
        "dd MMM, hh:mm a",
        Locale.getDefault()
    )

    return formatter.format(
        java.util.Date()
    )
}