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
import com.example.cuackeat.Data.*
import com.example.cuackeat.Models.*
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileRestaurantsFragment : Fragment(), View.OnClickListener {

    private var restaurants = ArrayList<Restaurant>()
    lateinit var rcListAlbum : RecyclerView
    lateinit var albumAdapter : ProfileRestaurantRecyclerAdapter
    //private var albumAdapter:ProfileRestaurantRecyclerAdapter? = null
    private var listener: OnFragmentActionsListener? = null
    private lateinit var myContext: Context
    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper
    private var restaurantsInserted=false

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(myContext)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_profile_restaurant, container, false)

        restaurants.clear()

        rcListAlbum = root.findViewById(R.id.rcListRestaurants_Profile)
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

    private fun addRestaurantsSQLite(id:Int, name:String, description:String, imagen:String) {
        val std = RestaurantModel(id=id, name=name, description = description, image = imagen)
        val status = sqliteHelper.insertRestaurantsProfile(std)
        //Check insert success or not success
        if (status>-1)
        {
            //Toast.makeText(myContext, "Videogames Added", Toast.LENGTH_SHORT).show()
            getVideogames()
        }else
        {
            Toast.makeText(myContext, "No se ha guardado en SQLITE", Toast.LENGTH_SHORT).show()
        }

    }

    /*Funcion del botón para obtener a los estudiantes*/
    private fun getVideogames():ArrayList<RestaurantModel> {
        val stdList = sqliteHelper.getAllRestaurantsProfile()
        Log.e("pppp", "${stdList.size}")

        return stdList
    }

    private fun getUserFavoriteReviews() {
        try {

            val userId = UserApplication.prefs.getCredentials().id
            val intUserId = userId.toNonNegativeInt(0)
            if(intUserId == 0){
                Toast.makeText(myContext, "Ocurrió un error al intentar obtener los restaurantes. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT)
            }

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteRestaurant> = service.getUserFavoriteRestaurant(intUserId)

            result.enqueue(object: Callback<ResponseFavoriteRestaurant> {
                override fun onFailure(call: Call<ResponseFavoriteRestaurant>, t: Throwable) {
                    Log.e("FAILED ResponseFavoriteRestaurant", t.toString())
                    Toast.makeText(myContext,"Ocurrió un error al intentar obtener los restaurantes. Por favor, conectese a internet e inténtalo de nuevo.",Toast.LENGTH_SHORT).show()

                    if(!restaurantsInserted)
                    {
                        val stdList= getVideogames()
                        for(favoriteRestaurant in stdList){
                            val restaurant = Restaurant()
                            restaurant.id = favoriteRestaurant?.id
                            restaurant.strTitle = favoriteRestaurant?.name.toString()
                            restaurant.strDescription = favoriteRestaurant?.description.toString()
                            val strImage:String =  favoriteRestaurant?.image?.replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    restaurant.imgArray = byteArray
                                }
                            } catch (e: IllegalArgumentException) {
                                Log.e("catchImage", e.toString())
                                //Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_SHORT)
                            }
                            restaurants.add(restaurant)
                        }
                        albumAdapter = ProfileRestaurantRecyclerAdapter(myContext, restaurants)
                        rcListAlbum.adapter = albumAdapter
                    }
                }

                override fun onResponse(call: Call<ResponseFavoriteRestaurant>, response: Response<ResponseFavoriteRestaurant>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(myContext,"Ocurrió un error al intentar obtener los restaurantes. Por favor, inténtalo de nuevo.",Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        val responseBody = response.body()
                        val favoriteRestaurants = responseBody?.data
                        val status = responseBody?.status

                        if (status == "success"){

                            sqliteHelper.truncateTableRestaurantsProfile() //Clear a la base de datos local

                            if (favoriteRestaurants != null) {
                                for(favoriteRestaurant in favoriteRestaurants){
                                    val restaurant = Restaurant()
                                    restaurant.id = favoriteRestaurant.restaurant?.id
                                    restaurant.strTitle = favoriteRestaurant.restaurant?.name.toString()
                                    restaurant.strDescription = favoriteRestaurant.restaurant?.description.toString()
                                    val strImage:String =  favoriteRestaurant?.restaurant?.image?.get(0).toString().replace("data:image/png;base64,","")
                                    try {
                                        val byteArray = Base64.getDecoder().decode(strImage)
                                        if(byteArray != null){
                                            restaurant.imgArray = byteArray
                                        }
                                    } catch (e: IllegalArgumentException) {
                                        Log.e("catchImage", e.toString())
                                        //Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG)
                                    }

                                    restaurants.add(restaurant)

                                    //Agregar en la base de datos de usuario
                                    addRestaurantsSQLite(favoriteRestaurant.restaurant?.id!!.toInt(), favoriteRestaurant.restaurant?.name.toString(),
                                        favoriteRestaurant.restaurant?.description.toString(), favoriteRestaurant?.restaurant?.image?.get(0).toString())
                                }
                                albumAdapter = ProfileRestaurantRecyclerAdapter(myContext, restaurants)
                                rcListAlbum.adapter = albumAdapter
                            }

                        }
                    }
                }

            })

        } catch (e: Exception) {
            Log.e("error", e.toString())
            Toast.makeText(myContext, "Ocurrió un error al intentar obtener los videojuegos. Por favor, inténtelo de nuevo.", Toast.LENGTH_SHORT)
        }
    }

}