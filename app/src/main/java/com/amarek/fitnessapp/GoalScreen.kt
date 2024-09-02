package com.amarek.fitnessapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GoalScreen(navController: NavController) {

    var goalWeight by remember { mutableStateOf(0f) }
    var goalSteps by remember { mutableStateOf(0) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var dataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val docRef = db.collection("Goals").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        goalWeight = (document.getDouble("goalWeight")?.toFloat() ?: 0f)
                        goalSteps = (document.getLong("dailySteps")?.toInt() ?: 0)
                        dataLoaded = true
                    } else {
                        val initialData = hashMapOf(
                            "goalWeight" to 0.0,
                            "dailySteps" to 0L
                        )
                        docRef.set(initialData)
                            .addOnSuccessListener {
                                Log.d("GoalScreen", "New Goals document created")
                                dataLoaded = true
                            }
                            .addOnFailureListener { e ->
                                Log.e("GoalScreen", "Error creating document: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GoalScreen", "Error fetching document: $e")
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (dataLoaded) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    TextField(
                        value = goalWeight.toString(),
                        onValueChange = { newValue ->
                            goalWeight = newValue.toFloatOrNull() ?: 0f
                            Log.d("GoalScreen", "New goal weight input")
                        },
                        label = { Text("Enter your goal weight (kg): ") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    )
                    TextField(
                        value = goalSteps.toString(),
                        onValueChange = { newValue ->
                            goalSteps = newValue.toIntOrNull() ?: 10000
                            Log.d("GoalScreen", "New daily steps goal input")
                        },
                        label = { Text("Enter your daily steps goal: ") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = {
                            userId?.let { uid ->
                                val docRef = db.collection("Goals").document(uid)
                                docRef.update("goalWeight", goalWeight.toDouble())
                                    .addOnSuccessListener {
                                        Log.d("GoalScreen", "Goal weight updated successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("GoalScreen", "Error updating goal weight: $e")
                                    }
                                docRef.update("dailySteps", goalSteps.toLong())
                                    .addOnSuccessListener {
                                        Log.d("GoalScreen", "Daily steps updated successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("GoalScreen", "Error updating daily steps goal: $e")
                                    }
                            }
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "Save Goals")
                    }
                }
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(vertical = 16.dp)
            ) {
                Text("Back")
            }
        }
    }
}