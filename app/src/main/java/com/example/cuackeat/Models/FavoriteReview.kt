package com.example.cuackeat.Models


import com.example.cuackeat.Data.Restaurant

data class FavoriteReview(var id:Int? =  null,
                          var user_id:Int? = null,
                          var review_id:Int? = null,
                          var review:ReviewData? = null){}


data class ReviewData(var id:Int? =  null,
                      var title:String? = null,
                      var description:String? = null,
                      var user_id:Int? =  null,
                      var restaurant_id:Int? =  null,
                      var images:List<ReviewImage>? =  null,
                      var user:User? =  null,
                      var restaurant:RestaurantReviewData? =  null){}

data class RestaurantReviewData(var id:Int? =  null,
                               var name:String? = null,
                               var description:String? = null,
                               var location:String? = null,
                               var image:String? = null){}

data class ResponseFavoriteReview(val status: String?,
                                  val data: List<FavoriteReview>){}