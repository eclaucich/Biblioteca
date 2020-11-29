package com.biblioteca.biblioteca

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biblioteca.biblioteca.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType{
    ANONYMOUS,
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val db = FirebaseFirestore.getInstance()

    private var titulosList = mutableListOf<String>()
    private var autoresList = mutableListOf<String>()
    private var idList = mutableListOf<String>()

    private var userId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Obtener variables de la pantalla de autenticacion
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val userName = bundle?.getString("userName")

        binding.searchEditText.visibility = View.INVISIBLE
        binding.tituloCheckBox.visibility = View.INVISIBLE
        binding.autorCheckBox.visibility = View.INVISIBLE

        postToList(userName ?:"")

        // Setup
        setup(email ?: "", userName ?: "")

        // Guardar credenciales
        val prefs = getSharedPreferences(getString(R.string.auth_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("userName", userName)
        prefs.apply()
    }

    private fun addToList(id: String, titulo: String, autor: String){
        titulosList.add(titulo)
        autoresList.add(autor)
        idList.add(id)
    }

    private fun postToList(userName: String){
        // Por cada documento (libro) en la coleccion libros, agregar un boton a la listas
        db.collection("libros")
                .get()
                .addOnSuccessListener { documents ->
                    for(doc in documents){
                        addToList(
                            doc.reference.id,
                            doc.get("titulo").toString(),
                            doc.get("autor").toString()
                        )
                    }

                    binding.librosRecycleView.layoutManager = LinearLayoutManager(this)
                    binding.librosRecycleView.adapter = RecyclerAdapter(this, userName, idList, titulosList, autoresList)
                    binding.loadingTextView.visibility = View.INVISIBLE
                    binding.searchEditText.visibility = View.VISIBLE
                    binding.tituloCheckBox.visibility = View.VISIBLE
                    binding.autorCheckBox.visibility = View.VISIBLE
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            showUser()
            true
        }
        R.id.action_logout -> {
            logOut()
            true
        }
        R.id.action_addBook -> {
            showAddBook()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun setup(email: String, userName: String){

        title = "Biblioteca"

        userId = userName

        binding.tituloCheckBox.isChecked = true
        binding.autorCheckBox.isChecked = true

        binding.searchEditText.doAfterTextChanged{
            filter(it.toString())
        }
    }

    private fun filter(text: String){
        var filteredTitulos: MutableList<String> = mutableListOf()
        var filteredAutores: MutableList<String> = mutableListOf()
        var filteredIds: MutableList<String> = mutableListOf()

        for(i in titulosList.indices){
            if(binding.tituloCheckBox.isChecked && !binding.autorCheckBox.isChecked){
                if(titulosList[i].toLowerCase().contains(text.toLowerCase())){
                    filteredTitulos.add(titulosList[i])
                    filteredAutores.add(autoresList[i])
                    filteredIds.add(idList[i])
                }
            }
            else if(!binding.tituloCheckBox.isChecked && binding.autorCheckBox.isChecked){
                if(autoresList[i].toLowerCase().contains(text.toLowerCase())){
                    filteredTitulos.add(titulosList[i])
                    filteredAutores.add(autoresList[i])
                    filteredIds.add(idList[i])
                }
            }
            else{
                if(titulosList[i].toLowerCase().contains(text.toLowerCase()) || autoresList[i].toLowerCase().contains(text.toLowerCase())){
                    filteredTitulos.add(titulosList[i])
                    filteredAutores.add(autoresList[i])
                    filteredIds.add(idList[i])
                }
            }
        }
        (binding.librosRecycleView.adapter as RecyclerAdapter).newValues(filteredIds, filteredTitulos, filteredAutores)
    }

    private fun logOut(){
        val prefs = getSharedPreferences(getString(R.string.auth_file), Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()

        FirebaseAuth.getInstance().signOut()

        val authIntent = Intent(this, AuthActivity::class.java).apply {
            putExtra("email", "")
            putExtra("userName", "")
        }

        startActivity(authIntent)
    }

    private fun showUser(){
        val userIntent = Intent(this, UserActivity::class.java).apply {
            putExtra("idUser", userId)
        }

        startActivity(userIntent)
    }

    private fun showAddBook(){
        val addBookIntent = Intent(this, AddBookActivity::class.java)

        startActivity(addBookIntent)
    }
}