package com.akristic.memorygem.screens.gameOver


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.akristic.memorygem.R
import com.akristic.memorygem.database.GemDatabase
import com.akristic.memorygem.databinding.FragmentGameOverBinding
import com.akristic.memorygem.main.MainGameActivity
import com.akristic.memorygem.main.MainGameViewModel



/**
 * A simple [Fragment] subclass.
 *
 */
class GameOverFragment : Fragment() {
    private lateinit var viewModel: GameOverViewModel
    private lateinit var binding: FragmentGameOverBinding
    private lateinit var viewModelFactory: GameOverModelFactory
    private lateinit var sharedViewModel: MainGameViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_game_over, container, false
        )
        val application = requireNotNull(this.activity).application
        val dataSource = GemDatabase.getInstance(application).gemDatabaseDao
        val scoreFragmentArgs by navArgs<GameOverFragmentArgs>()

        viewModelFactory = GameOverModelFactory(scoreFragmentArgs.level, scoreFragmentArgs.finalScore, dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameOverViewModel::class.java)

        binding.gameOverViewModel = viewModel
        if (null == getShareIntent().resolveActivity(activity!!.packageManager)) {
            // hide share button if it doesn't resolve
            binding.gameOverShareButton.visibility = View.GONE
        } else {
            binding.gameOverShareButton.setOnClickListener { _ ->
                shareSuccess()
            }
        }
        binding.gameOverExtraLifeButton.setOnClickListener { _ ->
            (activity as MainGameActivity).showRewardedVideoAd()
        }

        binding.gameOverOkButton.setOnClickListener { _ ->
            if (sharedViewModel.extraLife.value == 1) {
                val action = GameOverFragmentDirections.actionGameOverFragmentToGameFragment()
                action.setCurrentGamePlaying(true)
                NavHostFragment.findNavController(this).navigate(action)
            } else {
                viewModel.onGameOver()//delete current game from database
                (activity as MainGameActivity).incrementBoredAchievements()
                (activity as MainGameActivity).unlockScoreAchievements(scoreFragmentArgs.finalScore)
                //this is really game over and we notify sharedViewModel that that game is no longer started
                sharedViewModel.isGameStarted(false)
                //and now we return to main menu
                val action = GameOverFragmentDirections.actionGameOverFragmentToMenuFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        }

        // Get args using by navArgs property delegate
        binding.gameOverMessageText.text = getString(R.string.game_over_message, viewModel.score.value)

        //update score to google game service
        (activity as MainGameActivity).updateLeaderboards(scoreFragmentArgs.finalScore.toLong())
        return binding.root
    }

    // Creating our Share Intent
    private fun getShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
            .putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_success_text, viewModel.level.value, viewModel.score.value)
            )
        return shareIntent
    }

    // Starting an Activity with our new Intent
    private fun shareSuccess() {
        startActivity(getShareIntent())
    }

    /**
     * we track changes in MainGameViewModel of MainGameActivity if user is signed in or not
     * and change visibility of SingIn buttons accordingly
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // sharedViewModel must be in same scope
        activity?.let {
            sharedViewModel = ViewModelProviders.of(it).get(MainGameViewModel::class.java)
            if (sharedViewModel.enableExtraLife.value == false) {
                binding.gameOverExtraLifeButton.visibility = View.INVISIBLE
            }
            sharedViewModel.enableExtraLife.observe(this, Observer { enabled ->
                if (enabled) {
                    binding.gameOverExtraLifeButton.visibility = View.VISIBLE
                } else {
                    binding.gameOverExtraLifeButton.visibility = View.INVISIBLE
              }
            })
            sharedViewModel.extraLife.observe(this, Observer { life ->
                if (life==0){
                    binding.gameOverOkButton.text = getString(R.string.ok)
                    binding.gameOverMessageText.text = getString(R.string.game_over_message, viewModel.score.value)
                }else{
                    binding.gameOverOkButton.text = getString(R.string.try_again)
                    binding.gameOverMessageText.text = getString(R.string.extra_life_message)
                }
            })
        }
    }
}
