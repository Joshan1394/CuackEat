package com.example.cuackeat.Models

import android.content.Context

class Prefs (val context: Context){
    //Constantes
    val SHARED_NAME = "USERPREFERENCES" //<--PONER EL NOMBRE QUE MEJOR LES PARESCA


    //EL MANEJADOR DE LAS SHARED PREFERENCES
    //VAMOS A DEFINIR EL NOMBRE ARCHIVO XML QUE SE VA A GUARDAR EN NUESTRA CARPETA SHARED_PREFS
    //Y TAMBIEN DEFINIMOS QUIEN PUEDE TENER ACCESO A ES XML EN ESTA CASO ES PRIVADO
    val managerPrefs =  context.getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE)

    //FUNCION QUE NOS PERMITE GUARDAR LAS CREDENCIALES
    fun saveCredentials(credential:Credential){
        var editor =  managerPrefs.edit()
        editor.putString("id",credential.id)
        editor.putString("email",credential.email)
        editor.putString("password",credential.password)
        // editor.putString("token",credential.token)
        editor.commit()
    }

    //FUNCION QUE NOS PERMITE GUARDAR EL TOKEN
    //fun saveToken(credential:Credential){
    //    var editor =  managerPrefs.edit()
    //    editor.putString("token",credential.token)
    //    editor.commit()
    //}

    //FUNCION QUE PERMITE RECUPERAR LAS CREDENCIALES
    fun getCredentials():Credential{

        val credential:Credential =  Credential()
        //ENCASO DE QUE NO HAYA DATOS REGRESA UN VALOR POR DEFAULT
        val strId: String? =  managerPrefs.getString("id","0")
        val strEmail: String? =  managerPrefs.getString("email","")
        val strPassword:String? =  managerPrefs.getString("password", "")
        //val strToken:String? =  managerPrefs.getString("token", "")

        credential.id = strId!!
        credential.email = strEmail!!
        credential.password = strPassword!!
        //credential.token = strToken!!

        return credential
    }
}