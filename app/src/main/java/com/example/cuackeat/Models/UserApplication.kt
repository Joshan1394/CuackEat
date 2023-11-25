package com.example.cuackeat.Models

import android.app.Application

//AL EXTENDER DE APPLICATION NOS PERMITE HACER AL CONTEXTO GENERAL DE LA APLICACIÓN
//ESTA CLASE LA VAMOS A EJECUTAR AL PRINCIPIO CUANDO SE CARGA LA APLICACION POR PRIMERA VEZ
//PARA LO CUAL HAY QUE AGREGARLA AL ANDROID_MANIFEST(IR)
class UserApplication : Application() {

    //NOS VA PERMITIR QUE LO QUE ESTE AQUÍ DENTRO ACCEDER DESDE CUALQUIER PARTE
    companion object{
        //ESTAMOS DECLARANDO UNA VARIABLE QUE VAMOS A INSTANCIAR DESPUES
        lateinit var prefs:Prefs // ESTAMOS USANDO EL PATRON SINGLETON
    }

    override fun onCreate() {
        super.onCreate()
        //LE PASAMOS EL CONTEXTO GENERAL DE LA APLICACIÓN
        prefs = Prefs(applicationContext)
    }
}