package com.example.cuackeat.Models

import java.util.*

class ReviewImagesModel (
    var review_image_id: Int=getAutoId(),
    var image_path: String="",
    var review_id: Int=0
    ) {
        companion object{
            fun getAutoId():Int{
                val random= Random()
                return random.nextInt(100000)
            }
        }
}