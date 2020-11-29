package com.biblioteca.biblioteca

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.biblioteca.biblioteca.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        setup()
    }

    private fun setup()
    {
        title = "Registro de Usuario"

        binding.registerButton.setOnClickListener{
            val emailText = binding.emailRegisterEditText.text
            val userNameText = binding.userNameEditText.text
            val passwordText = binding.passwordRegisterEditText.text

            if(emailText.isNotEmpty() && userNameText.isNotEmpty() && passwordText.isNotEmpty()){
                var userUsed = true

                db.collection("users")
                        .whereEqualTo("userName", userNameText.toString())
                        .get()
                        .addOnSuccessListener { documents ->
                            userUsed = !documents.isEmpty
                            if(!userUsed) {
                                //No en uso -> Registrar usuario
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                                        emailText.toString(),
                                        passwordText.toString()
                                ).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        //agregar usuario a base de datos
                                        insertUserToDB(emailText.toString(), userNameText.toString())
                                        showHome(emailText.toString(), userNameText.toString())
                                    } else {
                                        Log.w("TAG", "NO SE REGISTRO CORRECTAMENTE: $userUsed")
                                        showAlert("EMAIL YA REGISTRADO")
                                    }
                                }
                            }
                            else {
                                Log.w("TAG", "USUARIO YA EN USO")
                                showAlert("USUARIO YA EN USO")
                            }
                        }
                        .addOnFailureListener{ e -> Log.w("TAG", "ERROR BUSCANDO USUARIO", e) }
            }
            else{
                Log.w("TAG", "FALTAN CAMPOS OBLIGATORIOS")
                showAlert("COMPLETAR TODOS LOS CAMPOS")
            }
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
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("userName", userName)
        }

        startActivity(homeIntent)
    }

    private fun insertUserToDB(email: String, userName: String){

        val data = hashMapOf(
                "userName" to userName,
                "email" to email
        )

        db.collection("users").document(userName)
            .set(data)
                .addOnSuccessListener { Log.d("TAG", "USUARIO $userName AGREGADO CORRECTAMENTE") }
                .addOnFailureListener { e -> Log.w("TAG", "ERROR AGREGANDO USUARIO", e)}
    }
}