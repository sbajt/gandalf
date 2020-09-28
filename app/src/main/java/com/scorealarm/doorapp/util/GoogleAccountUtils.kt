package com.scorealarm.doorapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.RuntimeExecutionException
import com.scorealarm.doorapp.R
import io.reactivex.subjects.PublishSubject

object GoogleAccountUtils {

    private const val googleAccountRequestId =
            "788174005347-h1k1h2nn1jhhd7p1i7tjjti68s0hqfg9.apps.googleusercontent.com"
    private val TAG = GoogleAccountUtils::class.java.canonicalName
    val signInSubject = PublishSubject.create<Boolean>()
    private val gso by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(googleAccountRequestId)
                .requestEmail()
                .build()
    }

    var isSignedIn = false
    private lateinit var gsc: GoogleSignInClient

    fun setup(activity: Activity) {
        gsc = GoogleSignIn.getClient(activity, gso)
    }

    fun sendSignInIntent(activity: Activity, requestCode: Int) {
        val signInIntent: Intent = gsc.signInIntent
        activity.startActivityForResult(signInIntent, requestCode)
    }

    @SuppressLint("CheckResult")
    fun silentlySignIn(context: Context) {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastAccount != null && !lastAccount.isExpired) {
            gsc.silentSignIn()?.addOnCompleteListener {
                Log.d(TAG, "Silently signed into google fetching token")
                try {
                    val token = it.result?.idToken
                    if (!token.isNullOrBlank()) {
                        isSignedIn = true
                        signInSubject.onNext(true)
                    }
                } catch (e: RuntimeExecutionException) {
                    isSignedIn = false
                    signInSubject.onNext(false)
                    Log.e(TAG, context.getString(R.string.message_error_silent_signin))
                }
            }?.addOnFailureListener {
                Log.e(TAG, context.getString(R.string.message_error_silent_signin))
            }
        }
    }

    fun signOut(context: Context) {
        gsc.signOut()?.addOnCompleteListener {
            isSignedIn = false
            signInSubject.onNext(false)
        }?.addOnFailureListener {
            Log.e(TAG, context.getString(R.string.message_error_google), it)
        }
    }


    fun notify(signedIn: Boolean) {
        isSignedIn = signedIn
        signInSubject.onNext(signedIn)
    }

}