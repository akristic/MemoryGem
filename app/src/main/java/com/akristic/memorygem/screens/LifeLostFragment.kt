package com.akristic.memorygem.screens


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.akristic.memorygem.R
import com.akristic.memorygem.databinding.FragmentLifeLostBinding
import com.akristic.memorygem.screens.menu.MenuFragmentDirections


/**
 * A simple [Fragment] subclass.
 *
 */
class LifeLostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLifeLostBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_life_lost, container, false
        )
        binding.lifeLostTryAgainButton.setOnClickListener {
            val action = LifeLostFragmentDirections.actionLifeLostFragmentToGameFragment()
            action.setCurrentGamePlaying(true)
            NavHostFragment.findNavController(this).navigate(action)
        }
        return binding.root
    }
}
