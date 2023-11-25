package com.example.cuackeat.Models

import java.util.*
/*Modelo con el usuario para manejarlos más fácilmente*/
data class ReviewModel(
    var id: Int= getAutoId(),
    var title: String="",
    var description: String,
    var user_id: Int=0,
    var restaurant_id: Int=0,
){

    companion object{
        fun getAutoId():Int{
            val random= Random()
            return random.nextInt(100000)
        }
    }
}
