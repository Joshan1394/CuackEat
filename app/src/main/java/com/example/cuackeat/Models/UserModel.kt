package com.example.cuackeat.Models

import java.util.*

/*Modelo con el usuario para manejarlos más fácilmente*/
data class UserModel (
    var id: Int= getAutoId(),
    var email: String="",
    var password: String="",
    var name: String="",
    var lastName: String="",
    var phone: String="",
    var address: String="",
    var alias: String="",
    var description: String="",
    var image: String=""
){

    companion object{
        fun getAutoId():Int{
            val random= Random()
            return random.nextInt(100)
        }
    }
}