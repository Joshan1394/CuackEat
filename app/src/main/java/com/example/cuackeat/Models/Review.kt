package com.example.cuackeat.Models


data class Review(var id:Int? =  null,
                  var title:String? = null,
                  var description:String? = null,
                  var user_id:Int? =  null,
                  var restaurant_id:Int? =  null,
                  var images:List<String>? = null){}


data class ResponseReview(val status: String?,
                          val data: Review,
                          val message: Map<String, List<String>>?){}