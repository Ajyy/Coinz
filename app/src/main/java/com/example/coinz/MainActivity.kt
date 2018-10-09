package com.example.coinz

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import org.json.JSONObject
import android.os.AsyncTask
import android.os.PersistableBundle
import android.support.design.widget.NavigationView
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset


class MainActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    private val tag = "MainActivity"
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var permissionManager: PermissionsManager
    private lateinit var originLocation: Location

    private var locationEngine: LocationEngine? = null//component gives us the user location
    private var locationLayerPlugin: LocationLayerPlugin? = null//showing icon representing the users current locatuon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync (this)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this@MainActivity)
    }

    override fun onMapReady(mapboxMap: MapboxMap?) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null")
        } else {
            map = mapboxMap
            // Set user interface options
            map?.uiSettings?.isCompassEnabled = true
            map?.uiSettings?.isZoomControlsEnabled = true
            // Make location information available
            enableLocation()
            DrawGeoJson().execute()
        }
    }


    private fun enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            Log.d(tag, "Permissions are granted")
            initializeLocationEngine()
            initializeLocationLayer()
        }else{
            Log.d(tag, "Permissions are not granted")
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine(){
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.interval = 5000
        locationEngine?.fastestInterval = 1000
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        }else{
            locationEngine?.addLocationEngineListener(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationLayer(){
        if (mapView == null) { Log.d(tag, "mapView is null") }
        else {
            if (map == null) {
                Log.d(tag, "map is null")
            } else {
                locationLayerPlugin = LocationLayerPlugin(mapView!!, map!!, locationEngine)
                locationLayerPlugin?.setLocationLayerEnabled(true)
                locationLayerPlugin?.cameraMode = CameraMode.TRACKING
                locationLayerPlugin?.renderMode = RenderMode.NORMAL
            }
        }
    }

    private fun setCameraPosition(location: Location){
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 17.0))
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Log.d(tag, "Permissions: $permissionsToExplain")
        // Present popup message or dialog
        Toast.makeText(this, "After that, you can begin this game.", Toast.LENGTH_SHORT)
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted){
            enableLocation()
        } else {
            // Open a dialogue with the user
            Toast.makeText(this, "Please allow to get your location", Toast.LENGTH_SHORT)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onLocationChanged(location: Location?) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null")
        } else {
            originLocation = location
            setCameraPosition(originLocation)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine?.requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }

        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (outState != null) {
            mapView?.onSaveInstanceState(outState)
        }
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_balance) {
            // Handle the camera action
        } else if (id == R.id.nav_friend) {

        } else if (id == R.id.nav_share) {

        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private inner class DrawGeoJson : AsyncTask<Void, Void, List<Point>>() {
        override fun doInBackground(vararg voids: Void): List<Point> {

            val points = ArrayList<Point>()

            try {
                // Load GeoJSON file
//                val inputStream = assets.open("geojson")
//                val rd = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
//                val sb = StringBuilder()
//                var cp: Int = rd.read()
//                while (cp != -1) {
//                    sb.append(cp.toChar())
//                    cp = rd.read()
//                }
//                var url = URL("http://homepages.inf.ed.ac.uk/stg/coinz/2018/01/01/coinzmap.geojson");
////                var request = url.openConnection();
////                request.connect();
//////                inputStream.close()
////                var jp = JsonParser();
                val `is` = URL("http://homepages.inf.ed.ac.uk/stg/coinz/2018/01/01/coinzmap.geojson").openStream()
                val rd = BufferedReader(InputStreamReader(`is`, Charset.forName("UTF-8")))
                val jsonText = StringBuilder()
                var cp = rd.read()
                while (cp!= -1) {
                    jsonText.append(cp.toChar())
                    cp = rd.read()
                }

                // Parse JSON
                val json = JSONObject(jsonText.toString())
                val features = json.getJSONArray("features")

                for (i in 0..features.length()){
                    val feature = features.getJSONObject(i)
                    val properties = feature.getJSONObject("properties")
                    val geometry = feature.getJSONObject("geometry")
                    if (geometry != null && properties != null) {
                        val type = geometry.getString("type")

                        if(type != null && type.toString() == "Point"){
                            val coord = geometry.getJSONArray("coordinates")
                            val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                            points.add(Point(properties.getString("id"), properties.getDouble("value")
                                    , properties.getString("currency"), properties.getString("marker-symbol")
                                    , properties.getString("marker-color"), latLng))
                        }
                    }
                }

            } catch (exception: Exception) {
                Log.e(tag, "Exception Loading GeoJSON: " + exception.toString())
            }

            return points
        }

        override fun onPostExecute(points: List<Point>) {
            super.onPostExecute(points)
            var iconFactory = IconFactory.getInstance(this@MainActivity)
//            var iconDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_marker, null)
            //var icon = iconFactory.fromResource(R.drawable.ic_action_name)
            if (points.isNotEmpty()) {
                for(point in points){
                    //var mDrawable = getDrawable(R.drawable.ic_action_name);
                    //mDrawable.colorFilter = PorterDuffColorFilter(Color.parseColor(point.markerColor), PorterDuff.Mode.MULTIPLY)
                    DrawableCompat.setTint(getDrawable(R.drawable.ic_action_name),
                            Color.GREEN)
                    var icon = iconFactory.fromResource(R.drawable.ic_action_name)
                    map?.addMarker(MarkerOptions()
                            .position(point.latlng)
                            .icon(icon)
                            .title(point.currency)
                            .snippet("id: "+point.id))
                }
            }
        }
    }

}
