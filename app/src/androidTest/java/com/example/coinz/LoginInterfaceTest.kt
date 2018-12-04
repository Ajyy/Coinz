package com.example.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.action.ViewActions.closeSoftKeyboard
import android.support.test.espresso.intent.Intents.*
import android.support.test.runner.AndroidJUnit4
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInterfaceTest{

    @get: Rule
    val mActivityRule = IntentsTestRule(LoginInterface::class.java)

    @Test
    fun testLogin(){
        onView(withId(R.id.etUserName))
//                .perform(typeText("a@a.com"), closeSoftKeyboard())
//
//        onView(withId(R.id.etPassword))
//                .perform(typeText("123456"), closeSoftKeyboard())

        onView(withId(R.id.tvFPass))
                .perform(click())

        intended(hasComponent(ResetPasswordActivity::class.java.name))
    }
}