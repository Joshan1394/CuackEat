package com.example.cuackeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cuackeat.Models.Credential
import com.example.cuackeat.Models.ResponseUser
import com.example.cuackeat.Models.UserApplication
import com.example.cuackeat.Models.UserPassword
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPasswordActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pass)

        val btnSave = findViewById<Button>(R.id.btnSave_EditPassword)
        btnSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnSave_EditPassword-> updateUserPassword()
        }
    }

    // Cambiar la contraseña del usuario
    private fun updateUserPassword(){
        try{

            val txtOldPassword = findViewById<TextView>(R.id.txtOldPassword_EditPassword)
            val txtNewPassword = findViewById<TextView>(R.id.txtNewPassword_EditPassword)
            val txtNewPasswordConfirmation = findViewById<TextView>(R.id.txtConfirmNewPassword_EditPassword)

            val oldPassword = txtOldPassword.text.toString()
            val newPassword = txtNewPassword.text.toString()
            val newPasswordConfirmation = txtNewPasswordConfirmation.text.toString()

            // Aquí se deberá obtener el ID del usuario que inició sesión
            var userId = UserApplication.prefs.getCredentials().id

            // Si el usuario no está autenticado, regresar al Login
            if(userId.equals("0")) {
                val intent = Intent(this@EditPasswordActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                return
            }

            val userPassword = UserPassword(oldPassword,
                newPassword,
                newPasswordConfirmation)

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.updateUserPassword(userId.toNonNegativeInt(0), userPassword)

            result.enqueue(object: Callback<ResponseUser> {

                override fun onFailure(call: Call<ResponseUser>, t: Throwable){
                    Log.e("Failure", t.toString())
                    Toast.makeText(this@EditPasswordActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()
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

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditPassword)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val lblErrorMessage = findViewById<TextView>(R.id.lblErrorMessage_EditPassword)
                            lblErrorMessage.setText("")

                            // Guardar la nueva contraseña en las SharedPrefs
                            saveCredentials()

                            Toast.makeText(this@EditPasswordActivity, "Contraseña actualizada correctamente.", Toast.LENGTH_LONG).show()

                            finish()
                        }
                    }
                }
            })

        }catch(e: Exception){
            Log.e("Failure", e.toString())
            Toast.makeText(this@EditPasswordActivity, "Ocurrió un error. Por favor, inténtalo de nuevo.", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveCredentials() {

        val strUserId = UserApplication.prefs.getCredentials().id
        val strEmail = UserApplication.prefs.getCredentials().email
        val password = findViewById<TextView>(R.id.txtOldPassword_EditPassword)
        val strPassword = password.text.toString()

        val credential: Credential = Credential()
        credential.id =  strUserId!!
        credential.email =  strEmail!!
        credential.password =  strPassword!!

        UserApplication.prefs.saveCredentials(credential)
    }
}