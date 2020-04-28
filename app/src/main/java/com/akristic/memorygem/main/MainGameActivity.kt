package com.akristic.memorygem.main


import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.akristic.memorygem.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.lang.Runnable
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.akristic.memorygem.database.GemDatabase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.Games
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.PlayersClient
import com.google.android.gms.common.api.ApiException


private const val RC_SIGN_IN = 9001;
private const val RC_UNUSED = 5001
private const val RC_ACHIEVEMENT_UI = 9003;

class MainGameActivity : AppCompatActivity(), RewardedVideoAdListener {
    // Client used to sign in with Google APIs
    private var mGoogleSignInAccount: GoogleSignInAccount? = null
    // Client variables
    private var mAchievementsClient: AchievementsClient? = null
    private var mLeaderboardsClient: LeaderboardsClient? = null
    private var mPlayersClient: PlayersClient? = null
    //Admob variables
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    lateinit var sharedViewModel: MainGameViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNUSED_VARIABLE")
        setContentView(R.layout.activity_main)
        // initialize Admob
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        //initialize viewModel
        val dataSource = GemDatabase.getInstance(application).gemDatabaseDao
        val viewModelFactory = MainGameViewModelFactory(dataSource, application)
        sharedViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainGameViewModel::class.java)

        //hide UI if it is shown for more than delay time in milliseconds
        supportActionBar?.hide();
        val mHideHandler = Handler()
        val mHidePart2Runnable = Runnable {
            hideSystemUI()
        }
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                mHideHandler.postDelayed(mHidePart2Runnable, 1500)
                // adjustments to your UI, such as showing the action bar or
                // other navigational controls.
            }
        }


        sharedViewModel.currentGame.observe(this, Observer {
            if (it != null) {
                sharedViewModel.isGameStarted(true)
            }
        })
     }

    /**
     * Add mob methods start
     */
    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            getString(R.string.admob_video_id),
            AdRequest.Builder().build()
        )
    }

    override fun onRewarded(reward: RewardItem) {
        sharedViewModel.addExtraLife(1)
        incrementLifeSaverAchievements()
    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {

    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoCompleted() {

    }

    fun showRewardedVideoAd() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(this) // add mob
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        signInSilently()//go to full screen
        mRewardedVideoAd.resume(this) // add mob
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(this) // add mob
    }

    // Admob methods end

    private fun signInSilently() {
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            mGoogleSignInAccount = account
            onConnected(mGoogleSignInAccount!!)
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            val signInClient = GoogleSignIn.getClient(this, signInOptions)
            signInClient
                .silentSignIn()
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // The signed in account is stored in the task's result.
                        mGoogleSignInAccount = task.result
                        onConnected(mGoogleSignInAccount!!)
                    } else {
                        onDisconnect()
                        // Player will need to sign-in explicitly using via UI.
                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                        // Interactive Sign-in.
                    }
                }
        }
    }

    fun startSignInIntent() {
        val signInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        val intent = signInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // The signed in account is stored in the result.
                mGoogleSignInAccount = result.signInAccount
                val accauntGoogle = mGoogleSignInAccount
                accauntGoogle?.let {
                    onConnected(accauntGoogle)
                }
            } else {
                var message = result.status.statusMessage
                if (message == null || message.isEmpty()) {
                    message = "Unknown error"
                }
                AlertDialog.Builder(this).setMessage(
                    getString(R.string.google_connect_error_message, message)
                )
                    .setNeutralButton(android.R.string.ok, null).show()
                sharedViewModel.setIsSignIn(false)
            }
        }
    }

    fun onConnected(googleSignInAccount: GoogleSignInAccount) {
        sharedViewModel.setIsSignIn(true)
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount)
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount)
    }

    fun signOut() {
        val signInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        signInClient.signOut().addOnCompleteListener(
            this
        ) {
            onDisconnect()
        }
    }

    fun onDisconnect() {
        sharedViewModel.setIsSignIn(false)
        mAchievementsClient = null
        mLeaderboardsClient = null
        mPlayersClient = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    fun updateLeaderboards(score: Long) {
        mLeaderboardsClient?.let {
            it.submitScore(getString(R.string.leaderboard_hall_of_fame), score)
        }
    }

    fun showLeaderboard() {
        if (sharedViewModel.isSignIn.value == false) {
            Toast.makeText(this, getString(R.string.need_to_sign_in_message), Toast.LENGTH_SHORT).show()
        }
        mLeaderboardsClient?.let {
            it.getAllLeaderboardsIntent()
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_UNUSED) }
                .addOnFailureListener { e -> handleException(e, "Leaderboard error") }
        }
    }


    private fun handleException(e: Exception, details: String) {
        var status = 0

        if (e is ApiException) {
            status = e.statusCode
        }
        val message = "Error! \nDetails: $details\nStatus: $status\n Exception $e"

        AlertDialog.Builder(this@MainGameActivity)
            .setMessage(message)
            .setNeutralButton(android.R.string.ok, null)
            .show()
    }

    //Achievements
    fun showAchievements() {
        if (sharedViewModel.isSignIn.value == false) {
            Toast.makeText(this, getString(R.string.need_to_sign_in_message), Toast.LENGTH_SHORT).show()
        }
        mAchievementsClient?.let {
            it.getAchievementsIntent()
                .addOnSuccessListener { intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI) }
                .addOnFailureListener { e -> handleException(e, "Achievements error") }
        }
    }

    fun unlockPrankedAchievement() {
        mAchievementsClient?.let {
            val currentClient = mAchievementsClient
            currentClient?.unlock(getString(R.string.achievement_pranked))
        }
    }

    fun incrementBoredAchievements() {
        mAchievementsClient?.let {
            val currentClient = mAchievementsClient
            currentClient?.increment(getString(R.string.achievement_bored), 1)
            currentClient?.increment(getString(R.string.achievement_really_bored), 1)
            currentClient?.increment(getString(R.string.achievement_super_extra_bored), 1)
        }
    }

    fun unlockScoreAchievements(score: Int) {
        if (score >= 500) {
            mAchievementsClient?.let {
                val currentClient = mAchievementsClient
                currentClient?.unlock(getString(R.string.achievement_legend))
            }
            if (score >= 1000) {
                mAchievementsClient?.let {
                    val currentClient = mAchievementsClient
                    currentClient?.unlock(getString(R.string.achievement_genius))
                }
                if (score >= 1500) {
                    mAchievementsClient?.let {
                        val currentClient = mAchievementsClient
                        currentClient?.unlock(getString(R.string.achievement_super_natural))
                    }
                }
            }
        }
    }

    fun incrementLifeSaverAchievements() {
        mAchievementsClient?.let {
            val currentClient = mAchievementsClient
            currentClient?.increment(getString(R.string.achievement_life_saver_i), 1)
            currentClient?.increment(getString(R.string.achievement_life_saver_ii), 1)
            currentClient?.increment(getString(R.string.achievement_life_saver_iii), 1)
        }
    }

    fun unlockFastStartAchievement(level: Int, score: Int, lives: Int, difficulty: Int) {
        if (level == 1 && score >= 55 && lives == 3 && difficulty == 1) {
            mAchievementsClient?.let {
                val currentClient = mAchievementsClient
                currentClient?.unlock(getString(R.string.achievement_fast_start))
            }
        }

    }

    fun unlockNeedForSpeedAchievement(level: Int, score: Int, lives: Int, difficulty: Int) {
        if (level == 3 && score >= 447 && lives == 3 && difficulty == 1) {
            mAchievementsClient?.let {
                val currentClient = mAchievementsClient
                currentClient?.unlock(getString(R.string.achievement_need_for_speed))
            }
        }
    }

    fun unlockDieHardAchievement(level: Int, lives: Int, difficulty: Int) {
        if (level >= 5 && lives == 3 && difficulty == 2) {
            mAchievementsClient?.let {
                val currentClient = mAchievementsClient
                currentClient?.unlock(getString(R.string.achievement_die_hard))
            }
        }
    }

}
