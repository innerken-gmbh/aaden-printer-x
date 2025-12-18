package com.innerken.aadenprinterx

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Context.MEDIA_ROUTER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaRouter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager.InvalidDisplayException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.innerken.aadenprinterx.RouteName.Index
import com.innerken.aadenprinterx.RouteName.Setting
import com.innerken.aadenprinterx.modules.GlobalSettingManager
import com.innerken.aadenprinterx.modules.printer.PrinterManager
import com.innerken.aadenprinterx.PrinterXService
import com.innerken.aadenprinterx.page.IndexPage
import com.innerken.aadenprinterx.page.SettingPage
import com.innerken.aadenprinterx.ui.theme.AadenprinterxTheme
import com.innerken.aadenprinterx.viewmodel.PrinterViewModel
import com.jakewharton.processphoenix.ProcessPhoenix
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.util.Locale
import javax.inject.Inject
import kotlin.getValue

object RouteName {
    const val Index = "index"
    const val Setting = "setting"
}



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var globalSettingManager: GlobalSettingManager

    lateinit var activityScopeNavController: NavController

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        val printerManager = PrinterManager(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val printerViewModel: PrinterViewModel by viewModels()
        printerViewModel.initPrinterManager(printerManager)

        setContent {
            val focusRequester = remember { FocusRequester() }
            val context = LocalContext.current
            val activity = context as? Activity

            LaunchedEffect(key1 = true) {
                printerViewModel.setBonImage()
            }

            val navController = rememberNavController()
            this.activityScopeNavController = navController

            NavHost(modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(),
                navController = navController,
                startDestination = Index) {

                composable(Index) {
                    IndexPage(printerViewModel,
                        onHomeClick = {
                            activity?.moveTaskToBack(true)
                        },
                        onDoubleClick = {
                            restartApplication()
                        }) {
                        navController.navigate(Setting)
                    }
                }
                composable(Setting) {
                    SettingPage(printerViewModel, back = {navController.popBackStack()}) {
                        restartApplication()
                    }
                }
            }
        }
    }



    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }



    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {

            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }

    }

    private fun restartApplication() {
        ProcessPhoenix.triggerRebirth(this)
    }

}