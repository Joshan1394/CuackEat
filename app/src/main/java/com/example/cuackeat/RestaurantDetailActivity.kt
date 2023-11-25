package com.example.cuackeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cuackeat.Data.*
import com.example.cuackeat.Models.*
import com.example.cuackeat.Utilities.ImageUtilies
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


class RestaurantDetailActivity: AppCompatActivity(), View.OnClickListener {
    var albumPosition:Int = 0
    var globalIsFavorite:Boolean = false
    private lateinit var restaurantName:String
    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        val toolbarAlbumDetail = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarRestaurantDetail_RestaurantDetail)
        toolbarAlbumDetail.setTitle("Detalles")
        setSupportActionBar(toolbarAlbumDetail)

        val btnRestaurantLike = findViewById<ImageButton>(R.id.btnRestaurantLike_RestaurantDetail)
        btnRestaurantLike.setOnClickListener(this)

        val btnAddReview = findViewById<Button>(R.id.btnAddReview_RestaurantDetail)
        btnAddReview.setOnClickListener(this)

        // Obtener el ID del Restaurante
        this.albumPosition =  savedInstanceState?.getInt(ALBUM_POSITION, DEFAULT_ALBUM_POSITION) ?: intent.getIntExtra(ALBUM_POSITION,DEFAULT_ALBUM_POSITION)

        // Obtener el detalle del restaurante
        getRestaurantDetails(this.albumPosition)
    }

    override fun onPause() {
        super.onPause()
        //this.saveAlbum()
    }

    //DISPLAY
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ALBUM_POSITION,this.albumPosition)
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
       //menuInflater.inflate(R.menu.menu_main, menu)
       return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        //return when(item.itemId){
        //    R.id.action_next -> this.moveNext()
        //    else -> super.onOptionsItemSelected(item)
        //}

        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean{
        //if(albumPosition >= DataManager.albums.lastIndex){
        //    val menuItem =  menu?.findItem(R.id.action_next)
        //    if(menuItem != null){
        //        menuItem.setIcon(R.drawable.ic_block_24)
        //        menuItem.isEnabled =  false
        //    }
        //}
        //return super.onPrepareOptionsMenu(menu)

        return false
    }

    //CLICK
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnRestaurantLike_RestaurantDetail->toggleLikeRestaurant()
            R.id.btnAddReview_RestaurantDetail->addReview()
        }
    }

    private fun addReview(){
        val intent = Intent(this, CreateReviewActivity::class.java)
        intent.putExtra("restaurant_id", this.albumPosition)
        intent.putExtra("restaurantName", this.restaurantName)
        startActivity(intent)
    }

    private fun getRestaurantDetails(id: Int){
        try {

            // Obtener el ID del Usuario
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseRestaurantDetail> = service.getRestaurantDetails(id, intUserId)

            result.enqueue(object: Callback<ResponseRestaurantDetail> {
                override fun onFailure(call: Call<ResponseRestaurantDetail>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    //Toast.makeText(this@VideogameDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    //finish()
                    val stdList= sqliteHelper.getRestaurant(albumPosition)
                    val txtName = findViewById<TextView>(R.id.lblName_RestaurantDetail)
                    val txtDescription = findViewById<TextView>(R.id.lblDescription_RestaurantDetail)
                    val imgAlbum = findViewById<ImageView>(R.id.imgRestaurantImage_RestaurantDetail)
                    val txtLikesCount = findViewById<TextView>(R.id.lblLikesCount_RestaurantDetail)
                    val btnRestaurantLike = findViewById<ImageButton>(R.id.btnRestaurantLike_RestaurantDetail)
                    /*val isFavorite: Boolean = data?.is_favorite!!
                        // Guardar la variable para su posterior uso en el toggle like
                        globalIsFavorite = data?.is_favorite!!
                    */
                    // Llenar los campos con los datos del Restaurante
                    txtName.setText(stdList[0].name)
                    txtDescription.setText(stdList[0].description)

                    // Guardar el nombre del Restaurante en una variable para mandarlo en el Intent de Crear Review
                    restaurantName = stdList[0].name!!

                    val strImage:String =  stdList[0].image.replace("data:image/png;base64,","")
                    try {
                        val byteArray = Base64.getDecoder().decode(strImage)
                        if(byteArray != null){
                            imgAlbum!!.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                        }
                    } catch (e: IllegalArgumentException){
                        // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                        val drawable = ContextCompat.getDrawable(this@RestaurantDetailActivity, R.drawable.circle)
                        imgAlbum.setImageDrawable(drawable)

                        Toast.makeText(this@RestaurantDetailActivity, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
                        finish()
                    }

                    // Establecer la cantidad de likes
                    //txtLikesCount.setText(data?.favorite_videogames_count.toString())
                    txtLikesCount.setText("Sin conexión a internet")
                }

                override fun onResponse(call: Call<ResponseRestaurantDetail>, response: Response<ResponseRestaurantDetail>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val data = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val txtName = findViewById<TextView>(R.id.lblName_RestaurantDetail)
                            val txtDescription = findViewById<TextView>(R.id.lblDescription_RestaurantDetail)
                            val txtLocation = findViewById<TextView>(R.id.lblLocation_RestaurantDetail)
                            val imgAlbum = findViewById<ImageView>(R.id.imgRestaurantImage_RestaurantDetail)
                            val txtLikesCount = findViewById<TextView>(R.id.lblLikesCount_RestaurantDetail)
                            val btnRestaurantLike = findViewById<ImageButton>(R.id.btnRestaurantLike_RestaurantDetail)
                            val isFavorite: Boolean = data?.is_favorite!!
                            // Guardar la variable para su posterior uso en el toggle like
                            globalIsFavorite = data?.is_favorite!!

                            // Llenar los campos con los datos del Restaurante
                            txtName.setText(data?.name)
                            txtDescription.setText(data?.description)
                            txtLocation.setText(data?.location)

                            // Guardar el nombre del Restaurante en una variable para mandarlo en el Intent de Crear Review
                            restaurantName = data?.name!!

                            val strImage:String =  data?.image.toString().replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    imgAlbum!!.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                                }
                            } catch (e: IllegalArgumentException){
                                // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                                val drawable = ContextCompat.getDrawable(this@RestaurantDetailActivity, R.drawable.circle)
                                imgAlbum.setImageDrawable(drawable)

                                //Toast.makeText(this@VideogameDetailActivity, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
                                finish()
                            }

                            // Establecer la cantidad de likes
                            txtLikesCount.setText(data?.favorite_restaurants_count.toString())

                            // Definir el color del botón dependiendo de si lo tiene en Favoritos
                            val colorFav: Int = ContextCompat.getColor(this@RestaurantDetailActivity, R.color.claret);
                            val colorNoFav: Int = ContextCompat.getColor(this@RestaurantDetailActivity, R.color.china_rose);

                            if(isFavorite){
                                btnRestaurantLike.setBackgroundColor(colorFav);
                            } else {
                                btnRestaurantLike.setBackgroundColor(colorNoFav);
                            }

                        }
                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun toggleLikeRestaurant(){
        // Verificar el estado del fondo del botón
        if (this.globalIsFavorite) {
            // El usuario tiene el restaurante en Favoritos
            removeRestaurantFromFavs()
        } else {
            // El usuario tiene el restaurante en Favoritos, eliminarlo
            addRestaurantToFavs()
        }
    }

    private fun addRestaurantToFavs(){
        try {
            // Obtener el ID del Usuario
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val intRestaurantId = this.albumPosition

            val favoriteRestaurant = FavoriteRestaurant(null, intUserId, intRestaurantId, null)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteRestaurant> = service.addRestaurantToFavorites(favoriteRestaurant)

            result.enqueue(object: Callback<ResponseFavoriteRestaurant> {
                override fun onFailure(call: Call<ResponseFavoriteRestaurant>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al agregar el restaurante a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseFavoriteRestaurant>, response: Response<ResponseFavoriteRestaurant>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al agregar el restaurante a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val status = responseUser?.status

                        if (status == "success"){
                            Toast.makeText(this@RestaurantDetailActivity,"Restaurante agregado a favoritos correctamente.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al agregar el restaurante a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun removeRestaurantFromFavs(){
        try {
            // Obtener el ID del Usuario y el ID del Restaurante
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val intRestaurantId = this.albumPosition

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteRestaurant> = service.removeRestaurantFromFavorites(intRestaurantId, intUserId)

            result.enqueue(object: Callback<ResponseFavoriteRestaurant> {
                override fun onFailure(call: Call<ResponseFavoriteRestaurant>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al eliminar el restaurante a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseFavoriteRestaurant>, response: Response<ResponseFavoriteRestaurant>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al eliminar el restaurante a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val status = responseUser?.status

                        if (status == "success"){
                            Toast.makeText(this@RestaurantDetailActivity,"Restaurante eliminado de favoritos correctamente.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@RestaurantDetailActivity,"Ocurrió un error al eliminar el restaurante de favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

}