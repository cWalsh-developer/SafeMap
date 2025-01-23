package com.example.safemap.View

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safemap.viewmodel.AuthoriseViewModel
import kotlinx.coroutines.CoroutineScope
import com.example.safemap.data.Result
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    authoriseViewModel: AuthoriseViewModel,
    onNavigateToSignIn: () -> Unit,
)
{
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope: CoroutineScope = rememberCoroutineScope()

    //Provides the current view of the screen
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val title = remember{ mutableStateOf("") }

    BackHandler {
        //Do Nothing
    }
    Scaffold(
        topBar =
        {
            TopAppBar(
                title = {

                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xff26662a)),
                navigationIcon = { IconButton(onClick =
                {
                    //Open the drawer
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }

                })
                {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }}
            )
        }, scaffoldState = scaffoldState,
        drawerContent = {
            Box(modifier = Modifier.background(Color(0xff26662a)).fillMaxSize())
            {
                Column {
                    Text(text = "Menu", modifier = Modifier.padding(bottom = 40.dp, start = 16.dp, top = 16.dp),
                        color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    LazyColumn(Modifier.padding(45.dp))
                    {
                        items(screensInsideOfDrawer){
                            item -> DrawerState(selected = currentRoute == item.route, item = item) {
                                scope.launch {
                                    scaffoldState.drawerState.close()
                                }
        //                    navController.navigate(item.route)
        //                    title.value = item.title
                        }
                        }
                    }
                    Button(onClick = {
                        authoriseViewModel.signOut(Result.LoggedOut)
                        onNavigateToSignIn()
                    }, modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp), colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xff26662a),
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.DarkGray
                    )) {
                        Text(text = "Sign Out", color = Color(0xff26662a))
                    }

                }

            }
        }
    )
    {
        Text(text = "Map Screen", modifier = Modifier.padding(it))
    }
}

@Composable
fun DrawerState(selected: Boolean,
                item: DrawerScreenHandler,
                onSelected: () -> Unit)
{
    val background = if (selected) Color.Red else Color.Transparent
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp).background(background)
        .clickable {onSelected()}) {
        Icon(painter = painterResource(id = item.icon), contentDescription = item.title, Modifier.padding(end = 8.dp, top = 4.dp), tint = Color.White)
        Text(text = item.title, style = MaterialTheme.typography.titleMedium, color = Color.White)

    }
}