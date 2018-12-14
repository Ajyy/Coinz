package com.example.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.app.Activity
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.runner.lifecycle.Stage.RESUMED
import android.util.Log
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import org.junit.After
import android.content.Intent

@RunWith(AndroidJUnit4::class)
class LoginActivityTest{

    val tag = "LoginActivityTest"

    @get: Rule
    val mActivityRule = ActivityTestRule(LoginActivity::class.java, true, false)

    // Create a account with email "a@a.com" and password "123456"
    @Before
    fun createAccount(){
        User.mAuth.signInWithEmailAndPassword("a@a.com", "123456").addOnCompleteListener {task ->
            if (task.isSuccessful){
                Log.d(tag, "signInWithEmail: success")
                User.mAuth.signOut()
                User.userAuth = User.mAuth.currentUser
            } else {
                Log.d(tag, "signInWithEmail: fail")
                User.mAuth.createUserWithEmailAndPassword("a@a.com", "123456").addOnCompleteListener { task1 ->
                    if (task1.isSuccessful){
                        User.mAuth.signOut()
                        User.userAuth = User.mAuth.currentUser

                        Log.d(tag, "createUserWithEmail: success")
                    } else {
                        try {
                            throw task1.exception!!
                        } catch (existEmail: FirebaseAuthUserCollisionException){
                            Log.d(tag, "createUserWithEmail: exist_email")
                        } catch (e: Exception) {
                            Log.w(tag, "createUserWithEmail: " + e.message)
                        }
                    }
                }
            }
        }

        Thread.sleep(5000)
    }

    // Test whether login successfully, note that please log out when test
    @Test
    fun testLogin(){
        val intent = Intent(Intent.ACTION_PICK)
        mActivityRule.launchActivity(intent)
        onView(withId(R.id.etUserName))
                .perform(typeText("a@a.com"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
                .perform(click())

        onView(withId(R.id.tvInf))
                .check(matches(withText("Please enter your password")))

        onView(withId(R.id.etPassword))
                .perform(typeText("123457"), closeSoftKeyboard())

        onView(withId(R.id.etUserName))
                .perform(replaceText(""))

        onView(withId(R.id.btnLogin))
                .perform(click())

        onView(withId(R.id.tvInf))
                .check(matches(withText("Please enter your email")))

        onView(withId(R.id.etUserName))
                .perform(typeText("a@a.com"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
                .perform(click())

        Thread.sleep(6000)

        onView(withId(R.id.tvInf))
                .check(matches(withText("Wrong password")))

        onView(withId(R.id.etPassword))
                .perform(replaceText(""))

        onView(withId(R.id.etPassword))
                .perform(typeText("123456"), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
                .perform(click())

        Thread.sleep(10000)

        check(getActivityInstance().localClassName == "MainActivity")
    }

    @After
    fun logOut(){
        User.mAuth.signOut()
        User.userAuth = User.mAuth.currentUser
    }

    private fun getActivityInstance(): Activity {
        var currentActivity: Activity? = null
        getInstrumentation().runOnMainSync {
            val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED)
            if (resumedActivities.iterator().hasNext()) {
                currentActivity = resumedActivities.iterator().next()
            }
        }

        return currentActivity!!
    }
}