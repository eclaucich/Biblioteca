package com.biblioteca.biblioteca

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.biblioteca.biblioteca.databinding.ActivityAuthBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completada")
        analytics.logEvent("InitScreen", bundle)

        // Setup
        setup()

        // Verificar si hay credenciales guardadas
        session()
    }

    override fun onStart() {
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
    }

    private fun setup(){

        title = "Autenticación"

        // Boton de registro
        binding.signUpButton.setOnClickListener{
            showRegister()
        }

        // Boton de ingreso con credenciales
        binding.logInButton.setOnClickListener{
            val emailText = binding.emailEditText.text
            if(emailText.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            //buscar el userName a partir del email
                            var userName: String = ""
                            db.collection("users")
                                .get()
                                .addOnSuccessListener { documents ->
                                    for(doc in documents){
                                        if(doc.get("email") == binding.emailEditText.text.toString()) {
                                            userName = doc.get("userName").toString()
                                            Log.w("TAG", "USERNAME: $userName")
                                            break
                                        }
                                    }
                                    val email = binding.emailEditText.text.toString()
                                    showHome(email, userName)
                                }
                        }
                        else{
                            showAlert("EMAIL Y/O CONTRASEÑA INCORRECTOS")
                        }
                }
            }
        }

    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.auth_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val userName = prefs.getString("userName", null)

        if(email != null && userName != null){
            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, userName)
        }
    }

    private fun showAlert(msg: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, userName: String){
        Log.w("TAG", "SHOW HOME: $userName")

        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("userName", userName)
        }

        startActivity(homeIntent)
    }

    private fun showRegister(){
        val registerIntent = Intent(this, RegisterActivity::class.java).apply {}

        startActivity(registerIntent)
    }
}