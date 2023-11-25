package com.example.cuackeat.Data


//Las variables se declaran como opcionales.
//Esto permite crear un objecto de album por defecto vacio
//que sea usa cuando vamos agregar un nuevo album
class ReviewObject(var id:Int? = null, var reviewTitle:String? = null, var reviewDescription:String? = null, var reviewLocation:String? = null, var reviewImgArray:List<ByteArray>? = null,
                   var user_id:Int? = null, var userNickname:String? = null, var restaurant_id:Int? = null, var restaurantTitle:String? = null){}