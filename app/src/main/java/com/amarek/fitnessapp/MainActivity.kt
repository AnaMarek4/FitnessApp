package com.amarek.fitnessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amarek.fitnessapp.ui.theme.FitnessAppTheme
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FitnessAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val isAuthenticated = currentUser != null

                    if (isAuthenticated) {
                        NavHost(navController = navController, startDestination = "main_screen") {
                            composable("main_screen") {
                                MainScreen(navController = navController)
                            }
                            composable("workout_screen") {
                                WorkoutScreen(navController = navController)
                            }
                            composable("step_counter") {
                                StepCounterScreen(navController = navController)
                            }
                            composable("bmi_calculator") {
                                BmiCalculatorScreen(navController = navController)
                            }
                            composable("progress_screen") {
                                ProgressScreen(navController = navController)
                            }
                            composable("goals_screen") {
                                GoalScreen(navController = navController)
                            }
                        }
                    } else {
                        AuthenticationScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current

    BackgroundImage(modifier = Modifier.fillMaxSize())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Power Fitness",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            val buttonWidth = 200.dp

            Button(
                onClick = { navController.navigate("workout_screen") },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Start workout")
            }

            Button(
                onClick = { navController.navigate("step_counter") },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Step counter")
            }

            Button(
                onClick = { navController.navigate("bmi_calculator") },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "BMI calculator")
            }
            Button(
                onClick = { navController.navigate("progress_screen") },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Progress")
            }
            Button(
                onClick = { navController.navigate("goals_screen") },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Goals")
            }
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    (context as? ComponentActivity)?.recreate()
                },
                modifier = Modifier
                    .width(buttonWidth)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Log Out")
            }
        }
    }
}
