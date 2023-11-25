package com.example.cuackeat
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.cuackeat.Models.Credential
import com.example.cuackeat.Models.UserApplication
import com.google.android.material.navigation.NavigationView

class HomeActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnFragmentActionsListener {

    //Manejar de forma global nuesto drawer
    private lateinit var drawer: DrawerLayout
    //Configura el icono de la aplicación ubicado a la izquierda de la barra de acciones o la barra de herramientas
    //para abrir y cerrar el cajón de navegación
    private lateinit var toggle: ActionBarDrawerToggle
    //Indice de nuestra seleccion
    private var intSelection:Int =  0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbarHome = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbarHome)

        // Cambiar el título de la barra de herramientas
        toolbarHome.setTitle("Inicio")

        // Cargar el fragmento de inicio
        loadFragment(HomeFragment())

        this.drawer = findViewById(R.id.drawer_layout)
        //Es el icono de hamburguesa
        //El contexto, drawlayout, toolbar, accesibilidad texto cuando abre, accesibilidad texto cuando cierra
        this.toggle = ActionBarDrawerToggle(this, drawer, toolbarHome, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        //conectamos nuestros toogle con el navigation draw
        this.drawer.addDrawerListener(toggle)
        //lo habilitamos para la navegación "ascendente" a través de
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Habilitamos el icono de la aplicación a través
        this.supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        //Para capturar el evento click de las opciones de menu
        navigationView.setNavigationItemSelectedListener(this)

        // Obtener el TextView del encabezado y cambiar su texto
        val srtEmail = UserApplication.prefs.getCredentials().email
        val navHeaderTextView: TextView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_textView)
        navHeaderTextView.text = srtEmail
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_item_home->{

                val toolbarHome = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHome)
                toolbarHome.setTitle("Inicio")
                this.intSelection =  0

                // Cargar el fragmento de las Reviews
                replaceFragment(HomeFragment())
            }
            R.id.nav_item_discover->{

                val toolbarHome = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHome)
                toolbarHome.setTitle("Descubre")
                this.intSelection =  1

                // Cargar el fragmento de los Restaurantes
                replaceFragment(DiscoverFragment())
            }
            R.id.nav_item_profile->{

                val toolbarHome = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHome)
                toolbarHome.setTitle("Perfil")
                this.intSelection =  2

                // Cargar el fragmento del perfil
                replaceFragment(ProfileFragment())
            }
            R.id.nav_item_settings->{

                val toolbarHome = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHome)
                toolbarHome.setTitle("Opciones")
                this.intSelection =  2

                // Cargar el fragmento de las opciones
                replaceFragment(SettingsFragment())
            }
        }

        //Cierra las opciones de menu
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }

        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //toggle.onConfigurationChanged(newConfig)
        Toast.makeText(this, "onConfigurationChanged", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }
    }

    // Cargar un fragmento
    private fun loadFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()
    }

    // Reemplazar el fragmento
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    // Implementamos el evento OnClick declarado en la Interfaz OnFragmentActionsListener
    override fun onClickProfileFragmentButton(){
        // Redirigir a la pantalla de editar perfil
        val intent = Intent(this@HomeActivity, EditProfileActivity::class.java)
        intent.putExtra("id", UserApplication.prefs.getCredentials().id)
        startActivity(intent)
    }

    override fun onClickSettingsChangePasswordFragmentButton(){
        // Redirigir a la pantalla de cambiar contraseña
        val intent = Intent(this@HomeActivity, EditPasswordActivity::class.java)
        startActivity(intent)
    }

    override fun onClickSettingsLogoutFragmentButton(){
        logout()
    }

    // Cerrar sesión
    private fun logout() {

        val credential: Credential = Credential()
        credential.id = ""
        credential.email = ""
        credential.password = ""
        //credential.token =  ""

        // Limpiar las credenciales en las SharedPrefs
        UserApplication.prefs.saveCredentials(credential)

        // Redirigir al Login
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}