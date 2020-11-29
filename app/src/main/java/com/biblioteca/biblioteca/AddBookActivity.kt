package com.biblioteca.biblioteca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.biblioteca.biblioteca.databinding.ActivityAddBookBinding
import com.biblioteca.biblioteca.databinding.ActivityUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        setup()
    }

    private fun setup(){

        title = "Agregar libro"

        val tituloText = binding.tituloEditText.text
        val autorText = binding.autorEditText.text
        val estanteText = binding.estanteEditText.text
        val hojasText = binding.hojasEditText.text

        binding.addBookButton.setOnClickListener{
            if(tituloText.isNotEmpty() && autorText.isNotEmpty() && estanteText.isNotEmpty()){
                var cantHojas: String = "-"
                if(hojasText.isNotEmpty())
                    cantHojas = hojasText.toString()

                val newBook = hashMapOf(
                    "titulo" to tituloText.toString(),
                    "autor" to autorText.toString(),
                    "estante" to estanteText.toString(),
                    "hojas" to cantHojas
                )

                db.collection("libros")
                    .add(newBook)
                    .addOnSuccessListener {
                        showAlert("EXITO", "Libro añadido correctamente")

                        binding.tituloEditText.text.clear()
                        binding.autorEditText.text.clear()
                        binding.estanteEditText.text.clear()
                        binding.hojasEditText.text.clear()
                    }
                    .addOnFailureListener{
                        showAlert("ERROR", "Error al añadir el libro. \n Intenta nuevamente")
                    }
            }
            else{
                showAlert("ERROR", "Completar campos obligatorios")
            }
        }
    }

    private fun showAlert(title: String, msg: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}