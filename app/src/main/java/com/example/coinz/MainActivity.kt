package com.example.coinz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.squareup.picasso.Picasso
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var permissionManager: PermissionsManager
    private lateinit var originLocation: Location
    private var locationEngine: LocationEngine? = null// Component gives us the user location
    private var locationLayerPlugin: LocationLayerPlugin? = null// Showing icon representing the users current locatuon

    private lateinit var navigationView: NavigationView
    private var tvNavUserName: TextView? = null
    private var tvNavEmail: TextView? = null
    private var ivProfilePicture: ImageView? = null
    private var fabLocation: FloatingActionButton? = null
    private var fabCollect: FloatingActionButton? = null

    private var mStorageReference: StorageReference? = null

    private val tag = "MainActivity"
    private val profile = 2
    private val pickName = 3

    private var coins = ArrayList<Coin>()
    private var coinsId = ArrayList<String>()
    private var markers = ArrayList<MarkerOptions>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mStorageReference = FirebaseStorage.getInstance().reference

        mapView = findViewById(R.id.mapView)
        fabLocation = findViewById(R.id.fabLocation)
        fabCollect = findViewById(R.id.fabCollect)
        navigationView = findViewById(R.id.nav_view)

        val headView = navigationView.getHeaderView(0)
        tvNavEmail = headView.findViewById(R.id.tvNavEmail)
        tvNavUserName = headView.findViewById(R.id.tvNavUserName)
        ivProfilePicture = headView.findViewById(R.id.ivProfilePicture)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView

        initializeUser(User.userAuth!!)
        navigationView.setNavigationItemSelectedListener(this@MainActivity)

        // Get coins player has gotten
        getCoinsId()

        // Set mapbox
        Mapbox.getInstance(applicationContext, getString(R.string.access_token))
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync (this)

        // Set navigation view, drawer
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_coins_inf, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onInfShowPopupWindowClick(findViewById(item!!.itemId))

        return super.onOptionsItemSelected(item)
    }

    // Set popup window to check the coins information in the map
    @SuppressLint("InflateParams", "SetTextI18n")
    fun onInfShowPopupWindowClick(view: View){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_map_coins_inf, null)
        val tvOverall = popupView.findViewById<View>(R.id.tvOverall) as TextView
        val tvShilMap = popupView.findViewById<View>(R.id.tvShilMap) as TextView
        val tvDolrMap = popupView.findViewById<View>(R.id.tvDolrMap) as TextView
        val tvQuidMap = popupView.findViewById<View>(R.id.tvQuidMap) as TextView
        val tvPenyMap = popupView.findViewById<View>(R.id.tvPenyMap) as TextView

        // Get the number of differet kinds of coins
        val num = intArrayOf(0, 0, 0, 0)
        for (coin in coins){
            if (coin.id !in coinsId){
                when {
                    coin.type == "SHIL" -> num[0]++
                    coin.type == "DOLR" -> num[1]++
                    coin.type == "QUID" -> num[2]++
                    coin.type == "PENY" -> num[3]++
                }
            }
        }

        tvOverall.text = "Overall Number: ${num.sum()}"
        tvShilMap.text = "Number of SHIL: ${num[0]}"
        tvDolrMap.text = "Number of DOLR: ${num[1]}"
        tvQuidMap.text = "Number of QUID: ${num[2]}"
        tvPenyMap.text = "Number of PENY: ${num[3]}"

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        val popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        popupView.setOnTouchListener { _, _ ->
            popupWindow.dismiss()
            return@setOnTouchListener true
        }
    }

    // Get all coins Id from Firebase and start to get today's coins
    private fun getCoinsId(){
        User.userDb.document(User.userAuth!!.uid).get()
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        @Suppress("UNCHECKED_CAST")
                        coinsId = task.result!!["coinsId"] as ArrayList<String>
                        DrawGeoJson().execute()
                        Log.d(tag, "get user data: Success")
                    } else {
                        Log.w(tag, "get user data: fail")
                    }
                }
    }

    // Initialize user's information
    private fun initializeUser(user: FirebaseUser){
        tvNavEmail?.text = user.email
        if (user.displayName != ""&&user.displayName != null){
            tvNavUserName!!.text = user.displayName
        } else {
            tvNavUserName!!.text = "SetYourName"
            Log.d(tag, "username not found")
        }

        // Get player's avatar
        val pathReference = mStorageReference!!.child("images/"+user.email+".jpg")
        pathReference.downloadUrl
                .addOnSuccessListener { filePath ->
                    Picasso.get().load(filePath).into(ivProfilePicture)
                    Log.d(tag, "down avatar: success")
                }
                .addOnFailureListener { exception ->
                    Log.w(tag, "down avatar: failure\n"+exception.message)
                }

        // Set the listener to change user's avatar
        ivProfilePicture!!.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "select picture"), pickName)
        }
    }

    // Upload user's avatar
    private fun uploadFile(bitmap: Bitmap, filePath: Uri){
        val riversRef = mStorageReference!!.child("images/"+User.userAuth!!.email+".jpg")
        riversRef.putFile(filePath)
                .addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "File Uploaded", Toast.LENGTH_LONG).show()
                    ivProfilePicture!!.setImageBitmap(bitmap)
                    Log.d(tag, "File uploaded")
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MainActivity, "Fail to upload", Toast.LENGTH_LONG).show()
                    Log.w(tag, "Fail to Upload"+exception.message)
                }
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
        }
    }

    // Add collected coins to Firebase
    private fun addCoins(coin: Coin){
        // Add to the coins collection
        User.userDb.document(User.userAuth!!.uid).collection("balance_"+coin.type).document(coin.id!!)
                .set(coin, SetOptions.merge())
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        Log.d(tag, "update balance: Success")
                    } else {
                        Log.w(tag, "update balance: fail")
                    }
                }

        // Add coins' id to the attribute coinsId
        User.userDb.document(User.userAuth!!.uid).update("coinsId", FieldValue.arrayUnion(coin.id))
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        fabCollect!!.isEnabled = true
                        val value = String.format("%.4f", coin.value)
                        Toast.makeText(this@MainActivity, "Get a ${coin.type} coin with a value $value!!", Toast.LENGTH_SHORT).show()
                        Log.d(tag, "update balance: Success")
                    } else {
                        Log.w(tag, "update balance: fail")
                    }
                }

        // Add coins value to the total balance
        User.userDb.document(User.userAuth!!.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val user = User()
                        user.totalBal = task.result!!["totalBal"] as Double
                        user.addBalance(coin.value!!, coin.type!!)
                        Log.d(tag, "get total balance: Success")
                    } else {
                        Log.w(tag, "get total balance: fail")
                    }
                }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            Log.d(tag, "Permissions are granted")
            initializeLocationEngine()
            initializeLocationLayer()

            // Set flout action button listener
            fabLocation!!.setOnClickListener{
                val lastLocation = locationEngine?.lastLocation
                if (lastLocation != null) {
                    setCameraPosition(lastLocation)
                }
            }

            fabCollect!!.setOnClickListener {
                val lastLocation = locationEngine?.lastLocation
                if (lastLocation != null) {
                    val addCoinsArr = ArrayList<Coin>()
                    for (coin in coins){
                        val distance = LatLng(coin.latitude, coin.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude))
                        // Once the distance is less than 25 meter, collect it
                        if (distance <= 25){
                            if (fabCollect!!.isEnabled){
                                fabCollect!!.isEnabled = false
                            }

                            addCoinsArr.add(coin)
                        }
                    }

                    // add coins and remove markers
                    for (coin in addCoinsArr){
                        addCoins(coin)
                        removeMarkers(coin)
                        coinsId.remove(coin.id)
                    }
                }
            }
        }else{
            Log.d(tag, "Permissions are not granted")
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine(){
        locationEngine = LocationEngineProvider(this@MainActivity).obtainBestLocationEngineAvailable()
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
                locationLayerPlugin?.cameraMode = CameraMode.TRACKING_GPS
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
        Toast.makeText(this, "After that, you can begin this game.", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        Log.d(tag, "[onPermissionResult] granted == $granted")
        if (granted){
            enableLocation()
        } else {
            // Open a dialogue with the user
            Toast.makeText(this, "Please allow to get your location", Toast.LENGTH_SHORT).show()
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

            Log.d(tag, "[onLocationChanged] location is "+location.latitude+" "+location.longitude)
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        Log.d(tag, "[onConnected] requesting location updates")
        locationEngine?.requestLocationUpdates()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        when (id) {
            R.id.nav_profile -> {
                User.reloadUser()
                startActivityForResult(Intent(this@MainActivity, ProfileActivity::class.java), profile)
            }
            R.id.nav_central_park -> {
                startActivity(Intent(this@MainActivity, CentralBankActivity::class.java))
            }
            R.id.nav_friend -> {
                User.reloadUser()
                startActivity(Intent(this@MainActivity, FriendActivity::class.java))
            }
            R.id.nav_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Come to Coinz and play with your friends!")
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            R.id.nav_log_out -> {
                // Log out and update userAuth
                User.mAuth.signOut()
                User.userAuth = User.mAuth.currentUser
                finish()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            R.id.nav_balance -> {
                startActivity(Intent(this@MainActivity, BalanceActivity::class.java))
            }
            R.id.nav_achievement -> {
                startActivity(Intent(this@MainActivity, AchievementActivity::class.java))
            }
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    // Remove marker by find the same one in the markers list
    private fun removeMarkers(coin: Coin){
        for (markerOption in markers){
            if (coin.latitude == markerOption.position.latitude&& coin.longitude == markerOption.position.longitude){
                map!!.removeMarker(markerOption.marker)
                break
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class DrawGeoJson : AsyncTask<Void, Void, List<Coin>>() {
        override fun doInBackground(vararg voids: Void): List<Coin> {

            val points = ArrayList<Coin>()

            val cal = Calendar.getInstance()
            val today = String.format("%d/%02d/%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

            try {
                val geoJsonData = URL("http://homepages.inf.ed.ac.uk/stg/coinz/$today/coinzmap.geojson").openStream()
                val rd = BufferedReader(InputStreamReader(geoJsonData, Charset.forName("UTF-8")))
                val jsonText = StringBuilder()
                var cp = rd.read()
                while (cp != -1) {
                    jsonText.append(cp.toChar())
                    cp = rd.read()
                }

                // Parse JSON
                val json = JSONObject(jsonText.toString())
                val rates = json.getJSONObject("rates")
                Coin.ratesArr["SHIL"] = rates.getDouble("SHIL")
                Coin.ratesArr["DOLR"] = rates.getDouble("DOLR")
                Coin.ratesArr["QUID"] = rates.getDouble("QUID")
                Coin.ratesArr["PENY"] = rates.getDouble("PENY")

                val features = json.getJSONArray("features")
                for (i in 0 until features.length()){
                    val feature = features.getJSONObject(i)
                    val properties = feature.getJSONObject("properties")
                    val geometry = feature.getJSONObject("geometry")
                    if (geometry != null && properties != null) {
                        val type = geometry.getString("type")

                        if(type != null && type.toString() == "Point"){
                            val coord = geometry.getJSONArray("coordinates")
                            val latLng = LatLng(coord.getDouble(1), coord.getDouble(0))
                            val point = Coin(properties.getString("id"), String.format("%.4f", properties.getDouble("value")).toDouble()
                                    , properties.getString("currency"), properties.getString("marker-symbol")
                                    , properties.getString("marker-color"), latLng.latitude, latLng.longitude, false)

                            // Only the coins which have not been collected are added to the map
                            if (point.id !in coinsId){
                                coins.add(point)
                                points.add(point)
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e(tag, "Exception Loading GeoJSON: "+ exception.stackTrace +"\n"+ exception.toString())
            }

            return points
        }

        override fun onPostExecute(coins: List<Coin>) {
            super.onPostExecute(coins)
            val iconFactory = IconFactory.getInstance(this@MainActivity)

            if (coins.isNotEmpty()) {
                for (coin in coins) {
                    val icon: Icon = when {
                        coin.markerColor == "#0000ff" -> iconFactory.fromResource(R.drawable.marker_0000ff)
                        coin.markerColor == "#008000" -> iconFactory.fromResource(R.drawable.marker_008000)
                        coin.markerColor == "#ff0000" -> iconFactory.fromResource(R.drawable.marker_ff0000)
                        else -> iconFactory.fromResource(R.drawable.marker_ffdf00)
                    }

                    markers.add(MarkerOptions()
                            .position(LatLng(coin.latitude, coin.longitude))
                            .icon(icon)
                            .title(coin.type + ": " + coin.markerSymbol)
                            .snippet("Value: " + coin.value))
                }

                map!!.addMarkers(markers)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == profile){
            if (resultCode == Activity.RESULT_OK){
                tvNavUserName!!.text = data!!.getStringExtra("name")
                Toast.makeText(this@MainActivity, "Update Successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == pickName&&data != null&&data.data != null){
            if (resultCode == Activity.RESULT_OK){
                val filePath = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                    uploadFile(bitmap, filePath!!)
                    Toast.makeText(this@MainActivity, "Update Successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }
}