package com.biblioteca.biblioteca

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private val activityFrom: AppCompatActivity, private val userName: String, private var idList: MutableList<String>, private var titulos: MutableList<String>, private var autores: MutableList<String>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){

    var listIds: MutableList<String> = idList.toMutableList()
    var listTitulos: MutableList<String> = titulos.toMutableList()
    var listAutores: MutableList<String> = autores.toMutableList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val itemTitle: TextView = itemView.findViewById(R.id.tituloTextView)
        val itemDetail: TextView = itemView.findViewById(R.id.descriptionTextView)

        init{
            itemView.setOnClickListener{
                val position: Int = adapterPosition

                showInfo(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listTitulos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(titulos.isNotEmpty()) {
            holder.itemTitle.text = listTitulos[position]
            holder.itemDetail.text = listAutores[position]
        }
    }

    private fun showInfo(position: Int){

        val infoIntent = Intent(activityFrom, InfoLibroActivity::class.java).apply{
            putExtra("userName", userName)
            putExtra("titulo", listTitulos[position])
            putExtra("autor", listAutores[position])
            putExtra("id", listIds[position])
        }

        activityFrom.startActivity(infoIntent)
    }

    public fun newValues(newIds: MutableList<String>, newTitulos: MutableList<String>, newAutores: MutableList<String>){
        listTitulos.clear()
        listAutores.clear()
        listIds.clear()

        listTitulos = newTitulos
        listAutores = newAutores
        listIds = newIds

        notifyDataSetChanged()
    }
}