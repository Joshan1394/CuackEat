package com.example.cuackeat
import com.example.cuackeat.Models.*
import retrofit2.Call
import retrofit2.http.*

//Retrofit usa una interface para hacer la petición hacia el servidor
interface Service{

    // Login
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun login(@Body userData: User):Call<ResponseUser>

    // Register
    @Headers("Content-Type: application/json")
    @POST("auth/register")
    fun register(@Body userData: User):Call<ResponseUser>



    // Edit Profile
    @GET("user/{id}")
    fun getUser(@Path("id") id: Int): Call<ResponseUser>

    @Headers("Content-Type: application/json")
    @PUT("user/edit-information/{id}")
    fun updateUserInfo(@Path("id") id: Int, @Body userData: User): Call<ResponseUser>

    @Headers("Content-Type: application/json")
    @PUT("user/edit-password/{id}")
    fun updateUserPassword(@Path("id") id: Int, @Body userData: UserPassword): Call<ResponseUser>



    // Create Review
    @Headers("Content-Type: application/json")
    @POST("reviews/create")
    fun createReview(@Body reviewData: Review): Call<ResponseReview>

    // Delete Review
    @Headers("Content-Type: application/json")
    @DELETE("reviews/{review_id}/user/{user_id}/delete")
    fun deleteReview(@Path("review_id") reviewId: Int, @Path("user_id") userId: Int): Call<ResponseReview>

    // Edit Review
    @Headers("Content-Type: application/json")
    @GET("reviews/{id}/user/{userId}")
    fun getReviewDetails(@Path("id") reviewId: Int, @Path("userId") userId: Int): Call<ResponseReviewDetail>

    @Headers("Content-Type: application/json")
    @PUT("reviews/update/{id}")
    fun updateReview(@Path("id") id: Int, @Body reviewData: Review): Call<ResponseReview>



    // Get User Favorite Restaurants
    @Headers("Content-Type: application/json")
    @GET("restaurant/user-favorite/{id}")
    fun getUserFavoriteRestaurant(@Path("id") id: Int): Call<ResponseFavoriteRestaurant>

    // Get Restaurant Details
    @Headers("Content-Type: application/json")
    @GET("restaurant/{id}/user/{userId}")
    fun getRestaurantDetails(@Path("id") id: Int, @Path("userId") userId: Int): Call<ResponseRestaurantDetail>

    // Add Restaurant to Favorites
    @Headers("Content-Type: application/json")
    @POST("restaurant/favorite/add")
    fun addRestaurantToFavorites(@Body data: FavoriteRestaurant): Call<ResponseFavoriteRestaurant>

    // Remove Restaurant from Favorites
    @Headers("Content-Type: application/json")
    @DELETE("restaurant/{restaurant_id}/user/{user_id}/favorite/delete")
    fun removeRestaurantFromFavorites(@Path("restaurant_id") restaurantId: Int, @Path("user_id") userId: Int): Call<ResponseFavoriteRestaurant>



    // Get User Favorite Reviews
    @Headers("Content-Type: application/json")
    @GET("reviews/user-favorite/{id}")
    fun getUserFavoriteReviews(@Path("id") id: Int): Call<ResponseFavoriteReview>

    // Add Review to Favorites
    @Headers("Content-Type: application/json")
    @POST("reviews/favorite/add")
    fun addReviewToFavorites(@Body data: FavoriteReview): Call<ResponseFavoriteReview>

    // Remove Restaurant from Favorites
    @Headers("Content-Type: application/json")
    @DELETE("reviews/{review_id}/user/{user_id}/favorite/delete")
    fun removeReviewFromFavorites(@Path("review_id") reviewId: Int, @Path("user_id") userId: Int): Call<ResponseFavoriteReview>



    // Get Restaurants ordered by Date DESC
    @Headers("Content-Type: application/json")
    @GET("restaurants/order-by-date-desc")
    fun getRestaurantsOrderedByDateDesc(): Call<ResponseListRestaurant>

    // Get Restaurants ordered by Date ASC
    @Headers("Content-Type: application/json")
    @GET("restaurants/order-by-date-asc")
    fun getRestaurantsOrderedByDateAsc(): Call<ResponseListRestaurant>

    // Get Restaurants ordered by Id DESC
    @Headers("Content-Type: application/json")
    @GET("restaurants/order-by-id-desc")
    fun getRestaurantsOrderedByIdDesc(): Call<ResponseListRestaurant>

    // Get Restaurants ordered by Id ASC
    @Headers("Content-Type: application/json")
    @GET("restaurants/order-by-id-asc")
    fun getRestaurantsOrderedByIdAsc(): Call<ResponseListRestaurant>



    // Get Reviews ordered by Date DESC
    @Headers("Content-Type: application/json")
    @GET("reviews/order-by-date-desc")
    fun getReviewsOrderedByDateDesc(): Call<ResponseListReviews>

    // Get Reviews ordered by Date ASC
    @Headers("Content-Type: application/json")
    @GET("reviews/order-by-date-asc")
    fun getReviewsOrderedByDateAsc(): Call<ResponseListReviews>

    // Get Reviews ordered by Id DESC
    @Headers("Content-Type: application/json")
    @GET("reviews/order-by-id-desc")
    fun getReviewsOrderedByIdDesc(): Call<ResponseListReviews>

    // Get Reviews ordered by Id ASC
    @Headers("Content-Type: application/json")
    @GET("reviews/order-by-id-asc")
    fun getReviewsOrderedByIdAsc(): Call<ResponseListReviews>

}