package com.example.cuackeat.Models

data class FavoriteRestaurant(var id:Int? =  null,
                             var user_id:Int? = null,
                             var restaurant_id:Int? = null,
                             var restaurant:RestaurantData? = null){}

data class RestaurantData(var id:Int? =  null,
                         var name:String? = null,
                         var description:String? = null,
                         var location:String? = null,
                         var image:List<String>? = null){}


data class ResponseFavoriteRestaurant(val status: String?,
                                     val data: List<FavoriteRestaurant>){}