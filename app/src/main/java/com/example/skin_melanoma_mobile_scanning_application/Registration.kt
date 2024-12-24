package com.example.skin_melanoma_mobile_scanning_application

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() &&
                        firstName.isNotBlank() && lastName.isNotBlank()) {
                        isLoading = true
                        val cleanEmail = email.trim().lowercase()

                        auth.createUserWithEmailAndPassword(cleanEmail, password)
                            .addOnCompleteListener { task ->
                                scope.launch(Dispatchers.Main) {
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            val user = hashMapOf(
                                                "firstName" to firstName.trim(),
                                                "lastName" to lastName.trim(),
                                                "email" to cleanEmail
                                            )

                                            db.collection("users")
                                                .document(userId)
                                                .set(user)
                                                .addOnSuccessListener {
                                                    scope.launch(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Registration successful!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        // Delay navigation slightly to ensure toast is visible
                                                        kotlinx.coroutines.delay(1000)
                                                        navController.navigate("login") {
                                                            popUpTo("registration") { inclusive = true }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error storing user data: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    isLoading = false
                                                }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Registration failed: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        isLoading = false
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all fields",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Register")
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?")
            TextButton(
                onClick = {
                    navController.navigate("login") {
                        popUpTo("registration") { inclusive = true }
                    }
                }
            ) {
                Text("Login")
            }
        }
    }
}