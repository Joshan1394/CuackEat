package com.example.cuackeat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ReviewObject
import com.example.cuackeat.Models.*
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileReviewsFragment : Fragment(), View.OnClickListener {

    private var reviews = ArrayList<ReviewObject>()
    lateinit var rcListAlbum : RecyclerView
    lateinit var albumAdapter : ProfileReviewsRecyclerAdapter
    private var listener: OnFragmentActionsListener? = null
    private lateinit var myContext: Context

    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(myContext)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_profile_favorite_reviews, container, false)

        reviews.clear()

        rcListAlbum = root.findViewById(R.id.rcListFavoriteReviews_Profile)
        rcListAlbum.layoutManager = LinearLayoutManager(this.myContext)

        getUserFavoriteReviews()

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Obtener el contexto para aplicarlo en el LinearLayoutManager y el AlbumRecyclerAdapter
        this.myContext = context

        if(context is OnFragmentActionsListener){
            this.listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

    // Declaramos el evento onClick, mismo que aparece en la interfaz OnFragmentActionsListener
    // e implementamos en la clase HomeActivity
    override fun onClick(v: View?){
        when(v!!.id){

        }
    }

    private fun addFavReviewsSQLite(id:Int, title:String, description:String, imagen:String, user_id:Int, nickname:String, restaurant_Id:Int,restaurant_name:String,) {
        val std = ReviewProfileModel(id=id, title=title, description = description, images = imagen, user_id=user_id, user_nickname =nickname , restaurant_id = restaurant_Id, restaurant_name = restaurant_name)
        val status = sqliteHelper.insertReviewsProfile(std)
        //Check insert success or not success
        if (status>-1)
        {
            //Toast.makeText(myContext, "Review Added", Toast.LENGTH_SHORT).show()
            getFavReviews()
        }else
        {
            Toast.makeText(myContext, "No se ha guardado en SQLITE", Toast.LENGTH_SHORT).show()
        }

    }

    /*Funcion del botón para obtener a los estudiantes*/
    private fun getFavReviews():ArrayList<ReviewProfileModel> {
        val stdList = sqliteHelper.getAllReviewsProfile()
        Log.e("pppp", "${stdList.size}")

        return stdList
    }

    private fun agregarOffline(){
        val favoriteReviews= getFavReviews()
        if (favoriteReviews != null) {
            for(favoriteReview in favoriteReviews){

                val review = ReviewObject()
                review.id = favoriteReview.id
                review.reviewTitle = favoriteReview.title
                review.reviewDescription = favoriteReview.description

                // Obtener las imagenes de la review    val reviewImageList = ArrayList<ByteArray>()
                if( favoriteReview?.images!=""){
                    /* favoriteReview?.images.forEach { image ->

                        val strImage = image.image!!.replace("data:image/png;base64,","")
                        try {
                            val byteArray = Base64.getDecoder().decode(strImage)
                            if (byteArray != null) {
                                reviewImageList.add(byteArray)
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e("catchImage", e.toString())
                            Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                        }

                    } */
                }
                // Agregar la lista de imagenes al objeto review.reviewImgArray = reviewImageList

                // Agregar los datos del User y Videogame
                review.user_id = favoriteReview.user_id
                review.userNickname = favoriteReview.user_nickname
                review.restaurant_id = favoriteReview.restaurant_id
                review.restaurantTitle = favoriteReview.restaurant_name

                // Agregar la review a la lista
                reviews.add(review)

            }
            albumAdapter = ProfileReviewsRecyclerAdapter(myContext, reviews)
            rcListAlbum.adapter = albumAdapter
        }
    }

    private fun getUserFavoriteReviews() {
        try {

            val userId = UserApplication.prefs.getCredentials().id
            val intUserId = userId.toNonNegativeInt(0)
            if(intUserId == 0){
                Toast.makeText(myContext, "Ocurrió un error al intentar obtener las reviews favoritas. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
            }

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteReview> = service.getUserFavoriteReviews(intUserId)

            result.enqueue(object: Callback<ResponseFavoriteReview> {
                override fun onFailure(call: Call<ResponseFavoriteReview>, t: Throwable) {
                    Log.e("FAILED ResponseFavoriteReview", t.toString())
                    Toast.makeText(myContext,"Ocurrió un error al intentar obtener las reviews favoritas. Por favor, inténtalo de nuevo.",
                        Toast.LENGTH_LONG).show()

                    agregarOffline()
                }

                override fun onResponse(call: Call<ResponseFavoriteReview>, response: Response<ResponseFavoriteReview>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(myContext,"Ocurrió un error al intentar obtener las reviews favoritas. Por favor, inténtalo de nuevo.",
                                Toast.LENGTH_LONG).show()
                        }

                    } else {
                        val responseBody = response.body()
                        val favoriteReviews = responseBody?.data
                        val status = responseBody?.status

                        if (status == "success"){

                            sqliteHelper.truncateTableReviewsProfile() //Clear a la base de datos local

                            if (favoriteReviews != null) {
                                for(favoriteReview in favoriteReviews){

                                    val review = ReviewObject()
                                    review.id = favoriteReview.review?.id
                                    review.reviewTitle = favoriteReview.review?.title
                                    review.reviewDescription = favoriteReview.review?.description

                                    // Obtener las imagenes de la review
                                    val reviewImageList = ArrayList<ByteArray>()
                                    if( favoriteReview?.review?.images?.size!! > 0){
                                        favoriteReview?.review?.images?.forEach { image ->
                                            val strImage = image.image!!.replace("data:image/png;base64,","")
                                            try {
                                                val byteArray = Base64.getDecoder().decode(strImage)
                                                if (byteArray != null) {
                                                    reviewImageList.add(byteArray)
                                                }
                                            } catch (e: IllegalArgumentException) {
                                                Log.e("catchImage", e.toString())
                                                Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    // Agregar la lista de imagenes al objeto
                                    review.reviewImgArray = reviewImageList

                                    // Agregar los datos del User y Videogame
                                    review.user_id = favoriteReview.review?.user_id
                                    review.userNickname = favoriteReview.review?.user?.nickname
                                    review.restaurant_id = favoriteReview.review?.restaurant_id
                                    review.restaurantTitle = favoriteReview.review?.restaurant?.name

                                    // Agregar la review a la lista
                                    reviews.add(review)

                                    val imagen= if (( favoriteReview?.review?.images?.size!! > 0)) reviewImageList[0].toString() else ""

                                    //Agregat a SQLite
                                    addFavReviewsSQLite(favoriteReview.review?.id!!.toInt(), favoriteReview.review?.title.toString(), favoriteReview.review?.description.toString(), imagen, favoriteReview.review?.user_id!!.toInt(), review.userNickname.toString(), favoriteReview.review?.restaurant_id!!.toInt(), favoriteReview.review?.restaurant?.name.toString())
                                }
                                albumAdapter = ProfileReviewsRecyclerAdapter(myContext, reviews)
                                rcListAlbum.adapter = albumAdapter
                            }

                        }
                    }
                }

            })

        } catch (e: Exception) {
            Log.e("error", e.toString())
            Toast.makeText(myContext, "Ocurrió un error al intentar obtener las reviews favoritas. Por favor, inténtelo de nuevo.", Toast.LENGTH_LONG)
        }
    }

}