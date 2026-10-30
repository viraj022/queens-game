package com.viraj022.queens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.viraj022.queens.ui.QueensGameRoute
import com.viraj022.queens.ui.QueensViewModel
import com.viraj022.queens.ui.theme.CreamBackground
import com.viraj022.queens.ui.theme.QueensTheme

class MainActivity : ComponentActivity() {

    private val viewModel: QueensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QueensTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = CreamBackground
                ) {
                    QueensGameRoute(viewModel = viewModel)
                }
            }
        }
    }
}
