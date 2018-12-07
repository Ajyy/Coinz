package com.example.coinz

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.NavigationViewActions
import android.view.Gravity

@RunWith(AndroidJUnit4::class)
class MainActivityTest{
    @get: Rule
    val mActivityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun grantPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            with(getInstrumentation().uiAutomation){
                executeShellCommand("appops set " + InstrumentationRegistry.getTargetContext().packageName + " android:mock_location allow")
                Thread.sleep(1000)
            }
        }
    }

    @Test
    fun testCollectCoins(){
        val lm = mActivityRule.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE

        val mockLocationProvider = LocationManager.GPS_PROVIDER

        lm.addTestProvider(mockLocationProvider, false, false, false, false,
                true, true, true, 0, 5)
        lm.setTestProviderEnabled(mockLocationProvider, true)

        val loc = Location(mockLocationProvider)
        val mockLocation = Location(mockLocationProvider)

        val coords = ArrayList<Array<Double>>()
        try {
            var reader = BufferedReader(InputStreamReader(mActivityRule.activity.assets.open("trace1input")))
            var line = reader.readLine()
                while (line != null){
                    coords.add(arrayOf(line.substring(0, 9).toDouble(), line.substring(10, 19).toDouble()))
                    line = reader.readLine()
                }

            reader = BufferedReader(InputStreamReader(mActivityRule.activity.assets.open("trace2input")))
            line = reader.readLine()
            while (line != null){
                coords.add(arrayOf(line.substring(0, 9).toDouble(), line.substring(10, 19).toDouble()))
                line = reader.readLine()
            }
        } catch (e: IOException){
            print(e.localizedMessage)
        }

        for (coord in coords){
            mockLocation.latitude = coord[1]
            mockLocation.longitude = coord[0]
            Log.d("MainActivityTest", coord[0].toString()+" "+coord[1].toString())
            mockLocation.altitude = loc.altitude
            mockLocation.time = System.currentTimeMillis()
            mockLocation.accuracy = 1F
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            }
            lm.setTestProviderLocation(mockLocationProvider, mockLocation)
            onView(withId(R.id.fabLocation)).perform(click())
        }
    }
}