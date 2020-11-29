package com.biblioteca.biblioteca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioteca.biblioteca.databinding.ActivityRegisterBinding
import com.biblioteca.biblioteca.databinding.ActivityUserBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        val bundle: Bundle? = intent.extras
        val idUser: String? = bundle?.getString("idUser")

        setup(idUser ?:"")
    }

    private fun setup(idUser: String){

        title = "Mi Usuario"

        Log.w("TAG", "BUSCANDO USUARIO: $idUser")

        binding.sinPrestadosTextView.visibility = View.INVISIBLE

        db.collection("users")
            .document(idUser)
            .get()
            .addOnSuccessListener { user ->
                binding.usuarioTextView.text = user.get("userName").toString()
                binding.emailTextView.text = user.get("email").toString()

                if(user.contains("prestados")){
                    var titulosList = mutableListOf<String>()
                    var autoresList = mutableListOf<String>()
                    var idList = mutableListOf<String>()

                    val prestados = user.get("prestados") as List<String>

                    if(prestados.isEmpty()) {
                        Log.w("TAG", "SIN LIBROS PRESTADOS")
                        binding.sinPrestadosTextView.visibility = View.VISIBLE
                    }

                    for(i in prestados.indices){
                        db.collection("libros")
                            .document(prestados[i])
                            .get()
                            .addOnSuccessListener {
                                idList.add(prestados[i])
                                titulosList.add(it.get("titulo").toString())
                                autoresList.add(it.get("autor").toString())

                                Log.w("TAG", "LIBRO PRESTADO: $i , ${prestados[i]}")

                                if(i == (prestados.size-1)){
                                    Log.w("TAG", "LIBROS PRESTADOS: ${idList.size} | ${titulosList.size} | ${autoresList.size}")

                                    binding.prestadosRecycleView.layoutManager = LinearLayoutManager(this)
                                    binding.prestadosRecycleView.adapter = RecyclerAdapter(this, idUser, idList, titulosList, autoresList)
                                }
                            }
                    }
                }
                else{
                    binding.sinPrestadosTextView.visibility = View.VISIBLE
                }
            }
    }
}