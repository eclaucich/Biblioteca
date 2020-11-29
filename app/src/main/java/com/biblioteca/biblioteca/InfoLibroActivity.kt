package com.biblioteca.biblioteca

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import com.biblioteca.biblioteca.databinding.ActivityInfoLibroBinding
import com.google.firebase.firestore.FirebaseFirestore

class InfoLibroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoLibroBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityInfoLibroBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        // Obtener variables de la pantalla home
        val bundle: Bundle? = intent.extras
        val user: String? = bundle?.getString("userName")
        val titulo: String? = bundle?.getString("titulo")
        val autor: String? = bundle?.getString("autor")
        val idLibro: String? = bundle?.getString("id")

        setup(user ?:"", idLibro ?:"", titulo ?:"NULL", autor ?:"NULL")
    }

    private fun setup(user: String, idLibro: String, titulo: String, autor: String){

        title = "Info Libro"

        binding.tituloInfoTextView.text = titulo
        binding.autorInfoTextView.text = autor
        binding.devolverLibroButton.isEnabled = false


        db.collection("libros")
            .document(idLibro)
            .get()
            .addOnSuccessListener { doc ->
                var hojas = doc["hojas"].toString()
                var estante = doc["estante"].toString()
                var poseedor = doc["poseedor"].toString()

                binding.hojasInfoTextView.text = hojas
                binding.estanteInfoTextView.text = estante
                if(poseedor == "") {
                    binding.poseedorInfoTextView.text = "-"
                    binding.retirarLibroButton.isEnabled = true
                }
                else {
                    binding.poseedorInfoTextView.text = poseedor
                    binding.retirarLibroButton.isEnabled = false
                    if(poseedor == user){
                        binding.devolverLibroButton.isEnabled = true
                    }
                }
            }

        binding.retirarLibroButton.setOnClickListener{
            //Cambiar en la base de datos el poseedor de este libro
            db.collection("libros")
                .document(idLibro)
                .get()
                .addOnSuccessListener {
                    db.collection("users")
                            .whereEqualTo("userName", user)
                            .get()
                            .addOnSuccessListener { documents ->
                                it.reference.update("poseedor", user)

                                for(doc in documents){
                                    if(doc.contains("prestados")){
                                        var prestados: List<String> = doc.data?.get("prestados") as List<String>

                                        Log.w("TAG", "PRESTADOS NO NULO")
                                        var list1: List<String> = listOf(idLibro)
                                        var finalList = concatenate(prestados, list1)

                                        doc.reference.update("prestados", finalList)
                                    }
                                    else{
                                        doc.reference.update("prestados", listOf(idLibro))
                                    }
                                }
                            }
                    binding.poseedorInfoTextView.text = user
                    binding.retirarLibroButton.isEnabled = false
                    binding.devolverLibroButton.isEnabled = true
                }
        }

        binding.devolverLibroButton.setOnClickListener{
            db.collection("libros")
                .document(idLibro)
                .get()
                .addOnSuccessListener { libro ->
                    db.collection("users")
                            .document(user)
                            .get()
                            .addOnSuccessListener { usuario ->
                                val prestados: List<String> = usuario.get("prestados") as List<String>
                                var nuevosPrestados: MutableList<String> = mutableListOf()
                                for(prest in prestados){
                                    if(prest != idLibro)
                                        nuevosPrestados.add(prest)
                                }

                                usuario.reference.update("prestados", nuevosPrestados)
                                libro.reference.update("poseedor", "")

                                binding.poseedorInfoTextView.text = ""
                                binding.devolverLibroButton.isEnabled = false
                                binding.retirarLibroButton.isEnabled = true
                            }
                }
        }

        binding.editarLibroButton.setOnClickListener{
            val editIntent = Intent(this, EditBookActivity::class.java).apply{
                putExtra("idLibro", idLibro)
            }

            startActivity(editIntent)
        }
    }

    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        return listOf(*lists).flatten()
    }
}