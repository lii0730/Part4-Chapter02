package com.example.aop_part4_chapter02

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.aop_part4_chapter02.databinding.FragmentPlayerBinding

class PlayerFragment : Fragment(R.layout.fragment_player){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)

    }

    companion object {
        fun newInstance() : PlayerFragment {
            return PlayerFragment()
        }
    }
}