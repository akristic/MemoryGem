package com.akristic.memorygem.screens.menu


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.akristic.memorygem.R
import com.akristic.memorygem.databinding.FragmentMenuBinding
import com.akristic.memorygem.main.MainGameActivity
import com.akristic.memorygem.main.MainGameViewModel


class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var sharedViewModel: MainGameViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_menu, container, false
        )

        binding.setLifecycleOwner(this)
        binding.signOutButton.setOnClickListener { _: View ->
            (activity as MainGameActivity).signOut()
        }

        binding.signInButton.setOnClickListener { _: View ->
            (activity as MainGameActivity).startSignInIntent()
        }
        binding.menuQuit.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_menuFragment_to_quitFragment))
        binding.menuOptions.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_menuFragment_to_optionsFragment))
        binding.menuLeaderboards.setOnClickListener { _: View ->
            (activity as MainGameActivity).showLeaderboard()
        }
        binding.menuAchievements.setOnClickListener { _: View ->
            (activity as MainGameActivity).showAchievements()
        }

        binding.menuNewGame.setOnClickListener { _: View ->
            startNewGame()
        }
        binding.menuResumeGame.setOnClickListener { _: View ->
            resumeOldGame()
        }
        binding.menuHelp.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_menuFragment_to_helpFragment))

        return binding.root
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
            sharedViewModel.isSignIn.observe(this, Observer {
                changeVisibilityOfSignInButtons(it)
            })
            sharedViewModel.isGameStarted.observe(this, Observer {
                changeVisibilityOfResumeButton(it)
            })

        }
    }

    private fun changeVisibilityOfResumeButton(isGameStarted: Boolean) {
        if (isGameStarted) {
            binding.menuResumeGame.visibility = View.VISIBLE
        } else {
            binding.menuResumeGame.visibility = View.GONE
        }
    }

    private fun changeVisibilityOfSignInButtons(isSignIn: Boolean) {
        if (isSignIn) {
            binding.signInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
        } else {
            binding.signInButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
        }
    }

    private fun startNewGame() {
        val action = MenuFragmentDirections.actionMenuFragmentToGameFragment()
        action.setCurrentGamePlaying(false)
        NavHostFragment.findNavController(this).navigate(action)
    }

    private fun resumeOldGame() {
        val action = MenuFragmentDirections.actionMenuFragmentToGameFragment()
        action.setCurrentGamePlaying(true)
        NavHostFragment.findNavController(this).navigate(action)
    }
}
