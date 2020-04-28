package com.akristic.memorygem.screens


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.akristic.memorygem.R
import com.akristic.memorygem.databinding.FragmentHelpBinding

/**
 * A simple [Fragment] subclass.
 *
 */
class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHelpBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_help, container, false
        )
        binding.howToPlayOkButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_helpFragment_to_menuFragment))
        return binding.root
    }


}
