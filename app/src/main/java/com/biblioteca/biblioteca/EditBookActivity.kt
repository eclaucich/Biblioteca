package com.biblioteca.biblioteca

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.biblioteca.biblioteca.databinding.ActivityEditBookBinding
import com.biblioteca.biblioteca.databinding.ActivityHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBookBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEditBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bundle: Bundle? = intent.extras
        val libroId: String? = bundle?.getString("idLibro")

        setup(libroId ?:"")
    }

    private fun setup(libroId: String){

        title = "Editar libro"

        db.collection("libros")
                .document(libroId)
                .get()
                .addOnSuccessListener { libro ->
                    binding.tituloEditText.setText(libro.get("titulo").toString(), TextView.BufferType.EDITABLE)
                    binding.autorEditText.setText(libro.get("autor").toString(), TextView.BufferType.EDITABLE)
                    binding.estanteEditText.setText(libro.get("estante").toString(), TextView.BufferType.EDITABLE)
                    binding.hojasEditText.setText(libro.get("hojas").toString(), TextView.BufferType.EDITABLE)
                }

        binding.confirmEditButton.setOnClickListener{
            if(binding.tituloEditText.text.isNotEmpty() && binding.autorEditText.text.isNotEmpty() && binding.estanteEditText.text.isNotEmpty()){
                var cantHojas: String = "-"
                if(binding.hojasEditText.text.isNotEmpty())
                    cantHojas = binding.hojasEditText.text.toString()

                val bookUpdates = hashMapOf(
                        "titulo" to binding.tituloEditText.text.toString(),
                        "autor" to binding.autorEditText.text.toString(),
                        "estante" to binding.estanteEditText.text.toString(),
                        "hojas" to cantHojas
                )

                db.collection("libros")
                        .document(libroId)
                        .get()
                        .addOnSuccessListener { libro ->

                            libro.reference.update(bookUpdates as Map<String, Any>)
                            showAlert("EXITO", "Libro editado correctamente. \n Los cambios se verán reflejados en el próximo inicio de la aplicación")

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