package com.example.aop_part4_chapter02

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
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
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private lateinit var musicAdapter: PlayListAdapter
    private var binding: FragmentPlayerBinding? = null
    private var model: PlayerModel = PlayerModel()
    private lateinit var player: SimpleExoPlayer

    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayView(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)
        initPlayListButton(fragmentPlayerBinding)
        initSeekBar(fragmentPlayerBinding)
        initPlayBottomControlButton(fragmentPlayerBinding)
        getVideoList()

    }

    override fun onStop() {
        super.onStop()
        player.pause()
        view?.removeCallbacks(updateSeekRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        player.release()
        view?.removeCallbacks(updateSeekRunnable)
    }

    private fun initSeekBar(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player.seekTo((seekBar.progress * 1000).toLong())
            }
        })
        fragmentPlayerBinding.playListSeekBar.setOnTouchListener { v, event ->
            false
        }
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

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    val newIndex = mediaItem?.mediaId ?: return
                    model.currentPosition = newIndex.toInt()
                    updatePlyerView(model.currentMusicModel())

                    musicAdapter.submitList(model.getAdapterModels())
                }

                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)

                    updateSeek()
                }
            })
        }
    }

    private fun updateSeek() {
        val player = this.player

        // duration = 재생중인 아이템의 전체 길이
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

        updateSeekUI(duration, position)

        val state = player.playbackState
        view?.removeCallbacks(updateSeekRunnable)
        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            view?.postDelayed(updateSeekRunnable, 1000)
        }
    }

    private fun updateSeekUI(duration: Long, position: Long) {
        binding?.let { binding ->
            binding.playListSeekBar.max = (duration / 1000).toInt()
            binding.playListSeekBar.progress = (position / 1000).toInt()
            binding.playerSeekBar.max = (duration / 1000).toInt()
            binding.playerSeekBar.progress = (position / 1000).toInt()

            binding.playTimeTextView.text = String.format(
                "%02d:%02d",
                TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),
                (position / 1000) % 60
            )
            binding.totalTimeTextView.text = String.format(
                "%02d:%02d",
                TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),
                (duration / 1000) % 60
            )
        }
    }

    private fun updatePlyerView(currentMusicModel: MusicModel?) {
        currentMusicModel ?: return

        binding?.let { binding ->
            binding.titleTextView.text = currentMusicModel.track
            binding.artistTextView.text = currentMusicModel.artist
            Glide.with(binding.coverImageView)
                .load(currentMusicModel.cover)
                .into(binding.coverImageView)
        }
    }

    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {

        try {
            musicAdapter = PlayListAdapter { item ->
                playMusic(item)
            }

            fragmentPlayerBinding.playListRecyclerView.apply {
                adapter = musicAdapter
                layoutManager = LinearLayoutManager(context)
            }
        } catch (e: Exception) {
            Log.i("Error", e.toString())
        }
    }

    private fun initPlayListButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playListImageView.setOnClickListener {

            if (model.currentPosition == -1) {
                return@setOnClickListener
            }

            fragmentPlayerBinding.playListViewGroup.isVisible = !model.isWatchingPlayingListView
            fragmentPlayerBinding.playerViewGroup.isVisible = model.isWatchingPlayingListView

            model.isWatchingPlayingListView = !model.isWatchingPlayingListView
        }
    }

    private fun initPlayBottomControlButton(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playControlImageView.setOnClickListener {
            if (fragmentPlayerBinding.playerView.player?.isPlaying!!) {
                fragmentPlayerBinding.playerView.player?.pause()
            } else {
                fragmentPlayerBinding.playerView.player?.play()
            }
        }

        fragmentPlayerBinding.skipNextImageView.setOnClickListener {
            val nextMusic = model.nextMusic() ?: return@setOnClickListener
            playMusic(nextMusic)
//            musicAdapter.currentList.forEachIndexed { index, musicModel ->
//                val tmp: MediaItem = MediaItem.fromUri(musicModel.streamUrl)
//                if (tmp.equals(fragmentPlayerBinding.playerView.player?.currentMediaItem)) {
//                    val model: MusicModel = musicAdapter.currentList.get(index + 1)
//                    fragmentPlayerBinding.playerView.player?.setMediaItem(MediaItem.fromUri(model.streamUrl))
//                    fragmentPlayerBinding.playerView.player?.prepare()
//                    fragmentPlayerBinding.playerView.player?.play()
//
//                    fragmentPlayerBinding.titleTextView.text = model.track
//                    fragmentPlayerBinding.artistTextView.text = model.artist
//                    Glide.with(fragmentPlayerBinding.coverImageView)
//                        .load(model.cover)
//                        .into(fragmentPlayerBinding.coverImageView)
//                    return@setOnClickListener
//                }
//            }
        }

        fragmentPlayerBinding.skipPrevImageView.setOnClickListener {
            val prevMusic = model.prevMusic() ?: return@setOnClickListener
            playMusic(prevMusic)
//            musicAdapter.currentList.forEachIndexed { index, musicModel ->
//                val tmp: MediaItem = MediaItem.fromUri(musicModel.streamUrl)
//                if (tmp.equals(fragmentPlayerBinding.playerView.player?.currentMediaItem)) {
//                    val model: MusicModel = musicAdapter.currentList.get(index - 1)
//                    fragmentPlayerBinding.playerView.player?.setMediaItem(MediaItem.fromUri(model.streamUrl))
//                    fragmentPlayerBinding.playerView.player?.prepare()
//                    fragmentPlayerBinding.playerView.player?.play()
//
//                    fragmentPlayerBinding.titleTextView.text = model.track
//                    fragmentPlayerBinding.artistTextView.text = model.artist
//                    Glide.with(fragmentPlayerBinding.coverImageView)
//                        .load(model.cover)
//                        .into(fragmentPlayerBinding.coverImageView)
//                    return@setOnClickListener
//                }
//            }
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
                    response.body()?.let { musicDto ->
                        model = musicDto.mapper()
                        setMusicList(model.getAdapterModels())
                        musicAdapter.submitList(model.getAdapterModels())
                    }
                }

                override fun onFailure(call: Call<MusicDTO>, t: Throwable) {

                }
            })
        }
    }

    private fun playMusic(item: MusicModel) {
        //TODO: Player UI Binding
        model.updateCurrentPosition(item)
        player.seekTo(model.currentPosition, 0)
        player.play()
    }

    private fun setMusicList(modelList: List<MusicModel>) {
        context?.let {
            player?.addMediaItems(modelList.map { musicModel ->
                MediaItem.Builder()
                    .setMediaId(musicModel.id.toString())
                    .setUri(musicModel.streamUrl)
                    .build()
            })
            player?.prepare()
        }
    }

    companion object {
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}