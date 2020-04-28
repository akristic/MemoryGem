package com.akristic.memorygem.screens.options


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.akristic.memorygem.R
import com.akristic.memorygem.database.GemDatabase
import com.akristic.memorygem.databinding.FragmentOptionsBinding


class OptionsFragment : Fragment() {
    private lateinit var viewModel: OptionsViewModel
    private lateinit var binding: FragmentOptionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_options, container, false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = GemDatabase.getInstance(application).gemDatabaseDao
        val viewModelFactory = OptionsViewModelFactory(dataSource, application)


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(OptionsViewModel::class.java)
        binding.optionsViewModel = viewModel
        binding.setLifecycleOwner(this)

        binding.optionsPrankModeButton.setOnClickListener { _: View ->
            viewModel.prankModeButtonClicked()
        }
        viewModel.prankModeEnabled.observe(this, Observer {
            if (viewModel.prankModeEnabled.value == 0) {
                binding.optionsPrankModeButton.setText(R.string.prank_mode_off)
                binding.optionsPrankModeButton.textSize = 16F
            } else {
                binding.optionsPrankModeButton.setText(R.string.prank_mode_on)
                binding.optionsPrankModeButton.textSize = 22F
            }
        })

        binding.optionsEasyButton.setOnClickListener { _: View ->
            viewModel.setGameDifficulty(OptionsViewModel.GameDifficulty.EASY.difficulty)
        }
        binding.optionsNormalButton.setOnClickListener { _: View ->
            viewModel.setGameDifficulty(OptionsViewModel.GameDifficulty.NORMAL.difficulty)
        }
        binding.optionsHardButton.setOnClickListener { _: View ->
            viewModel.setGameDifficulty(OptionsViewModel.GameDifficulty.HARD.difficulty)
        }

        viewModel.gameDifficulty.observe(this, Observer {
            if (viewModel.gameDifficulty.value == OptionsViewModel.GameDifficulty.EASY.difficulty) {
                binding.optionsEasyButton.textSize = 24F
                binding.optionsNormalButton.textSize = 16F
                binding.optionsHardButton.textSize = 16F
            }
            if (viewModel.gameDifficulty.value == OptionsViewModel.GameDifficulty.NORMAL.difficulty) {
                binding.optionsEasyButton.textSize = 16F
                binding.optionsNormalButton.textSize = 24F
                binding.optionsHardButton.textSize = 16F
            }
            if (viewModel.gameDifficulty.value == OptionsViewModel.GameDifficulty.HARD.difficulty) {
                binding.optionsEasyButton.textSize = 16F
                binding.optionsNormalButton.textSize = 16F
                binding.optionsHardButton.textSize = 24F
            }
        })

        binding.optionsOkButton.setOnClickListener { _: View ->
            viewModel.okButtonClicked()
            val action = OptionsFragmentDirections.actionOptionsFragmentToMenuFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
        return binding.root
    }

}

