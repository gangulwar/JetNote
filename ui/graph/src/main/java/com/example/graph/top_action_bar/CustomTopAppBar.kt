package com.example.graph.top_action_bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.common_ui.AdaptingRow
import com.example.common_ui.Cons.KEY_CLICK
import com.example.common_ui.DataStoreVM
import com.example.common_ui.Icons.MENU_BURGER_ICON
import com.example.graph.sound
import com.example.local.model.Label
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    drawerState: DrawerState,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    title: String
) {
    TopAppBar(
        navigationIcon = {
            Row {
                AdaptingRow(
                    Modifier.padding(start = 10.dp, end = 10.dp),
                ) {
                    Open_Drawer(drawerState = drawerState)
                }
            }
        },
        title = { Text(title, fontSize = 22.sp, modifier = Modifier.padding(start = 15.dp)) },
        scrollBehavior = topAppBarScrollBehavior
    )
}