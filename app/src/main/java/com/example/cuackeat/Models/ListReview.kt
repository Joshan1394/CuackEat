package com.example.cuackeat.Models


data class ListReviewsData(var id:Int? =  null,
                           var title:String? = null,
                           var description:String? = null,
                           var user_id:Int? =  null,
                           var restaurant_id:Int? =  null,
                           var images:List<ReviewImage>? =  null,
                           var user:User? =  null,
                           var restaurant:ListReviewsRestaurantData? =  null){}

data class ListReviewsRestaurantData(var id:Int? =  null,
                                    var name:String? = null,
                                    var description:String? = null,
                                    var image:List<String>? = null){}

data class ResponseListReviews(val status: String?,
                               val data: List<ListReviewsData>){}