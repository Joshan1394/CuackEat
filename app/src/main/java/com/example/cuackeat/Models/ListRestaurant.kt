package com.example.cuackeat.Models

data class ListRestaurantData(var id:Int? =  null,
                              var name:String? = null,
                              var description:String? = null,
                              var location:String? = null,
                              var image:String? = null){}

data class ResponseListRestaurant(val status: String?,
                                  val data: List<ListRestaurantData>){}