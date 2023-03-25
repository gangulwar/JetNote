package com.example.mobile.top_action_bar

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.common_ui.Cons.KEY_CLICK
import com.example.common_ui.Icons.DASHBOARD_ICON
import com.example.common_ui.Icons.LIST_VIEW_ICON_1
import com.example.datastore.DataStore
import com.example.graph.sound
import com.example.mobile.DataStoreVM
import kotlinx.coroutines.launch

@Composable
internal fun Layout(
    dataStore: DataStore?,
    dataStoreVM: DataStoreVM = hiltViewModel()
) {
//    val collectAsState = dataStore?.getLayout?.collectAsState(true)?.value
    val currentLayout  = dataStoreVM.getLayout.collectAsState().value
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val thereIsSoundEffect = DataStore(ctx).thereIsSoundEffect.collectAsState(false)

    Icon(
        painter = if (currentLayout == "LIST") painterResource(id = DASHBOARD_ICON) else painterResource(id = LIST_VIEW_ICON_1),
        contentDescription = null,
        modifier = Modifier.clickable {
            sound.makeSound.invoke(ctx, KEY_CLICK, thereIsSoundEffect.value)
//            scope.launch {
//                dataStore?.saveLayout(!collectAsState!!)
//            }
            dataStoreVM.setLayout(
                if (currentLayout == "GRID") "LIST" else "GRID"
            )
        }
    )
}


