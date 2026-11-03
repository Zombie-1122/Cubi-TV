package com.cubitv.app

import android.app.UiModeManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cubitv.app.data.LibraryStore
import com.cubitv.app.ui.DetailScreen
import com.cubitv.app.ui.HomeScreen
import com.cubitv.app.ui.PlayerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LibraryStore.init(applicationContext)
        val isTv = (getSystemService(UI_MODE_SERVICE) as UiModeManager).currentModeType ==
            Configuration.UI_MODE_TYPE_TELEVISION
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) { AppNav(isTv) }
        }
    }
}

@Composable
fun AppNav(isTv: Boolean) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(isTv) { a ->
                nav.navigate("detail/${Uri.encode(a.source)}/${Uri.encode(a.id)}")
            }
        }
        composable("detail/{source}/{id}") { e ->
            val s = e.arguments!!.getString("source")!!
            val id = e.arguments!!.getString("id")!!
            DetailScreen(s, id, isTv) { ep ->
                nav.navigate("player/${Uri.encode(s)}/${Uri.encode(id)}/${Uri.encode(ep)}")
            }
        }
        composable("player/{source}/{id}/{ep}") { e ->
            PlayerScreen(
                e.arguments!!.getString("source")!!,
                e.arguments!!.getString("id")!!,
                e.arguments!!.getString("ep")!!
            )
        }
    }
}
