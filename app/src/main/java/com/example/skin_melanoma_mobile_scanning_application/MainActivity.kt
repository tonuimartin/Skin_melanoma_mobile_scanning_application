package com.example.skin_melanoma_mobile_scanning_application

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.skin_melanoma_mobile_scanning_application.ui.theme.Skin_melanoma_mobile_scanning_applicationTheme
import kotlinx.coroutines.delay
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Skin_melanoma_mobile_scanning_applicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(onSplashComplete = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("registration") {
            RegistrationScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("scan") {
            ScanScreen(navController)
        }
        composable(
            route = "scanResult/{imageUri}/{isMalignant}/{confidence}",
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("isMalignant") { type = NavType.BoolType },
                navArgument("confidence") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val imageUri = Uri.parse(backStackEntry.arguments?.getString("imageUri"))
            val isMalignant = backStackEntry.arguments?.getBoolean("isMalignant") ?: false
            val confidence = backStackEntry.arguments?.getFloat("confidence") ?: 0f

            ScanResultScreen(
                navController = navController,
                imageUri = imageUri,
                classification = Pair(isMalignant, confidence)
            )
        }
        composable("history") {
            ScanHistoryScreen(navController)
        }

        composable("image-preview") {
            HomeScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        visible = false
        onSplashComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Android Logo",
                modifier = Modifier.size(200.dp)
            )
        }
    }
}