package com.example.cuackeat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(), View.OnClickListener {

    private var listener: OnFragmentActionsListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.activity_setting, container, false)

        val btnChangePassword = root.findViewById<Button>(R.id.btnChangePassword_Settings)
        btnChangePassword.setOnClickListener(this)

        val btnLogout = root.findViewById<Button>(R.id.btnLogout_Settings)
        btnLogout.setOnClickListener(this)

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnFragmentActionsListener){
            this.listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

    // Declaramos el evento onClick, mismo que aparece en la interfaz OnFragmentActionsListener
    // e implementamos en la clase HomeActivity
    override fun onClick(v: View?){
        when(v!!.id){
            R.id.btnChangePassword_Settings -> this.listener?.onClickSettingsChangePasswordFragmentButton()
            R.id.btnLogout_Settings -> this.listener?.onClickSettingsLogoutFragmentButton()
        }
    }

}