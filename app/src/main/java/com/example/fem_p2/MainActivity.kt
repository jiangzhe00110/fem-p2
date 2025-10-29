package com.example.fem_p2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fem_p2.ui.navigation.TravelPlannerNavHost

import com.example.fem_p2.ui.theme.Femp2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Femp2Theme {
                TravelPlannerNavHost()

            }
        }
    }
}

