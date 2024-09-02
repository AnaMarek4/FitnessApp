package com.amarek.fitnessapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest

@Composable
fun WorkoutScreen(navController: NavController) {
    var workouts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("workout").get().await()
            workouts = snapshot.documents.map { doc ->
                doc.data ?: emptyMap()
            }
        } catch (e: Exception) {
            Log.e("WorkoutScreen", "Error fetching workouts: $e")
        }
    }

    if (workouts.isNotEmpty()) {
        val workout = workouts[currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkoutItem(workout)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentIndex > 0) {
                    Button(
                        onClick = { currentIndex-- },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(text = "Previous")
                    }
                }
                Button(
                    onClick = {
                        if (currentIndex < workouts.size - 1) {
                            currentIndex++
                        } else {
                            navController.navigate("main_screen") {
                                popUpTo("workout_screen") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(text = if (currentIndex == workouts.size - 1) "Finish" else "Next")
                }
            }
        }
    } else {
        Text("No workouts available")
    }
}

@Composable
fun WorkoutItem(workout: Map<String, Any>) {
    val name = workout["name"] as? String ?: ""
    val reps = (workout["reps"] as? Number)?.toInt() ?: 0
    val url = workout["url"] as? String ?: ""

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
    ) {
        val imageModifier = Modifier
            .size(500.dp)
            .padding(bottom = 16.dp)
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .decoderFactory(GifDecoder.Factory())
                    .build()
            ),
            contentDescription = name,
            modifier = imageModifier
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 20.sp
        )
        Text(
            text = "Reps: $reps",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp
        )
    }
}