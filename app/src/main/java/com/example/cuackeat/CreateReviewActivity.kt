package com.example.cuackeat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Models.*
import com.example.cuackeat.ImageRecyclerAdapter
import com.example.cuackeat.SQLiteHelper
import org.json.JSONObject
import okhttp3.internal.toNonNegativeInt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class CreateReviewActivity : AppCompatActivity(), View.OnClickListener {

    var selectedImagesList = ArrayList<Uri>()
    val images = ArrayList<String>()
    var restaurant_id:Int = 0
    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val btnCreate = findViewById<Button>(R.id.btnCreate_CreateReview)
        btnCreate.setOnClickListener(this)

        val btnOpenGallery = findViewById<ImageButton>(R.id.imgBtnAddImage_CreateReview)
        btnOpenGallery.setOnClickListener(this)

        // Obtener el ID del restaurante en el Intent
        restaurant_id = intent.getIntExtra("restaurant_id", 0)

        // Obtener el nombre del restaurante a editar en el Intent
        val restaurantName = intent.getStringExtra("restaurantName")

        // Mostrar el nombre del restaurante en el TexView
        val lblRestaurantName = findViewById<TextView>(R.id.lblRestaurantName_CreateReview)
        lblRestaurantName.setText(restaurantName)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView_CreateReview)
        recyclerView.adapter = ImageRecyclerAdapter(this, selectedImagesList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnCreate_CreateReview-> createReview()
            R.id.imgBtnAddImage_CreateReview-> openGallery()
        }
    }

    val mimeTypes = arrayOf("image/*")
    private var launcher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        try{

            if (uris != null && uris.isNotEmpty()) {
                val recyclerView: RecyclerView = findViewById(R.id.recyclerView_CreateReview)
                recyclerView.adapter = ImageRecyclerAdapter(this, uris.toMutableList())
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                // Limpiamos el array de imágenes
                images?.clear()

                // Almacenar cada una de las imágenes en base64 al array
                // Definir la resolución máxima
                val maxWidth = 200.00
                val maxHeight = 200.00
                var targetWidth = 200
                var targetHeight = 200

                for (uri in uris) {

                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    val sourceWidth: Int = bitmap.width
                    val sourceHeight: Int = bitmap.height

                    targetWidth = 200
                    targetHeight = 200

                    val sourceRatio = sourceWidth.toFloat() / sourceHeight.toFloat()
                    val targetRatio = maxWidth.toFloat() / maxHeight.toFloat()

                    if (targetRatio > sourceRatio) {
                        targetWidth = (maxHeight.toFloat() * sourceRatio).toInt()
                    } else {
                        targetHeight = (maxWidth.toFloat() / sourceRatio).toInt()
                    }

                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val bytes = outputStream.toByteArray()
                    val encodedString:String =  Base64.getEncoder().encodeToString(bytes)
                    val strEncodeImage = "data:image/jpeg;base64," + encodedString
                    images?.add(strEncodeImage)
                }

            } else {
                // No se seleccionó una imagen, limpiar el array y el RecyclerView
                Toast.makeText(this, "No seleccionó ninguna imagen.", Toast.LENGTH_SHORT).show()
                images?.clear()
                val recyclerView: RecyclerView = findViewById(R.id.recyclerView_CreateReview)
                recyclerView.adapter = null
            }

        }catch(e: Exception){
            Log.e("Error", e.toString())
            Toast.makeText(this, "Ocurrió un error. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openGallery(){
        launcher.launch("image/*")
    }

    private fun addBorradorSQLite(id:Int, name:String, description:String, user_id:Int, restaurant_id:Int) {
        val std = ReviewModel(id=id, title=name, description = description, user_id = user_id, restaurant_id =restaurant_id)
        val status = sqliteHelper.insertReviewsBorrador(std)

        //Check insert success or not success
        if (status>-1)
        {
            Toast.makeText(this, "La review será públicada una vez tenga acceso a internet", Toast.LENGTH_SHORT).show()
        }else
        {
            Toast.makeText(this, "No se ha guardado en SQLITE", Toast.LENGTH_SHORT).show()
        }

        //Muestra lo último que fué guardado en la base de datos local
        for(ImgItem in images) {
            val std2 = ReviewImagesModel(review_image_id = 0, image_path = images.get(0), review_id = std.id)
            val status2 = sqliteHelper.insertReviewsImgsBorr(std2)

            //Check insert success or not success
            if (status2>-1)
            {
                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
            }else
            {
                Toast.makeText(this, "No se ha guardado las imagenes en SQLITE", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun validateReview(txtTitle:String, txtDescription:String, images:ArrayList<String>): Boolean {

        if(txtTitle.equals("") || txtDescription.equals("") || images.isEmpty()){
            return false
        }

        return true
    }

    // Crear la review
    private fun createReview(){
        try{

            val txtTitle = findViewById<TextView>(R.id.txtTitle_CreateReview)
            val txtDescription = findViewById<TextView>(R.id.txtDescription_CreateReview)

            val title = txtTitle.text.toString()
            val description = txtDescription.text.toString()

            if(!validateReview(title, description, images)){

                val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_CreateReview)
                lblErrorMessage.setText("Los campos no deben estar vacíos. Al menos una imagen es requerida.")

                return
            }

            // Aquí se deberá obtener el ID del restaurante, se obtiene del Intent desde los Detalles del restaurante a Crear Review
            // Aqui se deberá obtener el ID del usuario que Inició Sesión o se Registró
            val userId = UserApplication.prefs.getCredentials().id.toNonNegativeInt(0)
            val restaurantId = restaurant_id

            val review = Review(0, title, description, userId, restaurantId, images)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReview> = service.createReview(review)

            result.enqueue(object: Callback<ResponseReview> {

                override fun onFailure(call: Call<ResponseReview>, t: Throwable){
                    Log.e("Failure", t.toString())
                    Toast.makeText(this@CreateReviewActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()

                    //Acá se almacena en una queue
                    addBorradorSQLite(0, title, description, userId, restaurant_id)


                    finish()
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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_CreateReview)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_CreateReview)
                            lblErrorMessage.setText("")

                            Toast.makeText(this@CreateReviewActivity,"Reseña agregada con éxito.",
                                Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }
            })

        }catch(e: Exception){
            Log.e("Failure", e.toString())
            Toast.makeText(this@CreateReviewActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()
        }
    }
}