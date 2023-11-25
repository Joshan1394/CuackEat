package com.example.cuackeat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.cuackeat.Models.ResponseUser
import com.example.cuackeat.Models.UserApplication
import com.example.cuackeat.Models.UserModel
import com.google.android.material.tabs.TabLayout
import okhttp3.internal.toNonNegativeInt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment(), View.OnClickListener {

    private var listener: OnFragmentActionsListener? = null
    private lateinit var myContext: Context
    // Declarar el TabLayout y ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    //SQLite
    private lateinit var sqliteHelper: SQLiteHelper
    //private var std:UserModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        sqliteHelper = SQLiteHelper(myContext)
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.activity_perfil, container, false)

        // Inicializar el TabLayout y ViewPager
        tabLayout = root.findViewById(R.id.tabLayout_Profile)
        viewPager = root.findViewById(R.id.viewPager_Profile)

        // Configurar el TabLayout con el ViewPager
        tabLayout.setupWithViewPager(viewPager)

        // Crear un adapter para el ViewPager
        val adapter = ViewPagerAdapter(childFragmentManager)

        // Agregar los fragments que quieras mostrar en el ViewPager
        adapter.addFragment(ProfileRestaurantsFragment(), "Restaurantes")
        adapter.addFragment(ProfileReviewsFragment(), "Reseñas")
        //adapter.addFragment(ThirdFragment(), "Third")

        // Establecer el adapter en el ViewPager
        viewPager.adapter = adapter

        val btnEditProfile = root.findViewById<Button>(R.id.btnEditProfile_Profile)
        btnEditProfile.setOnClickListener(this)

        val strUserId = UserApplication.prefs.getCredentials().id
        val intUserId = strUserId.toNonNegativeInt(0)

        if(intUserId == 0){
            Toast.makeText(myContext,"Ocurrió un error al intentar obtener el perfil.",
                Toast.LENGTH_SHORT).show()
            return root
        }

        getUserProfile(intUserId, root)

        return root
    }

    // Clase para el adapter del ViewPager
    internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = ArrayList<Fragment>()
        private val titleList = ArrayList<String>()

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            titleList.add(title)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Obtener el contexto para aplicarlo en el LinearLayoutManager y el AlbumRecyclerAdapter
        this.myContext = context

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
            R.id.btnEditProfile_Profile -> this.listener?.onClickProfileFragmentButton()
        }
    }

    private fun addStudentSQLite(id:Int,name:String, lastName:String, nickname:String, description:String, imagen:String) {
        val std = UserModel(id=id, email="email@correo.com", password = "contrasenia", name=name, lastName = lastName, phone = "0123456789", address="direccionHogar", alias = nickname, description = description, image = imagen)
        val status = sqliteHelper.insertUser(std)
        //Check insert success or not success
        if (status>-1)
        {
            //Toast.makeText(myContext, "Studdent Added", Toast.LENGTH_SHORT).show()
            //clearEditText()
        }else
        {
            Toast.makeText(myContext, "No se ha guardado en SQLITE", Toast.LENGTH_SHORT).show()
        }

    }

    /*Funcion del botón para obtener a los estudiantes*/
    private fun getUser():ArrayList<UserModel> {
        val stdList = sqliteHelper.getAllUsers()
        Log.e("pppp", "${stdList.size}")

        return stdList
        //adapter?.addItems(stdList)
        //
    }

    private fun getUserProfile(id: Int, root: View){
        try {

            val service: Service =  RestEngine.getRestEngine().create(Service::class.java)
            val result: Call<ResponseUser> = service.getUser(id)

            result.enqueue(object: Callback<ResponseUser> {
                override fun onFailure(call: Call<ResponseUser>, t: Throwable) {
                    //Si no se logra conectar a la base de datos por falta de conexión
                    Log.e("FAILED", t.toString())
                    Toast.makeText(myContext,"Ocurrió un error. Por favor, asegurese de estár conectado a internet e inténtalo de nuevo.",
                        Toast.LENGTH_SHORT).show()

                    /*== SE ASIGNA LOS VALORES DE LA BASE DE DATOS EN SU LUGAR ==*/
                    val txtName = root.findViewById<TextView>(R.id.txtName_Profile)
                    val txtNickname = root.findViewById<TextView>(R.id.txtNickname_Profile)
                    val txtDescription = root.findViewById<TextView>(R.id.txtDescription_Profile)
                    val imgProfileImage = root.findViewById<ImageView>(R.id.imgProfileImage_Profile)

                    val stdList= getUser()

                    val strFullName = stdList[0].name +" "+ stdList[0].lastName
                    txtName.setText(strFullName)
                    txtNickname.setText(stdList[0].alias)
                    if(stdList[0].description.equals("null")){
                        txtDescription.setText("")
                    } else {
                        txtDescription.setText(stdList[0].description)
                    }

                    val strImage:String =  stdList[0].image.replace("data:image/png;base64,","")
                    try {
                        val byteArray = Base64.getDecoder().decode(strImage)
                        if(byteArray != null){
                            imgProfileImage!!.setImageBitmap(ImageUtilities.getBitMapFromByteArray(byteArray))
                        }
                    } catch (e: IllegalArgumentException){
                        // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                        val drawable = ContextCompat.getDrawable(myContext, R.drawable.circle)
                        imgProfileImage.setImageDrawable(drawable)

                        Toast.makeText(myContext, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT)
                    }

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

                            val lblErrorMessage = root.findViewById<TextView>(R.id.lblErrorMessage_EditProfile)
                            lblErrorMessage.setText(errorMessage.toString())
                        }

                    } else {
                        val responseUser = response.body()
                        val user = responseUser?.data
                        val status = responseUser?.status

                        if (status == "success"){

                            val txtName = root.findViewById<TextView>(R.id.txtName_Profile)
                            val txtNickname = root.findViewById<TextView>(R.id.txtNickname_Profile)
                            val txtDescription = root.findViewById<TextView>(R.id.txtDescription_Profile)
                            val imgProfileImage = root.findViewById<ImageView>(R.id.imgProfileImage_Profile)

                            // Llenar los campos con los datos del usuario
                            val strFullName = user?.name + " " + user?.last_name
                            txtName.setText(strFullName)
                            //txtLastName.setText(user?.last_name)
                            txtNickname.setText(user?.nickname)
                            txtDescription.setText(user?.description)
                            //strEncodeImage = user?.image

                            val strImage:String =  user?.image.toString().replace("data:image/png;base64,","")
                            try {
                                val byteArray = Base64.getDecoder().decode(strImage)
                                if(byteArray != null){
                                    imgProfileImage!!.setImageBitmap(ImageUtilities.getBitMapFromByteArray(byteArray))
                                }
                            } catch (e: IllegalArgumentException){
                                // Obtener el Drawable desde el recurso de Drawable definido en XML utilizando ContextCompat
                                val drawable = ContextCompat.getDrawable(myContext, R.drawable.circle)
                                imgProfileImage.setImageDrawable(drawable)

                                Toast.makeText(myContext, "Ocurrió un error al obtener la imagen. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT)
                            }

                            //Agregar en la base de datos de usuario
                            sqliteHelper.truncateTable()
                            //var id= user?.id.toString()
                            addStudentSQLite(user?.id!!.toInt(),user?.name.toString(), user?.last_name.toString(), user?.nickname.toString(), user?.description.toString(), user?.image.toString() )
                        }
                    }
                }

            })
        } catch (e: Exception) {
            Log.e("Error", e.toString())
            Toast.makeText(myContext,"Ocurrió un error. Por favor, inténtalo de nuevo.",
                Toast.LENGTH_SHORT).show()
        }
    }

}