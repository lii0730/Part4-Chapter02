package com.example.aop_part4_chapter02

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.aop_part4_chapter02.databinding.FragmentPlayerBinding
import com.example.aop_part4_chapter02.service.MusicDTO
import com.example.aop_part4_chapter02.service.MusicService
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private lateinit var musicAdapter: PlayListAdapter
    private var binding: FragmentPlayerBinding? = null
    private var isWatchingPlayingListView = true
    private lateinit var player: SimpleExoPlayer
    private var playList = listOf<MusicModel>()
    private lateinit var tmpView : View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayView(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)
        initPlayListButton(fragmentPlayerBinding)
        initPlayBottomControlButton(fragmentPlayerBinding)
        getVideoList()

    }

    private fun initPlayView(fragmentPlayerBinding: FragmentPlayerBinding) {
        context?.let {
            player = SimpleExoPlayer.Builder(it).build()
        }
        fragmentPlayerBinding.playerView.player = player

        binding?.let { binding ->
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        binding.playControlImageView.setImageResource(R.drawable.ic_pause_48)
                    } else {
                        binding.playControlImageView.setImageResource(R.drawable.ic_play_arrow_48)
                    }
                }
            })
        }
    }

    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {
        musicAdapter = PlayListAdapter(onItemClicked = { item ->

            fragmentPlayerBinding.titleTextView.text = item.track
            fragmentPlayerBinding.artistTextView.text = item.artist
            Glide.with(fragmentPlayerBinding.coverImageView.context)
                .load(item.cover)
                .into(fragmentPlayerBinding.coverImageView)

            //TODO: 음악 재생 기능 추가 (현재 듣고 있는 음악일 경우엔 Pause 예외 처리)
            fragmentPlayerBinding.playerView.player?.setMediaItem(MediaItem.fromUri(item.streamUrl))
            fragmentPlayerBinding.playerView.player?.prepare()
            fragmentPlayerBinding.playerView.player?.play()
        })

        fragmentPlayerBinding.playListRecyclerView.apply {
            adapter = musicAdapter
            layoutManager = LinearLayoutManager(context!!)
        }
    }

    private fun initPlayListButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playListImageView.setOnClickListener {

            //TODO: 만약 서버에서 데이터가 다 불려오지 않은 상태일 때
            //  선택 음악이 없으면 전환x
            fragmentPlayerBinding.playListViewGroup.isVisible = !isWatchingPlayingListView
            fragmentPlayerBinding.playerViewGroup.isVisible = isWatchingPlayingListView
            isWatchingPlayingListView = !isWatchingPlayingListView
        }
    }

    private fun initPlayBottomControlButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playControlImageView.setOnClickListener {
            if(fragmentPlayerBinding.playerView.player?.isPlaying!!){
                fragmentPlayerBinding.playerView.player?.pause()
            } else {
                fragmentPlayerBinding.playerView.player?.play()
            }
        }

        fragmentPlayerBinding.skipNextImageView.setOnClickListener {
            musicAdapter.currentList.forEachIndexed { index, musicModel ->
                val tmp : MediaItem = MediaItem.fromUri(musicModel.streamUrl)
                if(tmp.equals(fragmentPlayerBinding.playerView.player?.currentMediaItem)){
                    val model : MusicModel = musicAdapter.currentList.get(index + 1)
                    fragmentPlayerBinding.playerView.player?.setMediaItem(MediaItem.fromUri(model.streamUrl))
                    fragmentPlayerBinding.playerView.player?.prepare()
                    fragmentPlayerBinding.playerView.player?.play()

                    fragmentPlayerBinding.titleTextView.text = model.track
                    fragmentPlayerBinding.artistTextView.text = model.artist
                    Glide.with(fragmentPlayerBinding.coverImageView)
                        .load(model.cover)
                        .into(fragmentPlayerBinding.coverImageView)
                    return@setOnClickListener
                }
            }
        }

        fragmentPlayerBinding.skipPrevImageView.setOnClickListener {
            musicAdapter.currentList.forEachIndexed { index, musicModel ->
                val tmp : MediaItem = MediaItem.fromUri(musicModel.streamUrl)
                if(tmp.equals(fragmentPlayerBinding.playerView.player?.currentMediaItem)) {
                    val model: MusicModel = musicAdapter.currentList.get(index - 1)
                    fragmentPlayerBinding.playerView.player?.setMediaItem(MediaItem.fromUri(model.streamUrl))
                    fragmentPlayerBinding.playerView.player?.prepare()
                    fragmentPlayerBinding.playerView.player?.play()

                    fragmentPlayerBinding.titleTextView.text = model.track
                    fragmentPlayerBinding.artistTextView.text = model.artist
                    Glide.with(fragmentPlayerBinding.coverImageView)
                        .load(model.cover)
                        .into(fragmentPlayerBinding.coverImageView)
                    return@setOnClickListener
                }
            }
        }
    }

    private fun getVideoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MusicService::class.java).also {
            it.loadMusics().enqueue(object : Callback<MusicDTO> {
                override fun onResponse(call: Call<MusicDTO>, response: Response<MusicDTO>) {
                    if (!response.isSuccessful) {
                        return
                    }
                    response.body()?.let {
                        val modelList = it.musics.mapIndexed { index, playListItem ->
                            playListItem.mapper(index.toLong())
                        }
                        playList = modelList
                        musicAdapter.submitList(modelList)
                    }
                }

                override fun onFailure(call: Call<MusicDTO>, t: Throwable) {

                }
            })
        }
    }

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}