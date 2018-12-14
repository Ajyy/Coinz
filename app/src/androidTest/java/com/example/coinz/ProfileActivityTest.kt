package com.example.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileActivityTest{

    @get: Rule
    val mActivityRule = ActivityTestRule(ProfileActivity::class.java)

    // Test whether update profile successfully by checking the displayName, please make sure login already
    @Test
    fun testUpdateProfile(){
        onView(withId(R.id.etName))
                .perform(typeText("CoinzPlayer"), closeSoftKeyboard())

        onView(withId(R.id.etAge))
                .perform(typeText("10"), closeSoftKeyboard())

        onView(withId(R.id.btnSubmit))
                .perform(click())

        Thread.sleep(5000)

        check(User.getUser()!!.displayName == "CoinzPlayer")
    }
}