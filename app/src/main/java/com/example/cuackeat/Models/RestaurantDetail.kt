package com.example.cuackeat.Models

data class RestaurantDetail(var id:Int? =  null,
                           var name:String? = null,
                           var description:String? = null,
                           var location:String? = null,
                           var image:String? = null,
                           var favorite_restaurants_count: Int? = 0,
                           var is_favorite:Boolean? = false){}


data class ResponseRestaurantDetail(val status: String?,
                                   val data: RestaurantDetail){}