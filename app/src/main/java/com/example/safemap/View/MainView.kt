
package com.example.safemap.View


import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.safemap.viewmodel.AuthoriseViewModel
import kotlinx.coroutines.CoroutineScope
import com.example.safemap.model.Result
import com.example.safemap.model.StreetlightRepository
import com.example.safemap.R
import com.example.safemap.model.Directions
import com.example.safemap.model.Geocoder
import com.example.safemap.viewmodel.LocationViewModel
import com.example.safemap.viewmodel.MainViewModel
import com.example.safemap.viewmodel.SettingsViewModel
import com.example.safemap.viewmodel.StreetlightViewModel
import kotlinx.coroutines.launch
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.model.DirectionsResult

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    locationViewModel: LocationViewModel,
    authoriseViewModel: AuthoriseViewModel,
    onNavigateToSignIn: () -> Unit,
    apiKey: String
)
{
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    val scope: CoroutineScope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    //Provides the current view of the screen
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var pad = 85.dp

    //Geocoding stateholders
    var destinationCoordinates by remember { mutableStateOf<LatLng?>(null) }
    var directionResult by remember { mutableStateOf<DirectionsResult?>(null) }
    var eta by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    val geocoder = remember {Geocoder(apiKey)}
    val directions = remember { Directions(apiKey) }

    var userLocation by remember { mutableStateOf(locationViewModel.location.value?.let { LatLng(
        locationViewModel.location.value!!.latitude, it.longitude) }) }

    val currentScreen = remember{
        viewModel.currentScreen.value
    }

    val settingsViewModel: SettingsViewModel = viewModel()

    val systemUIController = rememberSystemUiController()
    systemUIController.setStatusBarColor(Color.Transparent)

    LaunchedEffect(destinationCoordinates) {
        if(destinationCoordinates != null)
        {
            userLocation?.let {
                directions.getWalkingDirections(it, destinationCoordinates!!){ result ->
                    directionResult = result
                }
            }
        }
    }

    LaunchedEffect(directionResult) {
        if(directionResult != null)
        {
            eta = directions.calculateETA(directionResult)
            duration = directionResult!!.routes[0].legs[0].duration.humanReadable
            Log.d("Directions", "ETA: $eta")
            Log.d("Directions", "Duration: $duration")
        }
        }

    val bottomBar: @Composable () -> Unit = {
        if(currentScreen is Screen.DrawerScreenHandler || currentScreen is Screen.MapScreen)
        {
            BottomNavigation(
                Modifier
                    .wrapContentSize().background(MaterialTheme.colorScheme.background)
                    .height(100.dp), backgroundColor = Color(0xff26662a)) {
                screensInBottom.forEach {
                        item -> BottomNavigationItem(selected = currentRoute == item.bottomRoute, onClick = {
                    navController.navigate(item.bottomRoute)
                }, icon = { Icon(contentDescription = item.bottomTitle, painter = painterResource(id = item.icon),
                    tint = Color.White) }, label = {
                    Text(text = item.bottomTitle, color = Color.White)
                },
                    selectedContentColor = Color.LightGray,
                    unselectedContentColor = Color.White,
                    modifier = Modifier.background(Color(0xff26662a)))
                }
            }
        }
    }

    BackHandler {
        //Do Nothing to prevent the user from going back after pressing back button on the phone
    }
    Scaffold(
        bottomBar = bottomBar,
        topBar =
        {
            TopAppBar(
                modifier = Modifier.height(103.dp),
                title = {

                },
                actions = {
                    SearchBar(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.width(305.dp)
                            .padding(start = 3.dp, end = 10.dp, top = 1.dp, bottom = 5.dp),
                        query = text,
                        onQueryChange = { newText ->
                            text = newText
                        },
                        onSearch = {
                            active = false
                            geocoder.geocodeAddress(text){
                                    result ->
                                if(result != null)
                                {
                                    destinationCoordinates = LatLng(result.geometry.location.lat,
                                        result.geometry.location.lng)
                                }
                                else
                                {
                                    Log.d("Searching:", "Geocoding failed for $text")
                                    destinationCoordinates = null
                                }
                            }
                        },
                        active = active,
                        onActiveChange = { active = it
                            pad = 700.dp
                        },
                        placeholder = { Text("Search", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 1.dp)) },
                        leadingIcon = {
                            if (active) {
                                IconButton(onClick = { active = false }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        trailingIcon = {
                            if (active) {
                                IconButton(onClick = {
                                    if (text.isNotEmpty()) {
                                        text = ""
                                    } else {
                                        active = false
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = Color.White,
                            inputFieldColors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.LightGray,
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedLeadingIconColor = Color.Black,
                                focusedLeadingIconColor = Color.Black,
                                cursorColor = Color.Black,
                            )),
                    ) {
                        // Search suggestions or results can go here
                    }
                    Icon(modifier = Modifier
                        .padding(top = 5.dp, bottom = 5.dp)
                        .clickable {}, painter = painterResource(id = R.drawable.ic_wifi), contentDescription = "Contact", tint = Color.White)
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
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.padding(top = 17.dp))
                }}
            )
        }, scaffoldState = scaffoldState,
        drawerContent = {
            Box(modifier = Modifier
                .background(Color(0xff26662a))
                .fillMaxSize())
            {
                Column() {
                    Text(text = "Menu", modifier = Modifier.padding(bottom = 40.dp, start = 16.dp, top = 16.dp),
                        color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    LazyColumn(Modifier.padding(45.dp))
                    {
                        items(screensInsideOfDrawer){
                                item -> DrawerState(selected = currentRoute == item.drawerRoute, item = item) {
                            scope.launch {
                                scaffoldState.drawerState.close()
                            }
                            navController.navigate(item.route)
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
        Navigation(navController = navController, viewmodel = viewModel, pd = it, authorisationModel = authoriseViewModel, settingsViewModel = settingsViewModel,
            destinationCoordinates, directionResult)
    }
}

@Composable
fun DrawerState(selected: Boolean,
                item: Screen.DrawerScreenHandler,
                onSelected: () -> Unit)
{
    val background = if (selected) Color.White else Color.Transparent
    val text = if (selected) Color(0xff26662a) else Color.White
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp)
        .background(background)
        .clickable { onSelected() }) {
        Icon(painter = painterResource(id = item.icon), contentDescription = item.title, Modifier.padding(end = 8.dp, top = 4.dp), tint = text)
        Text(text = item.title, style = MaterialTheme.typography.titleMedium, color = text)

    }
}

@Composable
fun Navigation(navController: NavController, viewmodel: MainViewModel, pd:PaddingValues, authorisationModel: AuthoriseViewModel, settingsViewModel: SettingsViewModel,
               destinationCoordinates: LatLng?, directionResult: DirectionsResult?)
{
    NavHost(navController = navController as NavHostController,
        startDestination = Screen.MapScreen.route, modifier = Modifier.padding(pd)) {
        composable(Screen.DrawerScreenHandler.EmergencyContact.route)
        {

        }
        composable(Screen.DrawerScreenHandler.TripPlanner.route)
        {

        }
        composable(Screen.DrawerScreenHandler.FavouriteRoutes.route)
        {

        }
        composable(Screen.MapScreen.route)
        {
            MapScreen(
                settingsViewModel = settingsViewModel,
                viewmodel = LocationViewModel(),
                streetlightViewModel = StreetlightViewModel(
                    streetlightRepository = StreetlightRepository()),
                destinationCoordinates = destinationCoordinates,
                directionResult = directionResult
            )
        }
        composable(Screen.MedicalScreen.route)
        {

        }
        composable(Screen.BottomScreen.AccountScreen.bottomRoute)
        {
            AccountView(authorisationModel = authorisationModel,
                onNavigateToMedicalInfo = {
                    navController.navigate(Screen.MedicalScreen.route)
                })
        }
        composable(Screen.BottomScreen.SettingsScreen.bottomRoute)
        {
            SettingsScreen(settingsViewModel = settingsViewModel)
        }
        composable(Screen.BottomScreen.MapScreen.bottomRoute)
        {
            MapScreen(
                settingsViewModel = settingsViewModel,
                viewmodel = LocationViewModel(),
                streetlightViewModel = StreetlightViewModel(streetlightRepository = StreetlightRepository()),
                destinationCoordinates = destinationCoordinates,
                directionResult =    directionResult)
        }
    }
}