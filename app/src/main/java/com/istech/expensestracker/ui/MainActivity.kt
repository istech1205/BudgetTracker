package com.istech.expensestracker.ui

import android.os.Bundle
import android.view.View
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.istech.expensestracker.R
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat

/**
 * MainActivity serves as the entry point for the app's UI.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Explicitly set status bar color to primaryDark
        setStatusBarColor()
    }
    fun setStatusBarColor( ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryDark))

                // Adjust padding to avoid overlap
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
        } else {
            // For Android 14 and below
            window.statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
        }
    }
} 