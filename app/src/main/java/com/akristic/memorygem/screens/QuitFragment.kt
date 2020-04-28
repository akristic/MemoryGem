package com.akristic.memorygem.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.akristic.memorygem.R
import com.akristic.memorygem.databinding.QuitFragmentBinding



class QuitFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: QuitFragmentBinding = DataBindingUtil.inflate(
            inflater, R.layout.quit_fragment, container, false
        )
        binding.quitNoButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_quitFragment_to_menuFragment))
        binding.quitYesButton.setOnClickListener { activity?.finish() }
        return binding.root
    }

}
