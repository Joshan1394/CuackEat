package com.example.cuackeat.Models

import java.util.*

/*Modelo con el usuario para manejarlos más fácilmente*/
data class RestaurantModel (
    var id: Int= getAutoId(),
    var name: String="",
    var description: String="",
    var location: String="",
    var image: String=""
){

    companion object{
        fun getAutoId():Int{
            val random= Random()
            return random.nextInt(100)
        }
    }
}
