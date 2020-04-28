package com.akristic.memorygem.screens.game


import android.content.Context
import android.media.AudioManager
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.akristic.memorygem.R
import com.akristic.memorygem.utility.Tile
import com.akristic.memorygem.database.GemDatabase
import com.akristic.memorygem.databinding.GameFragmentBinding
import com.akristic.memorygem.animation.Animation
import com.akristic.memorygem.main.MainGameActivity
import com.akristic.memorygem.main.MainGameViewModel
import com.akristic.memorygem.sound.Sound
import java.util.ArrayList


class GameFragment : Fragment() {
    private lateinit var viewModel: GameViewModel
    private lateinit var binding: GameFragmentBinding
    private lateinit var sharedViewModel: MainGameViewModel
    var buttonsList = ArrayList<Tile>()
    private lateinit var animation: Animation
    lateinit var sound: Sound
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.game_fragment,
            container,
            false
        )
        val application = requireNotNull(this.activity).application
        val dataSource = GemDatabase.getInstance(application).gemDatabaseDao
        val viewModelFactory = GameViewModelFactory(dataSource, application)

        val gameFragmentArgs by navArgs<GameFragmentArgs>()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)
        binding.gameViewModel = viewModel
        binding.setLifecycleOwner(this)


        //initialize animation and sound
        sound = Sound(this.context?.applicationContext)
        animation = Animation()
        //make list of buttons
        buttonsList.add(Tile(binding.mainButton1, 1))
        buttonsList.add(Tile(binding.mainButton2, 2))
        buttonsList.add(Tile(binding.mainButton3, 3))
        buttonsList.add(Tile(binding.mainButton4, 4))
        buttonsList.add(Tile(binding.mainButton5, 5))

        viewModel.isGameOver.observe(this, Observer { isGameOver ->
            if (isGameOver) {
                gameOver()

            }
        })
        viewModel.isLifeLost.observe(this, Observer { isLifeLost ->
            if (isLifeLost) {
                playPrankAndLoseLife()
            }
        })
        viewModel.eventBuzz.observe(this, Observer { buzzType ->
            if (buzzType != GameViewModel.BuzzType.NO_BUZZ) {
                buzz(buzzType.pattern)
                viewModel.onBuzzComplete()
            }
        })
        viewModel.fastAchievementTrigger.observe(this, Observer { isTriggered ->
            if (isTriggered) {
                (activity as MainGameActivity).unlockFastStartAchievement(
                    viewModel.level.value ?: 0,
                    viewModel.score.value ?: 0,
                    viewModel.numberOfLives.value ?: 0,
                    viewModel.gameDifficulty.value ?: 0
                )
                (activity as MainGameActivity).unlockNeedForSpeedAchievement(
                    viewModel.level.value ?: 0,
                    viewModel.score.value ?: 0,
                    viewModel.numberOfLives.value ?: 0,
                    viewModel.gameDifficulty.value ?: 0
                )
                (activity as MainGameActivity).unlockDieHardAchievement(
                    viewModel.level.value ?: 0,
                    viewModel.numberOfLives.value ?: 0,
                    viewModel.gameDifficulty.value ?: 0
                )
                viewModel.fastAchievementComplete()
            }
        })

        viewModel.buttonClicked.observe(this, Observer { buttonNumber ->
            if (buttonNumber >= 0) {
                if (!animation.isAnimationShowing) {
                    animation.buttonClickedAnimation(buttonsList, buttonNumber)
                    sound.playGemSound(buttonNumber)
                    viewModel.checkIsRightButtonClicked(buttonNumber)
                }
                viewModel.onButtonClickedComplete()
            }
        })
        viewModel.showAnimationLevelComplete.observe(this, Observer { showAnimation ->
            if (showAnimation) {
                animation.showAnimationLevelComplete(buttonsList, sound)
                viewModel.onShowAnimationCompleteLevelComplete()
            }
        })

        viewModel.showAnimationSequence.observe(this, Observer { showAnimation ->
            if (showAnimation) {
                val buttonSequence: ArrayList<Int> = viewModel.buttonsSequence.value ?: ArrayList(listOf(0, 0))
                animation.showAnimationSequence(buttonSequence, buttonsList, sound, viewModel)
                viewModel.onShowAnimationSequenceComplete()
            }
        })

        viewModel.level.observe(this, Observer { level ->
            if (level==4){
                binding.monsterImage.setImageResource(R.drawable.clown)
            }
            if (level==6){
                binding.monsterImage.setImageResource(R.drawable.chucky)
            }
        })

        binding.mainExitButton.setOnClickListener { _:View ->
            val action = GameFragmentDirections.actionGameFragmentToMenuFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        if (gameFragmentArgs.currentGamePlaying == false) {
            viewModel.startNewGame()
        } else {
            viewModel.resumeGame()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sound.release()
    }

    /**
     * Called when the game is finished
     */
    private fun gameOver() {
        val action = GameFragmentDirections.actionGameFragmentToGameOverFragment()
        action.setFinalScore(viewModel.score.value ?: 0)
        action.setLevel(viewModel.level.value ?: 1)
        NavHostFragment.findNavController(this).navigate(action)
        viewModel.gameOverComplete()
    }

    /**
     * called when life is lost
     */
    private fun lifeLost() {
        val action = GameFragmentDirections.actionGameFragmentToLifeLostFragment()
        NavHostFragment.findNavController(this).navigate(action)
        viewModel.lifeLostComplete()
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }

    //play prank part to scare player or just lose life
    private fun playPrankAndLoseLife() {
        if (viewModel.level.value ?: 0 >= 2 && viewModel.isPrankEnabled.value == 1) {
            //turn volume to max
            val audioManager = this.context?.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            //remember current volume
            val mediaDefaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val mediaMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaMaxVolume, AudioManager.FLAG_VIBRATE)
            //get monster image and start animation and sound
            val imageViewMonster = binding.monsterImage
            animation.animateMonsterImage(imageViewMonster, sound)

            //hide monster image and lose life after animation
            val handlerGone = Handler()
            handlerGone.postDelayed({
                imageViewMonster.visibility = View.GONE
                //set volume back to normal
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaDefaultVolume, AudioManager.FLAG_VIBRATE)
                lifeLost()//we still lose life right
                (activity as MainGameActivity).unlockPrankedAchievement() // this should be done only once
            }, Animation.Duration.ANIMATION_DURATION.milliseconds * 4)
        } else {
            lifeLost()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(MainGameViewModel::class.java)
            if (sharedViewModel.extraLife.value == 1) {
                viewModel.addExtraLife()
                sharedViewModel.addExtraLifeComplete()
            }
            //start a new game if game is not started and set that game is started
            if (!(sharedViewModel.isGameStarted.value ?: false)) {
                sharedViewModel.enableExtraLife()//check if it is new game and enable extra life so it is shown again in game over fragment
                sharedViewModel.isGameStarted(true)
            }
        }
   }

}
