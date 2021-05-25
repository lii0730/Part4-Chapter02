package com.example.aop_part4_chapter02

import com.example.aop_part4_chapter02.service.MusicDTO
import com.example.aop_part4_chapter02.service.PlayListItem

fun PlayListItem.mapper(id: Long): MusicModel =
    MusicModel(
        id = id,
        streamUrl = streamUrl,
        cover = cover,
        track = track,
        artist = artist
    )

fun MusicDTO.mapper(): PlayerModel = PlayerModel(
    playMusicList = musics.mapIndexed { index, playListItem ->
        playListItem.mapper(index.toLong())
    }
)