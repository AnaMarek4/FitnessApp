package com.amarek.fitnessapp

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthenticationScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Power Fitness",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isRegistering) {
                    val (isValid, message) = validateRegistration(email, password)
                    if (isValid) {
                        register(context, email, password)
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                } else {
                    signIn(context, email, password)
                }
            }) {
                Text(if (isRegistering) "Register" else "Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(
                    text = buildAnnotatedString {
                        if (isRegistering) {
                            append("Already have an account? ")
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append("Login")
                            }
                        } else {
                            append("Don't have an account? ")
                            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append("Register")
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun validateRegistration(email: String, password: String): Pair<Boolean, String> {
    return when {
        email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            Pair(false, "Invalid email format.")
        }
        password.length < 8 -> {
            Pair(false, "Password must have at least 8 characters.")
        }
        else -> {
            Pair(true, "")
        }
    }
}

private fun signIn(context: Context, email: String, password: String) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                (context as? ComponentActivity)?.recreate()
            } else {
                Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
}

private fun register(context: Context, email: String, password: String) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
                (context as? ComponentActivity)?.recreate()
            } else {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
}
