package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GpaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GpaViewModel = viewModel()
            val userProfileState by viewModel.userProfile.collectAsState()
            val systemDarkTheme = isSystemInDarkTheme()
            
            val isDarkTheme = remember(userProfileState, systemDarkTheme) {
                when (userProfileState?.themeMode) {
                    "light" -> false
                    "dark" -> true
                    else -> systemDarkTheme
                }
            }

            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        val currentProfile = userProfileState
                        if (currentProfile != null) {
                            val nextMode = when (currentProfile.themeMode) {
                                "light" -> "dark"
                                "dark" -> "auto"
                                else -> "light"
                            }
                            viewModel.updateProfile(
                                degreeName = currentProfile.degreeName,
                                degreeType = currentProfile.degreeType,
                                indexNumber = currentProfile.indexNumber,
                                faculty = currentProfile.faculty,
                                fullName = currentProfile.fullName,
                                themeMode = nextMode
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
