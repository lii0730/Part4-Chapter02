package com.example.aop_part4_chapter02

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aop_part4_chapter02.databinding.FragmentPlayerBinding
import com.example.aop_part4_chapter02.service.MusicDTO
import com.example.aop_part4_chapter02.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player){

    private lateinit var musicAdapter : PlayListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)

        musicAdapter = PlayListAdapter()
        fragmentPlayerBinding.playListRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context!!)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MusicService::class.java).also {
            it.loadMusics().enqueue(object : Callback<MusicDTO>{
                override fun onResponse(call: Call<MusicDTO>, response: Response<MusicDTO>) {
                    if(!response.isSuccessful){
                        return
                    }
                    response.body()?.let {
                        Log.i("PlayerFragment", it.toString())
                        musicAdapter.submitList(it.musics)
                    }
                }

                override fun onFailure(call: Call<MusicDTO>, t: Throwable) {

                }
            })
        }

    }

    companion object {
        fun newInstance() : PlayerFragment {
            return PlayerFragment()
        }
    }
}