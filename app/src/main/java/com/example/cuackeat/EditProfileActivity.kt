package com.example.cuackeat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.cuackeat.Models.ResponseUser
import com.example.cuackeat.Models.User
import com.example.cuackeat.Models.UserApplication
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity(), View.OnClickListener {

    // Referencia al ImageView de la foto de perfil
    var imgProfileImage_EditProfile: ImageView? = null
    // ByteArray de la imagen
    var imgArray:ByteArray? =  null
    // Variable con la imagen en base64
    var strEncodeImage:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Obtener la informacion del usuario y desplegarla en los campos
        var id = intent.extras!!.getString("id")

        // Si el usuario no está autenticado, regresar al Login
        if(id!!.equals("0")) {
            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        getUser(id.toNonNegativeInt(0))

        // OnClickListener el botón para Abrir la Cámara
        val btnCamera = findViewById<ImageButton>(R.id.btnCamera_EditProfile)
        btnCamera.setOnClickListener(this)

        // OnClickListener el botón para Guardar Cambios
        val btnSave = findViewById<Button>(R.id.btnSave_EditProfile)
        btnSave.setOnClickListener(this)

        // Obtener la referencia del ImageView
        imgProfileImage_EditProfile = findViewById(R.id.imgProfileImage_EditProfile)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
        //camera code
        private val CAMERA_CODE = 1002;
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnCamera_EditProfile-> openCamera()
            R.id.btnSave_EditProfile-> updateUserInfo()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val photo =  result.data?.extras?.get("data") as Bitmap
            val stream = ByteArrayOutputStream()
            //Bitmap.CompressFormat agregar el formato desado, estoy usando aqui jpeg
            photo.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            //Agregamos al objecto album el arreglo de bytes
            imgArray =  stream.toByteArray()
            //Mostramos la imagen en la vista
            this.imgProfileImage_EditProfile!!.setImageBitmap(photo)

            val bitmap = (imgProfileImage_EditProfile!!.getDrawable() as BitmapDrawable).bitmap

            // Resetear la variable strEncodeImage
            strEncodeImage = null

        } else {
            // El usuario canceló la operación de la cámara
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun getUser(id: Int){
        try {

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.getUser(id)

            result.enqueue(object: Callback<ResponseUser>{
                override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@EditProfileActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                    finish()
                }

                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>) {

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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditProfile)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val txtName = findViewById<TextView>(R.id.txtName_EditProfile)
                            val txtLastName = findViewById<TextView>(R.id.txtLastName_EditProfile)
                            val txtNickname = findViewById<TextView>(R.id.txtNickname_EditProfile)
                            val txtDescription = findViewById<TextView>(R.id.txtDescription_EditProfile)
                            val txtAddress = findViewById<TextView>(R.id.txtAddress_EditProfile)
                            val txtPhone = findViewById<TextView>(R.id.txtPhone_EditProfile)
                            val txtEmail = findViewById<TextView>(R.id.txtEmail_EditProfile)
                            val imgProfileImage = findViewById<ImageView>(R.id.imgProfileImage_EditProfile)

                            // Llenar los campos con los datos del usuario
                            txtName.setText(user?.name)
                            txtLastName.setText(user?.last_name)
                            txtNickname.setText(user?.nickname)
                            txtDescription.setText(user?.description)
                            txtAddress.setText(user?.address)
                            txtPhone.setText(user?.phone)
                            txtEmail.setText(user?.email)
                            strEncodeImage = user?.image

                            val strImage:String =  user?.image.toString().replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    imgProfileImage!!.setImageBitmap(ImageUtilities.getBitMapFromByteArray(byteArray))
                                }
                            } catch (e: IllegalArgumentException){
                                // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                                val drawable = ContextCompat.getDrawable(this@EditProfileActivity, R.drawable.circle)
                                imgProfileImage.setImageDrawable(drawable)

                                Toast.makeText(this@EditProfileActivity, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG)
                                finish()
                            }

                            // Limpiar el campo de los errores
                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditProfile)
                            lblErrorMessage.setText("")
                        }
                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@EditProfileActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Registro
    private fun updateUserInfo(){
        try {


            val txtName = findViewById<TextView>(R.id.txtName_EditProfile)
            val txtLastName = findViewById<TextView>(R.id.txtLastName_EditProfile)
            val txtNickname = findViewById<TextView>(R.id.txtNickname_EditProfile)
            val txtDescription = findViewById<TextView>(R.id.txtDescription_EditProfile)
            val txtAddress = findViewById<TextView>(R.id.txtAddress_EditProfile)
            val txtPhone = findViewById<TextView>(R.id.txtPhone_EditProfile)
            val txtEmail = findViewById<TextView>(R.id.txtEmail_EditProfile)

            val name = txtName.text.toString()
            val lastName = txtLastName.text.toString()
            val nickname = txtNickname.text.toString()
            val description = txtDescription.text.toString()
            val address = txtAddress.text.toString()
            val phone = txtPhone.text.toString()
            val email = txtEmail.text.toString()

            if(this.imgArray != null && strEncodeImage == null){
                val encodedString:String =  Base64.getEncoder().encodeToString(this.imgArray)
                strEncodeImage = "data:image/png;base64," + encodedString
            }

            val strUserId = UserApplication.prefs.getCredentials().id
            val intUserId = strUserId.toNonNegativeInt(0)

            if(intUserId == 0){
                val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            //val user =   User(intUserId,
            //    email,
            //    "",
            //    "",
            //    nickname,
            //    description,
            //    name,
            //    lastName,
            //    address,
            //    phone,
            //    strEncodeImage)

            // Patrón Builder
            val user = User.Builder()
                .id(intUserId)
                .email(email)
                .nickname(nickname)
                .description(description)
                .name(name)
                .last_name(lastName)
                .address(address)
                .phone(phone)
                .image(strEncodeImage)
                .build()

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.updateUserInfo(intUserId, user)

            result.enqueue(object: Callback<ResponseUser> {

                override fun onFailure(call: Call<ResponseUser>, t: Throwable){
                    Log.e("FAILED", t.toString())
                    Toast.makeText(this@EditProfileActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>){

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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditProfile)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditProfile)
                            lblErrorMessage.setText("")

                            Toast.makeText(this@EditProfileActivity, "Perfil editado correctamente.", Toast.LENGTH_LONG).show()

                            val intent = Intent(this@EditProfileActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(this@EditProfileActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
        }
    }
}