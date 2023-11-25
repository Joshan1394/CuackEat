package com.example.cuackeat.Models

data class ReviewDetail(var id:Int? =  null,
                        var title:String? = null,
                        var description:String? = null,
                        var user_id:Int? =  null,
                        var restaurant_id:Int? =  null,
                        var images:List<ReviewImage>? = null,
                        var restaurant: ReviewRestaurant? = null,
                        var user: User? = null,
                        var favorite_reviews_count: Int? = 0,
                        var is_favorite:Boolean? = false){}

data class ReviewImage(
    val id: Int? = null,
    val image: String? = null,
    val review_id: Int? = null
)

data class ReviewRestaurant(
    var id: Int? = null,
    var name: String? = null,
    var description: String? = null,
    var location: String? = null,
    var image:List<String>? = null,
)

data class ResponseReviewDetail(
    val status: String?,
    val data: ReviewDetail,
    val message: Map<String, List<String>>?
)