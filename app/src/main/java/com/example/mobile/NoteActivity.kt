package com.example.mobile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.common_ui.DataStoreVM
import com.example.glance.WidgetReceiver
import com.example.graph.CONS.AUDIOS
import com.example.graph.CONS.IMAGES
import com.example.graph.Graph
import com.example.graph.checkShortcut
import com.example.graph.intentHandler
import com.example.graph.urlPreview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NoteActivity : ComponentActivity() {

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window,false)

        setContent {

            val navHostController = rememberNavController()
            val scope = rememberCoroutineScope()

            intentHandler(intent, this@NoteActivity, navHostController, scope)

            AppTheme {
                Graph(navHostController)
            }
        }
    }

    @Composable
    private fun AppTheme(
        dataStoreVM: DataStoreVM = hiltViewModel(),
        content: @Composable () -> Unit
    ) {
        val currentTheme = remember(dataStoreVM, dataStoreVM::getTheme).collectAsState()
        val systemUiController = rememberSystemUiController()
        val isDarkUi = isSystemInDarkTheme()

        val theme = when {
            isSystemInDarkTheme() -> darkColorScheme()
            currentTheme.value == "DARK" -> darkColorScheme()
            else -> lightColorScheme()
        }

        SideEffect {
            systemUiController.apply {
                setStatusBarColor(Color.Transparent, !isDarkUi)
                setNavigationBarColor(
                    if (currentTheme.value == "DARK" || isDarkUi) Color(red = 28, green = 27, blue = 31)
                    else Color(red = 255, green = 251, blue = 254)
                )
            }
        }

        MaterialTheme(colorScheme = theme, content = content)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        urlPreview(this, null, null, null, null, null, null)?.cleanUp()
    }

    override fun onStart() {
        super.onStart()
        // TODO: move it to init module as work manager.
        File(this.filesDir.path + "/" + IMAGES).mkdirs()
        File(this.filesDir.path + "/" + AUDIOS).mkdirs()
        File(this.filesDir.path + "/" + "links_img").mkdirs()

//        mapOf(
//            "Coffee" to "Prepare hot coffee for my self.",
//            "Certification" to "Call instructor for complete details.",
//            "Team Meeting" to "Planning sprint log for next product application update.",
//            "Birthday Party" to "Tomorrow is my brother birthday there will be party at 7:00 pm.",
//            "Vacation Tickets" to  "Buy tickets for the family vacation.",
//            "Appointment" to "Health check up with physician."
//
//        ).forEach {
//            vm.value.addNote(
//                Note(
//                    uid = UUID.randomUUID().toString(),
//                    title = it.key,
//                    description = it.value
//                )
//            )
//        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        checkShortcut(this)
    }

    override fun onPause() {
        super.onPause()
        WidgetReceiver.updateBroadcast(this)
    }


}

