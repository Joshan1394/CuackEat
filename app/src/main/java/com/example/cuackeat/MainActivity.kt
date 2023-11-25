package com.example.cuackeat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.cuackeat.Models.Credential
import com.example.cuackeat.Models.ResponseUser
import com.example.cuackeat.Models.User
import com.example.cuackeat.Models.UserApplication
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin_Login)
        btnLogin.setOnClickListener(this)

        val lblRegister = findViewById<TextView>(R.id.lblRegister_Login)
        lblRegister.setOnClickListener(this)

        // Iniciar sesión si tiene guardadas las credenciales
        // Obtener las credenciales de SharedPrefs
        val email = UserApplication.prefs.getCredentials().email
        val password = UserApplication.prefs.getCredentials().password
        // Lanzar la HomeAcitivty
        if (!email.equals("") && !password.equals("")) {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnLogin_Login-> login()
            R.id.lblRegister_Login-> register()
        }
    }

    // OBTENER CREDENCIALES
    private fun login(){
        try{

            val txtEmail = findViewById<TextView>(R.id.txtEmail_Login)
            val txtPassword = findViewById<TextView>(R.id.txtPassword_Login)

            val email = txtEmail.text.toString()
            val password = txtPassword.text.toString()

            //val user =   User(0,
            //    email,
            //    password,
            //    "",
            //    "",
            //    "",
            //    "",
            //    "",
            //    "",
            //    "",
            //    "")

            // Patrón Builder
            val user = User.Builder()
                .email(email)
                .password(password)
                .build()

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.login(user)

            result.enqueue(object: Callback<ResponseUser> {

                override fun onFailure(call: Call<ResponseUser>, t: Throwable){

                    Toast.makeText(this@MainActivity,"Ocurrió un error. Por favor, inténtalo nuevamente.",
                        Toast.LENGTH_LONG).show()
                    Log.e("Error", t.toString())
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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_Login)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_Login)
                            lblErrorMessage.setText("")

                            // Guardar las credenciales del usuario usando SharedPreference
                            saveCredentials(user?.id.toString())

                            Log.e("prefs", UserApplication.prefs.getCredentials().id
                                    + " " + UserApplication.prefs.getCredentials().email + " "
                                    + UserApplication.prefs.getCredentials().password)

                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                }
            })

        }catch(e: Exception){
            Log.e("Error", e.toString())
            Toast.makeText(this@MainActivity,"Ocurrió un error. Por favor, inténtalo de nuevo.",
                Toast.LENGTH_LONG).show()
        }
    }

    // Abrir pantalla de registro
    private fun register(){

        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun saveCredentials(id:String) {

        val txtEmail = findViewById<TextView>(R.id.txtEmail_Login)
        val txtPassword = findViewById<TextView>(R.id.txtPassword_Login)

        val strEmail = txtEmail.text.toString()
        val strPassword = txtPassword.text.toString()

        val credential: Credential = Credential()
        credential.id =  id!!
        credential.email =  strEmail!!
        credential.password =  strPassword!!
        //credential.token =  token!!

        UserApplication.prefs.saveCredentials(credential)
    }

}