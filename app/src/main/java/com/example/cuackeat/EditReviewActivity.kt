package com.example.cuackeat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cuackeat.Models.ResponseReview
import com.example.cuackeat.Models.ResponseReviewDetail
import com.example.cuackeat.Models.Review
import com.example.cuackeat.Models.UserApplication
import com.example.cuackeat.HomeActivity
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


class EditReviewActivity : AppCompatActivity(), View.OnClickListener {

    val images = ArrayList<String>()
    var review_id:Int? = 0
    var user_id:Int? = 0
    var restaurant_id:Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_review)

        getReviewDetails()

        val btnSave = findViewById<Button>(R.id.btnSave_EditReview)
        btnSave.setOnClickListener(this)

        val btnOpenGallery = findViewById<ImageButton>(R.id.imgBtnAddImage_EditReview)
        btnOpenGallery.setOnClickListener(this)
    }

    private fun getReviewDetails(){
        try {

            // Obtener el ID del Usuario Autenticado
            val userId = UserApplication.prefs.getCredentials().id
            val intUserId = userId.toNonNegativeInt(0)

            // Obtener el ID de la Review en el Intent
            val reviewId:Int = intent.getIntExtra("review_id", 0)

            val service: Service = RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReviewDetail> = service.getReviewDetails(reviewId, intUserId)

            result.enqueue(object: Callback<ResponseReviewDetail> {
                override fun onFailure(call: Call<ResponseReviewDetail>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@EditReviewActivity,"Ocurrió un error. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseReviewDetail>, response: Response<ResponseReviewDetail>) {

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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditReview)
                            lblErrorMessage.setText(errorMessage.toString())

                            Log.e("FAILED", errorMessage.toString())
                            Toast.makeText(this@EditReviewActivity,"Ocurrió un error. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                            finish()
                        }

                    } else {

                        val responseReview = response.body()
                        val review = responseReview?.data
                        val status = responseReview?.status

                        if (status == "success"){

                            // Almacenar el user_id y restaurant_id que serán utilizados para Editar la Review
                            review_id = review?.id
                            user_id = review?.user_id
                            restaurant_id = review?.restaurant_id

                            // Llenar los campos con los datos del usuario
                            val lblRestaurantName = findViewById<TextView>(R.id.lblRestaurantName_EditReview)
                            val txtTitle = findViewById<TextView>(R.id.txtTitle_EditReview)
                            val txtDescription = findViewById<TextView>(R.id.txtDescription_EditReview)

                            lblRestaurantName.setText(review?.restaurant?.name)
                            txtTitle.setText(review?.title)
                            txtDescription.setText(review?.description)

                            // Limpiar el campo de los errores
                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditReview)
                            lblErrorMessage.setText("")

                            // Obtener todas las Uri de las imágenes
                            val uris = ArrayList<Uri>()

                            for (image in review?.images.orEmpty()) {
                                val base64 = image.image?.substringAfter(",") ?: ""
                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val path = MediaStore.Images.Media.insertImage(contentResolver, bmp, null, null)
                                val uri = Uri.parse(path)
                                uris.add(uri)

                                // Agregar las imágenes a la lista que se mandará a la petición
                                val base64Image:String = image.image?: ""
                                images.add(base64Image)
                            }

                            // Configurar el RecyclerView
                            val recyclerView: RecyclerView = findViewById(R.id.recyclerView_EditReview)
                            recyclerView.layoutManager = LinearLayoutManager(this@EditReviewActivity, LinearLayoutManager.HORIZONTAL, false)
                            recyclerView.adapter = ImageRecyclerAdapter(this@EditReviewActivity, uris)

                            // for (image in review?.images.orEmpty()) {
                            //     // hacer algo con cada imagen
                            //     val imageUrl = image.image
                            //     Log.d("Review Image", imageUrl?: "")
                            // }
                        }
                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@EditReviewActivity,"Ocurrió un error. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnSave_EditReview-> editReview()
            R.id.imgBtnAddImage_EditReview-> openGallery()
        }
    }

    val mimeTypes = arrayOf("image/*")
    private var launcher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        try{

            if (uris != null && uris.isNotEmpty()) {
                val recyclerView: RecyclerView = findViewById(R.id.recyclerView_EditReview)
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
                    val encodedString:String =  java.util.Base64.getEncoder().encodeToString(bytes)
                    val strEncodeImage = "data:image/jpeg;base64," + encodedString
                    images?.add(strEncodeImage)
                }

            } else {
                // No se seleccionó una imagen, limpiar el array y el RecyclerView
                Toast.makeText(this, "No seleccionó ninguna imagen.", Toast.LENGTH_SHORT).show()
                images?.clear()
                val recyclerView: RecyclerView = findViewById(R.id.recyclerView_EditReview)
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

    private fun editReview(){
        try{

            val txtTitle = findViewById<TextView>(R.id.txtTitle_EditReview)
            val txtDescription = findViewById<TextView>(R.id.txtDescription_EditReview)

            val title = txtTitle.text.toString()
            val description = txtDescription.text.toString()

            // Se obtiene el ID de la Review a editar
            val reviewId:Int = review_id?: 0

            // Crear el objeto que se enviará a la petición
            val review = Review(0, title, description, user_id, restaurant_id, images)

            // Realizar la petición para Editar la Review
            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseReview> = service.updateReview(reviewId, review)

            result.enqueue(object: Callback<ResponseReview> {

                override fun onFailure(call: Call<ResponseReview>, t: Throwable){
                    Log.e("Failure", t.toString())
                    Toast.makeText(this@EditReviewActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()
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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditReview)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditReview)
                            lblErrorMessage.setText("")

                            // Enviar a HomeActivity y limpiar todas las Task
                            Toast.makeText(this@EditReviewActivity, "Reseña editada correctamente.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@EditReviewActivity, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
                }
            })

        }catch(e: Exception){
            Log.e("Failure", e.toString())
            Toast.makeText(this@EditReviewActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()
        }
    }
}