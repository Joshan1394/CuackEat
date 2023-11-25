package com.example.cuackeat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Data.ReviewObject
import com.example.cuackeat.Models.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class HomeFragment : Fragment(), View.OnClickListener, SearchView.OnQueryTextListener {

    private var reviews = ArrayList<ReviewObject>()
    lateinit var rcListAlbum : RecyclerView
    lateinit var albumAdapter : ProfileReviewsRecyclerAdapter
    private var listener: OnFragmentActionsListener? = null
    private lateinit var myContext: Context

    private var isAdapterInitialized: Boolean = false

    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(myContext)
        getBorradores() //Publica los borradores
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.activity_main_search, container, false)

        // Limpiamos la lista de las Reviews
        reviews.clear()

        // Obtenemos la referencia del RecyclerView
        rcListAlbum = root.findViewById(R.id.rcListAlbum)
        rcListAlbum.layoutManager = LinearLayoutManager(this.myContext)

        //Spinner
        val orderSpinner: Spinner = root.findViewById(R.id.spinnerOrderBy)
        val orderOptions = arrayOf("Fecha ascendente", "Fecha descendente", "ID ascendente", "ID descendente")
        val adapter = ArrayAdapter(myContext, android.R.layout.simple_spinner_dropdown_item, orderOptions)
        orderSpinner.adapter = adapter

        getReviewsOrderedByParam("Fecha ascendente")

        //SearchView
        val vwSearchAlbum = root.findViewById<SearchView>(R.id.vwSearchAlbum)
        vwSearchAlbum.setOnQueryTextListener(this)

        // Eventos spinner
        orderSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
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
                            getReviewsOrderedByParam("Fecha ascendente")
                        }
                    }
                    1 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getReviewsOrderedByParam("Fecha descendente")
                        }
                    }
                    2 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getReviewsOrderedByParam("ID ascendente")
                        }
                    }
                    3 -> {
                        if(isAdapterInitialized){
                            vwSearchAlbum.setQuery("", false)
                            getReviewsOrderedByParam("ID descendente")
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

    private fun addFavReviewsSQLite(id:Int, title:String, description:String, imagen:String, user_id:Int, nickname:String, restaurant_Id:Int,restaurant_name:String,) {
        val std = ReviewProfileModel(id=id, title=title, description = description, images = imagen, user_id=user_id, user_nickname =nickname , restaurant_id = restaurant_Id, restaurant_name = restaurant_name)
        val status = sqliteHelper.insertReviewsHome(std)
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
        val stdList = sqliteHelper.getAllReviewsHome()
        Log.e("pppp", "${stdList.size}")

        return stdList
    }

    /*Funcion del botón para obtener los borradores*/
    private fun getBorradores() {
        sqliteHelper.createTableBorrador()
        val stdList = sqliteHelper.getAllReviewsBorrador()
        Log.e("pppp", "${stdList.size}")

        var images  = ArrayList<String>()

        for(reviewItem in stdList){

            sqliteHelper.createTableReviewImages()
            images=sqliteHelper.getReviewImagesId(reviewItem.id)

            val review = Review(0, reviewItem.title, reviewItem.description, reviewItem.user_id, reviewItem.restaurant_id, images)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReview> = service.createReview(review)

            result.enqueue(object: Callback<ResponseReview> {

                override fun onFailure(call: Call<ResponseReview>, t: Throwable){
                    Log.e("Failure", t.toString())
                    Toast.makeText(myContext, "Los borradores serán cargados una vez se establezca la conexión ", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ResponseReview>, response: Response<ResponseReview>){

                    if (!response.isSuccessful) {
                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")
                        val messageJson = jsonObject.getJSONObject("message")

                        if (statusJson == "failed"){

                            val errorMessage = StringBuilder()
                            val keys = messageJson.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val messages = messageJson.getJSONArray(key)
                                for (i in 0 until messages.length()) {
                                    errorMessage.append(messages.getString(i)).append(" ")
                                }
                            }
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){
                            sqliteHelper.truncateTableReviewsBorrador(); //Vuelve a limpiar la lista
                            sqliteHelper.truncateTableReviewImages();
                            //Toast.makeText(myContext,"Reseña agregada con éxito.",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }

    }

    private fun agregarOffline(){
        val stdList = sqliteHelper.getAllReviewsHome()
        if (stdList != null) {

            // Limpiar la lista de reviews
            reviews.clear()

            for(reviewItem in stdList){

                val review = ReviewObject()
                review.id = reviewItem?.id
                review.reviewTitle = reviewItem?.title
                review.reviewDescription = reviewItem?.description

                // Obtener las imagenes de la review
                val reviewImageList = ArrayList<ByteArray>()
                if(reviewItem?.images!= ""){
                    reviewItem?.images?.forEach { image ->
                        val strImage = reviewItem?.images!!.replace("data:image/png;base64,","")
                        try {
                            val byteArray = Base64.getDecoder().decode(strImage)
                            if (byteArray != null) {
                                reviewImageList.add(byteArray)
                            }
                        } catch (e: IllegalArgumentException) {
                            Log.e("catchImage", e.toString())
                            //Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                // Agregar la lista de imagenes al objeto
                review.reviewImgArray = reviewImageList

                // Agregar los datos del User y Videogame
                review.user_id = reviewItem.user_id
                review.userNickname = reviewItem.user_nickname
                review.restaurant_id = reviewItem?.restaurant_id
                review.restaurantTitle = reviewItem?.restaurant_name

                // Agregar la review a la lista
                reviews.add(review)
            }

            // Asignar el Adapter al RecyclerView
            albumAdapter = ProfileReviewsRecyclerAdapter(myContext, reviews)
            rcListAlbum.adapter = albumAdapter

            // Bandera para indicar que ha sido inicializado
            isAdapterInitialized = true
        }
    }

    private fun getReviewsOrderedByParam(param: String) {
        try {

            // Dependiendo del tipo de ordenamiento, mandamos a llamar una petición u otra
            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseListReviews> = when (param) {
                "Fecha ascendente" -> service.getReviewsOrderedByDateAsc()
                "Fecha descendente" -> service.getReviewsOrderedByDateDesc()
                "ID ascendente" -> service.getReviewsOrderedByIdAsc()
                "ID descendente" -> service.getReviewsOrderedByIdDesc()
                else -> service.getReviewsOrderedByDateDesc()
            }

            result.enqueue(object: Callback<ResponseListReviews> {
                override fun onFailure(call: Call<ResponseListReviews>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(myContext,"Ocurrió un error al intentar obtener las reviews. Por favor, inténtalo de nuevo.",
                        Toast.LENGTH_SHORT).show()
                    agregarOffline()
                }

                override fun onResponse(call: Call<ResponseListReviews>, response: Response<ResponseListReviews>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(myContext,"Ocurrió un error al intentar obtener las reviews. Por favor, inténtalo de nuevo.",
                                Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        val responseBody = response.body()
                        val reviewsList = responseBody?.data
                        val status = responseBody?.status

                        if (status == "success"){

                            if (reviewsList != null) {

                                // Limpiar la lista de reviews
                                reviews.clear()
                                sqliteHelper.truncateTableReviewsHome() //Clear a la base de datos local

                                for(reviewItem in reviewsList){

                                    val review = ReviewObject()
                                    review.id = reviewItem?.id
                                    review.reviewTitle = reviewItem?.title
                                    review.reviewDescription = reviewItem?.description

                                    // Obtener las imagenes de la review
                                    val reviewImageList = ArrayList<ByteArray>()
                                    if(reviewItem?.images?.size!! > 0){
                                        reviewItem?.images?.forEach { image ->
                                            val strImage = image?.image!!.replace("data:image/png;base64,","")
                                            try {
                                                val byteArray = Base64.getDecoder().decode(strImage)
                                                if (byteArray != null) {
                                                    reviewImageList.add(byteArray)
                                                }
                                            } catch (e: IllegalArgumentException) {
                                                Log.e("catchImage", e.toString())
                                                //Toast.makeText(myContext, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                    // Agregar la lista de imagenes al objeto
                                    review.reviewImgArray = reviewImageList

                                    // Agregar los datos del User y Videogame
                                    review.user_id = reviewItem.user_id
                                    review.userNickname = reviewItem.user?.nickname
                                    review.restaurant_id = reviewItem?.restaurant_id
                                    review.restaurantTitle = reviewItem?.restaurant?.name

                                    // Agregar la review a la lista
                                    reviews.add(review)
                                    //Agregat a SQLite
                                    val imagen= if (( reviewItem?.images?.size!! > 0)) reviewImageList[0].toString() else ""
                                    addFavReviewsSQLite(reviewItem?.id!!.toInt(), reviewItem?.title.toString(), reviewItem?.description.toString(), imagen, reviewItem.user_id!!.toInt(), review.userNickname.toString(), reviewItem?.restaurant_id!!.toInt(), reviewItem?.restaurant?.name.toString())

                                }
                                // Asignar el Adapter al RecyclerView
                                albumAdapter = ProfileReviewsRecyclerAdapter(myContext, reviews)
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
            Toast.makeText(myContext, "Ocurrió un error al intentar obtener las reviews. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT)
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