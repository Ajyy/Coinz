package com.example.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInterfaceTest{

    @get: Rule
    val mActivityRule = ActivityTestRule(LoginInterface::class.java)

    @Test
    fun testLogin(){
        onView(withId(R.id.etUserName))
                .perform(typeText("a@a.com"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
                .perform(click())

        onView(withId(R.id.tvInf))
                .check(matches(withText("Please enter your password")))

        onView(withId(R.id.etPassword))
                .perform(typeText("wrongPassword"), closeSoftKeyboard())

        onView(withId(R.id.etUserName))
                .perform(replaceText(""))

        onView(withId(R.id.btnLogin))
                .perform(click())

        onView(withId(R.id.tvInf))
                .check(matches(withText("Please enter your email")))
    }
}