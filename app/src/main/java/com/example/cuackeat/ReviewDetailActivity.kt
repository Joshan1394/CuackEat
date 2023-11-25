package com.example.cuackeat

import android.app.AlertDialog
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
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ReviewDetailActivity: AppCompatActivity(), View.OnClickListener {
    var albumPosition:Int = 0
    var authorUserId:Int = 0
    var globalIsFavorite:Boolean = false
    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)

        val toolbarAlbumDetail = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarReviewDetail_ReviewDetail)
        toolbarAlbumDetail.setTitle("Detalles")
        setSupportActionBar(toolbarAlbumDetail)

        val btnReviewLike = findViewById<ImageButton>(R.id.btnReviewLike_ReviewDetail)
        btnReviewLike.setOnClickListener(this)

        // Obtener el ID de la Review
        this.albumPosition =  savedInstanceState?.getInt(ALBUM_POSITION, DEFAULT_ALBUM_POSITION) ?: intent.getIntExtra(ALBUM_POSITION,DEFAULT_ALBUM_POSITION)

        // Obtener el ID del Usuario Autor de la Review author_user_id
        this.authorUserId =  intent.getIntExtra("author_user_id", 0)

        // Obtener el Detalle de la Review
        getReviewDetails(this.albumPosition)
    }

    override fun onPause() {
        super.onPause()
        //this.saveAlbum()
    }

    override fun onResume() {
        super.onResume()

        // Obtener el Detalle de la Review para actualizar los cambios al editar
        //getReviewDetails(this.albumPosition)
    }

    //DISPLAY
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ALBUM_POSITION,this.albumPosition)
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

       // Inflate the menu; this adds items to the action bar if it is present.
       menuInflater.inflate(R.menu.menu_main, menu)

       val userId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)

       // Ocultar los iconos del menú si el usuario no es el creador de la review
       if (userId != authorUserId) {
           menu.findItem(R.id.itemActionEdit_MenuMain)?.isVisible = false
           menu.findItem(R.id.itemActionDelete_MenuMain)?.isVisible = false
       }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.itemActionEdit_MenuMain -> editReview()
            R.id.itemActionDelete_MenuMain -> showDeleteReviewDialog()
            else -> super.onOptionsItemSelected(item)
        }
        return true
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
            R.id.btnReviewLike_ReviewDetail->toggleLikeReview()
        }
    }

    private fun editReview(){
        // Lanzamos el intent para abrir la pantalla de editar la review
        val activityIntent = Intent(this@ReviewDetailActivity, EditReviewActivity::class.java)
        activityIntent.putExtra("review_id", this.albumPosition)
        startActivity(activityIntent)
    }

    private fun deleteReview(){
        try {
            // Obtener el ID del Usuario y el ID de la Review
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val intReviewId = this.albumPosition

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReview> = service.deleteReview(intReviewId, intUserId)

            result.enqueue(object: Callback<ResponseReview> {
                override fun onFailure(call: Call<ResponseReview>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ResponseReview>, response: Response<ResponseReview>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                        }

                    } else {

                        val responseUser = response.body()
                        val status = responseUser?.status

                        if (status == "success"){
                            // Enviar a HomeActivity y limpiar todas las Task
                            Toast.makeText(this@ReviewDetailActivity,"Reseña eliminada correctamente.",Toast.LENGTH_LONG).show()
                            val intent = Intent(this@ReviewDetailActivity, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }

                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
        }
    }

    private fun getReviewDetails(id: Int){
        try {

            // Obtener el ID del Usuario Autenticado
            val userId = UserApplication.prefs.getCredentials().id
            val intUserId = userId.toNonNegativeInt(0)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReviewDetail> = service.getReviewDetails(id, intUserId)

            result.enqueue(object: Callback<ResponseReviewDetail> {
                override fun onFailure(call: Call<ResponseReviewDetail>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()

                    val stdList= sqliteHelper.getReview(albumPosition)
                    val stdListRestaurant= sqliteHelper.getRestaurant(stdList[0].restaurant_id)
                    //Variables que tenemos de las review
                    val txtReviewTitle = findViewById<TextView>(R.id.lblReviewTitle_ReviewDetail)
                    val txtReviewDescription = findViewById<TextView>(R.id.lblReviewDescription_ReviewDetail)
                    val txtLikesCount = findViewById<TextView>(R.id.lblLikesCount_ReviewDetail)

                    val imgRestaurantImage = findViewById<ImageView>(R.id.imgRestaurantImage_ReviewDetail)
                    val txtRestaurantName = findViewById<TextView>(R.id.lblRestaurantName_ReviewDetail)
                    val txtRestaurantDescription = findViewById<TextView>(R.id.lblRestaurantDescription_ReviewDetail)
                    val txtRestaurantLocation = findViewById<TextView>(R.id.lblLocation_ReviewDetail)
                    // Llenar los campos con los datos del Restaurante
                    txtReviewTitle.setText(stdList[0].title)
                    txtReviewDescription.setText(stdList[0].description)
                    val linearLayoutReviewImages = findViewById<LinearLayout>(R.id.linearLayoutReviewImages_ReviewDetail)

                    txtRestaurantName.setText(stdListRestaurant[0].name)
                    txtRestaurantDescription.setText(stdListRestaurant[0].description)
                    txtRestaurantLocation.setText(stdListRestaurant[0].location)

                    // Establecer la imagen del Restaurante
                    val strImage:String = stdListRestaurant[0].image.replace("data:image/png;base64,","")
                    try {
                        val byteArray = Base64.getDecoder().decode(strImage)
                        if(byteArray != null){
                            imgRestaurantImage!!.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                        }
                    } catch (e: IllegalArgumentException){
                        // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                        val drawable = ContextCompat.getDrawable(this@ReviewDetailActivity, R.drawable.circle)
                        imgRestaurantImage.setImageDrawable(drawable)

                        Toast.makeText(this@ReviewDetailActivity, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
                        finish()
                    }

                    // Establecer la cantidad de likes
                    //txtLikesCount.setText(data?.favorite_videogames_count.toString())
                    txtLikesCount.setText("Sin conexión a internet")

                    val stdImgs= sqliteHelper.getReviewImagesIdDetail(id)

                    if(stdImgs.size!! > 0){
                        stdImgs.forEach { image ->
                            val strImage = image

                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if (byteArray != null) {
                                    // Construir el ImageView para agregarla al LinearLayout
                                    val imageViewReviewImage = ImageView(this@ReviewDetailActivity)
                                    // Definir las dimensiones de la imagen
                                    val size = resources.getDimensionPixelSize(R.dimen.review_image_size)
                                    val lp = LinearLayout.LayoutParams(
                                        size,
                                        size
                                    )
                                    val margin = resources.getDimensionPixelSize(R.dimen.fab_margin)
                                    lp.setMargins(0, 0, 0, margin)
                                    imageViewReviewImage.setLayoutParams(lp);
                                    // Establecer el scaleType a centerCrop
                                    imageViewReviewImage.scaleType = ImageView.ScaleType.FIT_CENTER
                                    // Aplicar la imagen en Bitmap
                                    imageViewReviewImage.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                                    // Agregar la imagen al Linear Layout
                                    linearLayoutReviewImages.addView(imageViewReviewImage)
                                    //reviewImageList.add(byteArray)
                                }
                            } catch (e: IllegalArgumentException) {
                                Log.e("catchImage", e.toString())
                                Toast.makeText(this@ReviewDetailActivity, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    //finish()
                }

                override fun onResponse(call: Call<ResponseReviewDetail>, response: Response<ResponseReviewDetail>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val ReviewDetail = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val imgRestaurantImage = findViewById<ImageView>(R.id.imgRestaurantImage_ReviewDetail)
                            val txtRestaurantName = findViewById<TextView>(R.id.lblRestaurantName_ReviewDetail)
                            val txtRestaurantDescription = findViewById<TextView>(R.id.lblRestaurantDescription_ReviewDetail)
                            val txtRestaurantLocation = findViewById<TextView>(R.id.lblLocation_ReviewDetail)
                            val txtAlias = findViewById<TextView>(R.id.lblUserNickname_ReviewDetail)
                            val txtReviewTitle = findViewById<TextView>(R.id.lblReviewTitle_ReviewDetail)
                            val txtReviewDescription = findViewById<TextView>(R.id.lblReviewDescription_ReviewDetail)
                            val linearLayoutReviewImages = findViewById<LinearLayout>(R.id.linearLayoutReviewImages_ReviewDetail)
                            val txtLikesCount = findViewById<TextView>(R.id.lblLikesCount_ReviewDetail)
                            val btnRestaurantLike = findViewById<ImageButton>(R.id.btnReviewLike_ReviewDetail)
                            val isFavorite: Boolean = ReviewDetail?.is_favorite!!
                            // Guardar la variable para su posterior uso en el toggle like
                            globalIsFavorite = ReviewDetail?.is_favorite!!

                            // Llenar los campos del Restaurante
                            txtRestaurantName.setText(ReviewDetail?.restaurant?.name)
                            txtRestaurantDescription.setText(ReviewDetail?.restaurant?.description)
                            txtRestaurantLocation.setText(ReviewDetail?.restaurant?.location)

                            // Llenar los campos con los datos de la Review
                            txtAlias.setText(ReviewDetail?.user?.nickname)
                            txtReviewTitle.setText(ReviewDetail?.title)
                            txtReviewDescription.setText(ReviewDetail?.description)

                            // Establecer la imagen del Restaurante
                            val strImage:String =  ReviewDetail?.restaurant?.image?.get(0)!!.replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    imgRestaurantImage!!.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                                }
                            } catch (e: IllegalArgumentException){
                                // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                                val drawable = ContextCompat.getDrawable(this@ReviewDetailActivity, R.drawable.circle)
                                imgRestaurantImage.setImageDrawable(drawable)

                                Toast.makeText(this@ReviewDetailActivity, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
                                finish()
                            }

                            //AGREGAR
                            sqliteHelper.truncateTableReviewsImage(id) //Primero quitamos los registros

                            // Desplegar las imágenes de la Review
                            //val reviewImageList = ArrayList<ByteArray>()
                            if(ReviewDetail?.images?.size!! > 0){
                                ReviewDetail?.images?.forEach { image ->
                                    val strImage = image.image!!.replace("data:image/png;base64,","")

                                    //ASÍ agregaremos las imagenes en SQLITE
                                    val img = ReviewImagesModel(0, strImage, id)
                                    val status = sqliteHelper.insertReviewsImgs(img)

                                    try {
                                        val byteArray = Base64.getDecoder().decode(strImage)
                                        if (byteArray != null) {
                                            // Construir el ImageView para agregarla al LinearLayout
                                            val imageViewReviewImage = ImageView(this@ReviewDetailActivity)
                                            // Definir las dimensiones de la imagen
                                            val size = resources.getDimensionPixelSize(R.dimen.review_image_size)
                                            val lp = LinearLayout.LayoutParams(
                                                size,
                                                size
                                            )
                                            val margin = resources.getDimensionPixelSize(R.dimen.fab_margin)
                                            lp.setMargins(0, 0, 0, margin)
                                            imageViewReviewImage.setLayoutParams(lp);
                                            // Establecer el scaleType a centerCrop
                                            imageViewReviewImage.scaleType = ImageView.ScaleType.FIT_CENTER
                                            // Aplicar la imagen en Bitmap
                                            imageViewReviewImage.setImageBitmap(com.example.cuackeat.ImageUtilities.getBitMapFromByteArray(byteArray))
                                            // Agregar la imagen al Linear Layout
                                            linearLayoutReviewImages.addView(imageViewReviewImage)
                                            //reviewImageList.add(byteArray)
                                        }
                                    } catch (e: IllegalArgumentException) {
                                        Log.e("catchImage", e.toString())
                                        Toast.makeText(this@ReviewDetailActivity, "Hubo un error al intentar obtener algunas imágenes.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            // Establecer la cantidad de likes
                            txtLikesCount.setText(ReviewDetail?.favorite_reviews_count.toString())

                            // Definir el color del botón dependiendo de si lo tiene en Favoritos
                            val colorFav: Int = ContextCompat.getColor(this@ReviewDetailActivity, R.color.claret);
                            val colorNoFav: Int = ContextCompat.getColor(this@ReviewDetailActivity, R.color.china_rose);

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
            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            //finish()
        }
    }

    private fun showDeleteReviewDialog() {
        // Mostrar el Dialog para Eliminar la Review
        val builder = AlertDialog.Builder(this@ReviewDetailActivity)
        builder.setTitle("Eliminar reseña")
        builder.setMessage("¿Deseas eliminar la reseña?")
        builder.setPositiveButton("Eliminar") { dialog, which ->
            // Mandar a llamar deleteReview
            this.deleteReview()
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun toggleLikeReview(){
        // Verificar el estado del fondo del botón
        if (this.globalIsFavorite) {
            // El usuario tiene el videojuego en Favoritos
            removeReviewFromFavs()
        } else {
            // El usuario tiene el videojuego en Favoritos, eliminarlo
            addReviewToFavs()
        }
    }

    private fun addReviewToFavs(){
        try {
            // Obtener el ID del Usuario
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val intReviewId = this.albumPosition

            val favoriteReview = FavoriteReview(null, intUserId, intReviewId, null)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteReview> = service.addReviewToFavorites(favoriteReview)

            result.enqueue(object: Callback<ResponseFavoriteReview> {
                override fun onFailure(call: Call<ResponseFavoriteReview>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al agregar la reseña a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseFavoriteReview>, response: Response<ResponseFavoriteReview>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al agregar la reseña a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val status = responseUser?.status

                        if (status == "success"){
                            Toast.makeText(this@ReviewDetailActivity,"Reseña agregada a favoritos correctamente.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al agregar la reseña a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun getBorradores() {
        val stdList = sqliteHelper.getReviewImagesIdDetail(10)//Obtenemos la lista de imagenes que pertenece
    }

    private fun removeReviewFromFavs(){
        try {

            // Obtener el ID del Usuario y el ID del Restaurante
            val intUserId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val intReviewId = this.albumPosition

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseFavoriteReview> = service.removeReviewFromFavorites(intReviewId, intUserId)

            result.enqueue(object: Callback<ResponseFavoriteReview> {
                override fun onFailure(call: Call<ResponseFavoriteReview>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseFavoriteReview>, response: Response<ResponseFavoriteReview>) {

                    if (!response.isSuccessful) {

                        // Parsear errorBody a JSON
                        val responseBody = response.errorBody()?.string()
                        val jsonObject = JSONObject(responseBody)
                        val statusJson = jsonObject.getString("status")

                        if (statusJson == "failed"){
                            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña a favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseUser = response.body()
                        val status = responseUser?.status

                        if (status == "success"){
                            Toast.makeText(this@ReviewDetailActivity,"Reseña eliminada de favoritos correctamente.",Toast.LENGTH_LONG).show()
                            finish()
                        }

                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@ReviewDetailActivity,"Ocurrió un error al eliminar la reseña de favoritos. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

}