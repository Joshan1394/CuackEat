package com.example.cuackeat.Models

import java.util.*

data class ReviewProfileModel(
    var id: Int= getAutoId(),
    var title: String="",
    var description: String="",
    var images: String="",
    var user_id: Int=0,
    var user_nickname: String="",
    var restaurant_id: Int=0,
    var restaurant_name: String="",
) {

    companion object{
        fun getAutoId():Int{
            val random= Random()
            return random.nextInt(100)
        }
    }
}
