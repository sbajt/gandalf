package com.scorealarm.doorapp.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.scorealarm.doorapp.R
import com.scorealarm.doorapp.rest.RestService
import com.scorealarm.doorapp.util.GoogleAccountUtils
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.canonicalName
    private val RESULT_CODE_SIGN_IN = 9001
    private val disposable = CompositeDisposable()

    override fun attachBaseContext(newBase: Context?) {
        newBase?.apply {
            super.attachBaseContext(ViewPumpContextWrapper.wrap(this))
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GoogleAccountUtils.setup(this)
        initButtonListeners()
        initObservables()
    }

    override fun onStart() {
        super.onStart()
        if (!UiUtils.isAirplaneModeOn(this)) {
            GoogleAccountUtils.silentlySignIn(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "$requestCode, $resultCode, $data")
        val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RESULT_CODE_SIGN_IN -> {
                    try {
                        if (signInResult?.isSuccess == true) {
                            GoogleAccountUtils.notify(true)
                        }
                    } catch (ex: ApiException) {
                        GoogleAccountUtils.notify(false)
                        Log.e(TAG, getString(R.string.message_error_google), ex)
                        Toast.makeText(this, R.string.message_error_login, Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
        } else {
            Log.e(TAG, getString(R.string.message_error_login))
            Toast.makeText(this, R.string.message_error_login, Toast.LENGTH_SHORT)
                    .show()
            GoogleAccountUtils.notify(false)
        }
        return
    }

    private fun openTheDoor() {
        val token = GoogleSignIn.getLastSignedInAccount(this)?.idToken
        if (!token.isNullOrBlank()) {
            restProgressIndicatorView?.visibility = View.VISIBLE
            disposable.add(
                    RestService.doorApi.activate(token)
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                Log.d(TAG, getString(R.string.REST_response_OK) + " $it")
                                openTheDoorProgressView?.post {
                                    Toast.makeText(
                                            this,
                                            R.string.REST_response_OK,
                                            Toast.LENGTH_LONG
                                    ).show()
                                    restProgressIndicatorView?.visibility = View.GONE

                                    onRESTResponse(it.validUntil ?: DateTime.now())
                                }
                            }, {
                                openTheDoorProgressView?.post {
                                    openTheDoorProgressView?.setProgressPercent(-1f)
                                    restProgressIndicatorView?.visibility = View.GONE
                                    openTheDoorProgressView?.isClickable = true
                                    Toast.makeText(
                                            this,
                                            it.cause.toString(),
                                            Toast.LENGTH_LONG
                                    ).show()
                                    Log.e(TAG, getString(R.string.message_error_REST), it)
                                }
                            })
            )
        } else {
            changeUI()
            openTheDoorProgressView?.setProgressPercent(-1f)
            openTheDoorProgressView?.isClickable = true
            Toast.makeText(
                    this,
                    R.string.message_error_REST,
                    Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, getString(R.string.message_error_REST))
        }
    }

    private fun initObservables() {
        observeAccountSignStatus()
    }

    private fun observeAccountSignStatus() {
        disposable.add(
                GoogleAccountUtils.signInSubject
                        .subscribe({
                            changeUI()
                        }, {
                            changeUI()
                            Log.e(TAG, getString(R.string.message_error_google), it)
                        })
        )
    }

    private fun initButtonListeners() {
        openTheDoorProgressView.setOnClickListener {
            it.isClickable = false
            if (!UiUtils.isAirplaneModeOn(this) && GoogleAccountUtils.isSignedIn) {
                openTheDoor()
            }
        }

        signInButtonView.setOnClickListener {
            if (!UiUtils.isAirplaneModeOn(this)) {
                GoogleAccountUtils.sendSignInIntent(this, RESULT_CODE_SIGN_IN)
            }
        }

        signOutButtonView.setOnClickListener {
            if (!UiUtils.isAirplaneModeOn(this)) {
                GoogleAccountUtils.signOut(this)
            }
        }
    }

    private fun onRESTResponse(response: DateTime) {
        openTheDoorProgressView?.post {
            val validUntil: DateTime = response
            val durationSeconds = Duration(DateTime.now(), validUntil).standardSeconds
            if (durationSeconds > 0) {
                UiUtils.vibrateOpenTheDoor(this)
                disposable.add(
                        Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                                .takeUntil { it >= durationSeconds }
                                .subscribe({
                                    openTheDoorProgressView?.post {
                                        openTheDoorProgressView?.setProgressPercent((it + 1) * 100f / durationSeconds)
                                        Log.d(TAG, "${(it + 1) * 100f / durationSeconds}")
                                    }
                                }, {
                                    openTheDoorProgressView?.post {
                                        Toast.makeText(
                                                this,
                                                it.cause.toString(),
                                                Toast.LENGTH_SHORT
                                        )
                                                .show()
                                        Log.e(TAG, it.cause.toString(), it)
                                    }
                                }, {
                                    openTheDoorProgressView?.post {
                                        openTheDoorProgressView?.setProgressPercent(-1f)
                                        openTheDoorProgressView?.isClickable = true
                                    }
                                })
                )
            } else {
                Toast.makeText(
                        this,
                        R.string.message_error_REST_time,
                        Toast.LENGTH_SHORT
                )
                        .show()
                Log.e(TAG, getString(R.string.message_error_REST_time))
                openTheDoorProgressView?.isClickable = true
                openTheDoorProgressView?.setProgressPercent(0f)
            }
        }
    }

    private fun changeUI() {
        logoImageView?.visibility =
                if (!GoogleAccountUtils.isSignedIn)
                    View.VISIBLE
                else
                    View.GONE

        signInButtonView?.visibility =
                if (!GoogleAccountUtils.isSignedIn)
                    View.VISIBLE
                else
                    View.GONE

        signOutButtonView?.visibility =
                if (GoogleAccountUtils.isSignedIn)
                    View.VISIBLE
                else
                    View.GONE

        bgImageView?.visibility =
                if (GoogleAccountUtils.isSignedIn)
                    View.VISIBLE
                else
                    View.GONE

        openTheDoorProgressView?.visibility =
                if (GoogleAccountUtils.isSignedIn)
                    View.VISIBLE
                else
                    View.GONE
    }
}

