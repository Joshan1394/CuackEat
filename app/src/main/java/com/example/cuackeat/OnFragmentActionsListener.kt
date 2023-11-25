package com.example.cuackeat

import android.util.Log
import android.widget.Toast

interface OnFragmentActionsListener {

    // Aqui se declaran los eventos onClick de los botones dentro de los fragmentos
    // Se implementan en la clase HomeActivity

    fun onClickProfileFragmentButton(){
    }

    fun onClickSettingsChangePasswordFragmentButton(){
    }

    fun onClickSettingsLogoutFragmentButton(){
    }

}