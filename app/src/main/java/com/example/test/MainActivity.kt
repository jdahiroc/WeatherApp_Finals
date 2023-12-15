package com.example.test

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.test.constant.Const.Companion.colorBg1
import com.example.test.constant.Const.Companion.colorBg2
import com.example.test.constant.Const.Companion.permissions
import com.example.test.model.MyLatLng
import com.example.test.model.forecast.ForecastResult
import com.example.test.model.weather.WeatherResult
import com.example.test.view.WeatherSection
import com.example.test.ui.theme.TestTheme
import com.example.test.view.ForecastSection
import com.example.test.viewmodel.MainViewModel
import com.example.test.viewmodel.STATE
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mainViewModel: MainViewModel
    private var locationRequired: Boolean = false

    override fun onResume() {
        super.onResume()
        if(locationRequired) startLocationUpdate();
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        locationCallback?.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateAgeMillis(100)
                .build()

                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.getMainLooper()
                )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLocationClient()
        initViewModel()


        setContent {

            //this will keep your value of our current location
            var currentLocation by remember {
                mutableStateOf(MyLatLng(0.0, 0.0))
            }

            //Implement location callback
            locationCallback = object: LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for (location in p0.locations){
                        currentLocation = MyLatLng(
                            location.latitude,
                            location.longitude

                        )
                    }

                }
            }

            TestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationScreen(this@MainActivity ,currentLocation)
                }
            }
        }
    }

    private fun fetchWeatherInformation(mainViewModel: MainViewModel, currentLocation: MyLatLng) {
        mainViewModel.state = STATE.LOADING
        mainViewModel.getWeatherByLocation(currentLocation)
        //mainViewModel.getForecastByLocation(currentLocation)
        mainViewModel.state = STATE.SUCCESS
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
    }

    @Composable
    private fun LocationScreen(context: Context, currentLocation: MyLatLng) {
        //Request runtime permission
        val launcherMultiplePermissions = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ permissionMap ->
            val areGranted = permissionMap.values.reduce {
                accepted, next -> accepted && next
            }
            // Check all permission is accept
            if(areGranted) {
                locationRequired = true;
                startLocationUpdate();
                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }

        val systemUiController = rememberSystemUiController()

        DisposableEffect(key1 = true, effect = {
            systemUiController.isSystemBarsVisible = false // hide status bar
            onDispose {
                systemUiController.isSystemBarsVisible = true // show statis bar
            }
        } )

        LaunchedEffect(key1 = currentLocation, block = {
            coroutineScope {
                if(permissions.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                    }) {
                    //if all Permission accepted
                    startLocationUpdate()
                }
                else {
                    launcherMultiplePermissions.launch(permissions)
                }
            }

        })

        LaunchedEffect(key1 =true, block = {
            fetchWeatherInformation(mainViewModel, currentLocation)
        })

        val gradient = Brush.linearGradient(
            colors = listOf(Color(colorBg1), Color(colorBg2)),
            start = Offset(1000f, -1000f),
            end = Offset(1000f, 1000f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            val marginTop = screenHeight * 0.1f // I want margin top by 20% height
            val marginTopPx = with(LocalDensity.current) { marginTop.toPx()}

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)

                        //Define layout for the child
                        layout(
                            placeable.width,
                            placeable.height + marginTopPx.toInt()
                        ) {
                            placeable.placeRelative(0, marginTopPx.toInt())
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                when (mainViewModel.state) {
                    STATE.LOADING -> {
                        LoadingSection()
                    }
                    STATE.FAILED -> {
                        ErrorSection(mainViewModel.errorMessage)
                    }
                    else -> {
                        WeatherSection(mainViewModel.weatherResponse)
                        ForecastSection(mainViewModel.forecastResponse)
                    }
                }
            }
            FloatingActionButton(
                onClick = {
                    //Fetch API when location change
                    fetchWeatherInformation(mainViewModel, currentLocation)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Aligns the FAB to the bottom center
                    .offset(y = (-16).dp) // Adjusts the FAB position upwards

            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Add")
            }
        }

    }


    @Composable
    fun ErrorSection(errorMessage: String) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = errorMessage, color = Color.White)
        }
    }

    @Composable
    fun LoadingSection() {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    private fun initLocationClient() {
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
    }
}

