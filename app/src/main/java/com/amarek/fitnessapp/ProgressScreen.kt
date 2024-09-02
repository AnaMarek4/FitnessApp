package com.amarek.fitnessapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import com.amarek.fitnessapp.ui.theme.DownArrowColor
import com.amarek.fitnessapp.ui.theme.UpArrowColor

@Composable
fun ProgressScreen(navController: NavController) {

    var newWeightInput by remember { mutableStateOf("") }
    var weightEntries by remember { mutableStateOf(listOf<Pair<Float, String>>()) }
    var goalWeight by remember { mutableStateOf(0f) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(userId) {
        userId?.let { uid ->
            db.collection("Goals").document(uid).get()
                .addOnSuccessListener { document ->
                    goalWeight = document?.getDouble("goalWeight")?.toFloat() ?: 0f
                }
                .addOnFailureListener { e ->
                    Log.e("ProgressScreen", "Error fetching goal weight: $e")
                }

            db.collection("progress").document(uid).collection("weight")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val entries = mutableListOf<Pair<Float, String>>()
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    for (document in documents) {
                        val weight = document.getDouble("weight")?.toFloat() ?: 0f
                        val date = document.getTimestamp("date")?.toDate()
                        val formattedDate = date?.let { dateFormat.format(it) } ?: ""
                        entries.add(Pair(weight, formattedDate))
                    }
                    weightEntries = entries
                }
                .addOnFailureListener { e ->
                    Log.e("ProgressScreen", "Error fetching weight entries: $e")
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Goal: $goalWeight kg",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(top = 58.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                weightEntries.forEach { entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.first > goalWeight) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = if (entry.first > goalWeight) "Above Goal" else "Below Goal",
                            tint = if (entry.first > goalWeight) UpArrowColor else DownArrowColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${entry.second} - ${entry.first} kg",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            TextField(
                value = newWeightInput,
                onValueChange = { newValue -> newWeightInput = newValue },
                label = { Text("Enter new weight (kg)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            Button(
                onClick = {
                    val weight = newWeightInput.toFloatOrNull() ?: return@Button
                    val date = com.google.firebase.Timestamp.now()

                    userId?.let { uid ->
                        val weightData = hashMapOf(
                            "weight" to weight,
                            "date" to date
                        )
                        db.collection("progress").document(uid).collection("weight")
                            .add(weightData)
                            .addOnSuccessListener {
                                Log.d("ProgressScreen", "New weight entry added")
                                newWeightInput = "" // Clear the input field
                                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date.toDate())
                                weightEntries = weightEntries + Pair(weight, formattedDate)
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProgressScreen", "Error adding new weight entry: $e")
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(vertical = 2.dp)
            ) {
                Text(text = "Add Weight Entry")
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
            ) {
                Text("Back")
            }
        }
    }
}