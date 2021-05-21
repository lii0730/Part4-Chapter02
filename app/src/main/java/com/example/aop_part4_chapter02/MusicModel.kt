package com.example.aop_part4_chapter02

data class MusicModel(
    val id : Long,
    val track : String,
    val artist : String,
    val streamUrl : String,
    val cover : String,
    val isPlaying : Boolean = false
)
