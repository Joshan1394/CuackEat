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
import com.example.cuackeat.Models.Credential
import com.example.cuackeat.Models.ResponseUser
import com.example.cuackeat.Models.User
import com.example.cuackeat.Models.UserApplication
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    var imgProfileImage_Register: ImageView? = null
    var imgArray:ByteArray? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister_Register)
        btnRegister.setOnClickListener(this)

        val btnCamera = findViewById<ImageButton>(R.id.btnCamera_Register)
        btnCamera.setOnClickListener(this)

        val lblLogin = findViewById<TextView>(R.id.lblLogin_Register)
        lblLogin.setOnClickListener(this)

        imgProfileImage_Register = findViewById(R.id.imgProfileImage_Register)
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
            R.id.btnRegister_Register-> register()
            R.id.btnCamera_Register-> openCamera()
            R.id.lblLogin_Register-> login()
        }
    }

    // Registro
    private fun register(){
        try{

            val txtName = findViewById<TextView>(R.id.txtName_Register)
            val txtLastName = findViewById<TextView>(R.id.txtLastName_Register)
            val txtNickname = findViewById<TextView>(R.id.txtNickname_Register)
            val txtDescription = findViewById<TextView>(R.id.txtDescription_Register)
            val txtAddress = findViewById<TextView>(R.id.txtAddress_Register)
            val txtPhone = findViewById<TextView>(R.id.txtPhone_Register)
            val txtEmail = findViewById<TextView>(R.id.txtEmail_Register)
            val txtPassword = findViewById<TextView>(R.id.txtPassword_Register)
            val txtConfirmPasword = findViewById<TextView>(R.id.txtConfirmPassword_Register)

            val name = txtName.text.toString()
            val lastName = txtLastName.text.toString()
            val nickname = txtNickname.text.toString()
            val description = txtDescription.text.toString()
            val address = txtAddress.text.toString()
            val phone = txtPhone.text.toString()
            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()
            val confirmPassword = txtConfirmPasword.text.toString()

            var strEncodeImage:String? = null

            if(this.imgArray != null){
                val encodedString:String =  Base64.getEncoder().encodeToString(this.imgArray)
                strEncodeImage = "data:image/png;base64," + encodedString
            }

            //val user =   User(0,
            //    email,
            //    password,
            //    confirmPassword,
            //    nickname,
            //    description,
            //    name,
            //    lastName,
            //    address,
            //    phone,
            //    strEncodeImage)

            // Patrón Builder
            val user = User.Builder()
                .email(email)
                .password(password)
                .password_confirmation(confirmPassword)
                .nickname(nickname)
                .description(description)
                .name(name)
                .last_name(lastName)
                .address(address)
                .phone(phone)
                .image(strEncodeImage)
                .build()

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.register(user)
            Log.e("Error", result.toString())
            result.enqueue(object: Callback<ResponseUser> {

                override fun onFailure(call: Call<ResponseUser>, t: Throwable){
                    Log.e("Error", t.toString())
                    Toast.makeText(this@RegisterActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ResponseUser>, response: Response<ResponseUser>){
                    Log.e("Error", "efreson es un pollito?")
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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_Register)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_Register)
                            lblErrorMessage.setText("")

                            saveCredentials(user?.id.toString())

                            Log.e("prefs", UserApplication.prefs.getCredentials().id + " " +
                                    UserApplication.prefs.getCredentials().email + " " +
                                    UserApplication.prefs.getCredentials().password)

                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                }
            })

        }catch(e: Exception){
            Log.e("Error", e.toString())
            Toast.makeText(this@RegisterActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",Toast.LENGTH_LONG).show()
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
            this.imgProfileImage_Register!!.setImageBitmap(photo)

            val bitmap = (imgProfileImage_Register!!.getDrawable() as BitmapDrawable).bitmap

        } else {
            // El usuario canceló la operación de la cámara
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    // Abrir pantalla de registro
    private fun login(){

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun saveCredentials(id:String?) {

        val txtEmail = findViewById<TextView>(R.id.txtEmail_Register)
        val txtPassword = findViewById<TextView>(R.id.txtPassword_Register)

        val strEmail = txtEmail.text.toString()
        val strPassword = txtPassword.text.toString()

        val credential: Credential = Credential()
        credential.id =  id!!
        credential.email =  strEmail!!
        credential.password =  strPassword!!
        // credential.token =  token!!

        //ESTAMOS GRABANDO
        UserApplication.prefs.saveCredentials(credential)
    }
}