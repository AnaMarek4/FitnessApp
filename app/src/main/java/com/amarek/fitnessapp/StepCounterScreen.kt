package com.amarek.fitnessapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

@Composable
fun StepCounterScreen(navController: NavController) {

    val context = LocalContext.current

    val prefsName = "StepCounterPrefs"
    val keySteps = "steps"

    fun saveSteps(context: Context, steps: Int) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putInt(keySteps, steps).apply()
    }

    fun loadSteps(context: Context): Int {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return prefs.getInt(keySteps, 0)
    }

    var steps by rememberSaveable { mutableStateOf(loadSteps(context)) }
    var dailyStepGoal by remember { mutableStateOf(10000) }

    val sensorManager = (LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
    val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var previousMagnitude by remember { mutableStateOf(0f) }
    var previousStepTimestamp by remember { mutableStateOf(0L) }
    var gravity = FloatArray(3) { 0f }
    val alpha = 0.8f

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val docRef = db.collection("Goals").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val stepsGoal = document.getLong("dailySteps")?.toInt() ?: 0
                        dailyStepGoal = if (stepsGoal > 0) stepsGoal else 10000
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("StepCounterScreen", "Error fetching document: $e")
                }
        }
    }

    val sensorEventListener = rememberUpdatedState(object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                val x = event.values[0] - gravity[0]
                val y = event.values[1] - gravity[1]
                val z = event.values[2] - gravity[2]

                val magnitude = sqrt(x * x + y * y + z * z)

                val threshold = 10f
                if (magnitude > threshold && (event.timestamp - previousStepTimestamp) > 250_000_000L) {
                    steps += 1
                    previousStepTimestamp = event.timestamp
                }
                previousMagnitude = magnitude
                saveSteps(context, steps)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    })

    DisposableEffect(sensorManager) {
        val listener = sensorEventListener.value
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val progress = steps.toFloat() / dailyStepGoal

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Step Count: $steps",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(25.dp)
                    .padding(vertical = 8.dp)
            )
            Text(
                text = "Goal: $steps / $dailyStepGoal steps",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.4f)
                .padding(bottom = 30.dp)
        ) {
            Text("Back")
        }
    }
}