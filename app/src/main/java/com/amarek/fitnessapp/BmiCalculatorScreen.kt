package com.amarek.fitnessapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BmiCalculatorScreen(navController: NavController) {
    var weightInput by remember { mutableStateOf(0f) }
    var heightInput by remember { mutableStateOf(0f) }
    var bmi by remember { mutableStateOf(0f) }
    var bmiCategory by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val docRef = db.collection("BMI").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        weightInput = (document.getDouble("weight")?.toFloat() ?: 0f)
                        heightInput = (document.getDouble("height")?.toFloat() ?: 0f)
                        bmi = calculateBmi(weightInput, heightInput)
                        bmiCategory = getBmiCategory(bmi)
                    } else {
                        val initialData = hashMapOf(
                            "weight" to 0.0,
                            "height" to 0.0
                        )
                        docRef.set(initialData)
                            .addOnSuccessListener {
                                Log.d("BmiScreen", "New BMI document created")
                            }
                            .addOnFailureListener { e ->
                                Log.e("BmiScreen", "Error creating document: $e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BmiScreen", "Error fetching document: $e")
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
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TextField(
                    value = weightInput.toString(),
                    onValueChange = { newValue ->
                        weightInput = newValue.toFloatOrNull() ?: 0f
                        Log.d("BmiScreen", "New weight input")
                    },
                    label = { Text("Enter your weight (kg): ") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )
                TextField(
                    value = heightInput.toString(),
                    onValueChange = { newValue ->
                        heightInput = newValue.toFloatOrNull() ?: 0f
                        Log.d("BmiScreen", "New height input")
                    },
                    label = { Text("Enter your height (cm): ") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                )
                Button(
                    onClick = {
                        userId?.let { uid ->
                            val docRef = db.collection("BMI").document(uid)
                            docRef.update("weight", weightInput.toDouble())
                                .addOnSuccessListener {
                                    Log.d("BmiScreen", "Weight updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("BmiScreen", "Error updating weight: $e")
                                }
                            docRef.update("height", heightInput.toDouble())
                                .addOnSuccessListener {
                                    Log.d("BmiScreen", "Height updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("BmiScreen", "Error updating height: $e")
                                }

                            bmi = calculateBmi(weightInput, heightInput)
                            bmiCategory = getBmiCategory(bmi)
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Calculate BMI")
                }
                Text(
                    text = "Your BMI is: ",
                    fontSize = 40.sp,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 32.dp)
                )
                Text(
                    text = String.format("%.2f", bmi),
                    fontSize = 50.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = bmiCategory,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(16.dp)
            ) {
                Text("Back")
            }
        }
    }
}

fun calculateBmi(weight: Float, height: Float): Float {
    val heightInMeters = height / 100.0f
    return if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0f
}

fun getBmiCategory(bmi: Float): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi in 18.5..24.9 -> "Normal weight"
        bmi in 25.0..29.9 -> "Overweight"
        bmi >= 30 -> "Obesity"
        else -> ""
    }
}
