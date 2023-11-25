package com.example.cuackeat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.Restaurant
import com.example.cuackeat.Models.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.util.*

class DiscoverFragment : Fragment(), View.OnClickListener, SearchView.OnQueryTextListener {

    private var restaurants = ArrayList<Restaurant>()
    lateinit var rcListAlbum : RecyclerView
    lateinit var albumAdapter : ProfileRestaurantRecyclerAdapter
    private var listener: OnFragmentActionsListener? = null
    private lateinit var myContext: Context
    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper
    private var restaurantInserted=false

    private var isAdapterInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(myContext)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.activity_main_search, container, false)

        // Limpiar la lista de Restaurantes
        restaurants.clear()

        // Obtener la referencia del RecyclerView
        rcListAlbum = root.findViewById(R.id.rcListAlbum)
        rcListAlbum.layoutManager = LinearLayoutManager(this.myContext)

        //Spinner
        val orderSpinner: Spinner = root.findViewById(R.id.spinnerOrderBy)
        val orderOptions = arrayOf("Fecha ascendente", "Fecha descendente", "ID ascendente", "ID descendente")
        val adapter = ArrayAdapter(myContext, android.R.layout.simple_spinner_dropdown_item, orderOptions)
        orderSpinner.adapter = adapter

        getRestaurantOrderedByParam("Fecha ascendente")

        //SearchView
        val vwSearchAlbum = root.findViewById<SearchView>(R.id.vwSearchAlbum)
        vwSearchAlbum.setOnQueryTextListener(this)

        // Eventos spinner
        orderSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getRestaurantOrderedByParam("Fecha ascendente")
                        }
                    }
                    1 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getRestaurantOrderedByParam("Fecha descendente")
                        }
                    }
                    2 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getRestaurantOrderedByParam("ID ascendente")
                        }
                    }
                    3 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getRestaurantOrderedByParam("ID descendente")
                        }
                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

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
    }

    private fun addRestaurantSQLite(id:Int, name:String, description:String, location:String, imagen:String) {
        val std = RestaurantModel(id=id, name=name, description = description, location = location, image = imagen)
        val status = sqliteHelper.insertRestaurant(std)
        //Check insert success or not success
        if (status>-1)
        {
            //Toast.makeText(myContext, "Restaurant Added", Toast.LENGTH_SHORT).show()
            getRestaurant()
        }else
        {
            Toast.makeText(myContext, "No se ha guardado en SQLITE", Toast.LENGTH_SHORT).show()
        }

    }

    /*Funcion del botón para obtener a los estudiantes*/
    private fun getRestaurant(): ArrayList<RestaurantModel> {
        val stdList = sqliteHelper.getAllRestaurants()
        Log.e("pppp", "${stdList.size}")

        return stdList
    }

    private fun getRestaurantOrderedByParam(param: String) {
        try {

            // Dependiendo del tipo de ordenamiento, mandamos a llamar una petición u otra
            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseListRestaurant> = when (param) {
                "Fecha ascendente" -> service.getRestaurantsOrderedByDateAsc()
                "Fecha descendente" -> service.getRestaurantsOrderedByDateDesc()
                "ID ascendente" -> service.getRestaurantsOrderedByIdAsc()
                "ID descendente" -> service.getRestaurantsOrderedByIdDesc()
                else -> service.getRestaurantsOrderedByDateDesc()
            }

            result.enqueue(object: Callback<ResponseListRestaurant> {
                override fun onFailure(call: Call<ResponseListRestaurant>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(myContext,"Ocurrió un error al intentar obtener los videojuegos. Favor de conectarse a internet e inténtalo de nuevo.",
                        Toast.LENGTH_LONG).show()

                    if(!restaurantInserted)
                    {
                        //Muestra lo último que fué guardado en la base de datos local
                        val stdList= getRestaurant()
                        for(restaurantItem in stdList){
                            val restaurant = Restaurant()
                            restaurant.id = restaurantItem?.id
                            restaurant.strTitle = restaurantItem?.name.toString()
                            restaurant.strDescription = restaurantItem?.description.toString()
                            val strImage:String =  restaurantItem?.image!!.replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    restaurant.imgArray = byteArray
                                }
                            } catch (e: IllegalArgumentException) {
                                Log.e("catchImage", e.toString())
                                Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG)
                            }
                            restaurants.add(restaurant)

                        }
                        // Agregar el Adapter al RecyclerView
                        albumAdapter = ProfileRestaurantRecyclerAdapter(myContext, restaurants)
                        rcListAlbum.adapter = albumAdapter

                        // Bandera para indicar que ha sido inicializado
                        isAdapterInitialized = true
                        restaurantInserted=true
                    }
                }

                override fun onResponse(call: Call<ResponseListRestaurant>, response: Response<ResponseListRestaurant>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(myContext,"Ocurrió un error al intentar obtener los restaurantes, por favor inténtalo de nuevo.",
                                Toast.LENGTH_LONG).show()
                        }

                    } else {
                        val responseBody = response.body()
                        val restaurantsList = responseBody?.data
                        val status = responseBody?.status

                        if (status == "success"){
                            //Clear a la base de datos local
                            sqliteHelper.truncateTableRestaurants()
                            // Agregar los restaurantes al ArrayList restaurants
                            if (restaurantsList != null) {

                                // Limpiar la lista de reviews
                                restaurants.clear()

                                for(restaurantItem in restaurantsList){
                                    val restaurant = Restaurant()
                                    restaurant.id = restaurantItem?.id
                                    restaurant.strTitle = restaurantItem?.name.toString()
                                    restaurant.strDescription = restaurantItem?.description.toString()
                                    val strImage:String =  restaurantItem?.image!!.replace("data:image/png;base64,","")
                                    try {
                                        val byteArray = Base64.getDecoder().decode(strImage)
                                        if(byteArray != null){
                                            restaurant.imgArray = byteArray
                                        }
                                    } catch (e: IllegalArgumentException) {
                                        Log.e("catchImage", e.toString())
                                        Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG)
                                    }
                                    restaurants.add(restaurant)
                                    //Agregar en la base de datos de usuario
                                    addRestaurantSQLite(restaurantItem?.id!!.toInt(), restaurantItem?.name.toString(), restaurantItem?.description.toString(), restaurantItem?.location.toString(), restaurantItem?.image.toString())

                                }
                                // Agregar el Adapter al RecyclerView
                                albumAdapter = ProfileRestaurantRecyclerAdapter(myContext, restaurants)
                                rcListAlbum.adapter = albumAdapter

                                // Bandera para indicar que ha sido inicializado
                                isAdapterInitialized = true
                            }

                        }
                    }
                }

            })

        } catch (e: Exception) {
            Log.e("error", e.toString())
            Toast.makeText(myContext, "Ocurrió un error al intentar obtener los videojuegos. Por favor, inténtelo de nuevo.", Toast.LENGTH_LONG)
        }
    }

    //Search View
    override fun onQueryTextSubmit(query: String?): Boolean {

        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {

        if (newText != null && isAdapterInitialized){
            if(albumAdapter != null) this.albumAdapter?.filter?.filter(newText)
        }
        return false
    }

}