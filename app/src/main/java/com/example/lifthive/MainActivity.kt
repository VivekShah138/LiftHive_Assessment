package com.example.lifthive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.lifthive.presentation.MainViewModel
import com.example.lifthive.presentation.navigation.SetupNavGraph
import com.example.lifthive.ui.theme.LiftHiveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftHiveTheme(darkTheme = mainViewModel.isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    SetupNavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}