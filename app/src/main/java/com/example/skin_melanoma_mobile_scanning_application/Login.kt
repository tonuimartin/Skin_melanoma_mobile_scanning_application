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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isResettingPassword by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = {
                auth.signOut()
                showVerificationDialog = false
                isLoading = false
            },
            title = { Text("Email Not Verified") },
            text = {
                Text("Please verify your email address before logging in. Would you like us to send another verification email?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                currentUser?.sendEmailVerification()?.await()
                                Toast.makeText(
                                    context,
                                    "Verification email sent",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Failed to send verification email",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            auth.signOut()
                            showVerificationDialog = false
                            isLoading = false
                        }
                    }
                ) {
                    Text("Resend Email")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        showVerificationDialog = false
                        isLoading = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
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
                .padding(bottom = 8.dp)
        )

        TextButton(
            onClick = { showForgotPasswordDialog = true },
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
        ) {
            Text("Forgot Password?")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        val cleanEmail = email.trim().lowercase()

                        auth.signInWithEmailAndPassword(cleanEmail, password)
                            .addOnCompleteListener { task ->
                                scope.launch(Dispatchers.Main) {
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        if (user?.isEmailVerified == true) {
                                            try {
                                                val userDoc = db.collection("users")
                                                    .document(user.uid)
                                                    .get()
                                                    .await()

                                                if (userDoc.getBoolean("tempAccount") == true) {
                                                    db.collection("users")
                                                        .document(user.uid)
                                                        .update(
                                                            mapOf(
                                                                "tempAccount" to false,
                                                                "isEmailVerified" to true
                                                            )
                                                        )
                                                        .await()
                                                }

                                                Toast.makeText(
                                                    context,
                                                    "Login successful!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Error updating user status: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                isLoading = false
                                            }
                                        } else {
                                            currentUser = user
                                            showVerificationDialog = true
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Login failed: ${task.exception?.message}",
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
                Text("Login")
            }
        }

        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?")
            TextButton(
                onClick = {
                    navController.navigate("registration") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            ) {
                Text("Register")
            }
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                resetEmail = ""
            },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your email address and we'll send you a password reset link.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it.trim() },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            isResettingPassword = true
                            auth.sendPasswordResetEmail(resetEmail)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            context,
                                            "Password reset email sent",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to send reset email: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    isResettingPassword = false
                                    showForgotPasswordDialog = false
                                    resetEmail = ""
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter your email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = !isResettingPassword
                ) {
                    if (isResettingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        resetEmail = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}