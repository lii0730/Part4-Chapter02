package com.example.aop_part4_chapter02.service

data class PlayListItem(
    // Retrofit Service 로 받아오는 값
    val streamUrl : String,
    val artist : String,
    val cover : String,
    val track : String
)