package com.example.controlrecaudos

import android.app.*
import android.os.Bundle
import android.content.*
import android.graphics.Color
import android.view.*
import android.widget.*
import java.text.NumberFormat
import java.util.Locale

data class Recaudo(val cliente:String,val valor:Double,val medio:String)

class MainActivity : Activity() {
    private val recaudos = mutableListOf<Recaudo>()
    private lateinit var total: TextView
    private lateinit var lista: LinearLayout
    private val nf = NumberFormat.getCurrencyInstance(Locale("es","CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mostrarInicio()
    }

    private fun mostrarInicio() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(28,28,28,20)
            setBackgroundColor(Color.rgb(247,249,252))
        }
        val title = TextView(this).apply { text="CONTROL RECAUDOS"; textSize=25f; setTextColor(Color.rgb(20,55,100)); setPadding(0,0,0,18) }
        root.addView(title)
        total = TextView(this).apply { textSize=30f; setTextColor(Color.rgb(20,120,70)); setPadding(0,15,0,25) }
        root.addView(total)
        val btn = Button(this).apply { text="＋ Registrar recaudo"; setOnClickListener { dialogRecaudo() } }
        root.addView(btn)
        val close = Button(this).apply { text="Cierre de caja"; setOnClickListener { cierre() } }
        root.addView(close)
        val label = TextView(this).apply { text="Recaudos de hoy"; textSize=20f; setPadding(0,20,0,10) }
        root.addView(label)
        lista = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(lista)
        setContentView(root)
        actualizar()
    }

    private fun actualizar() {
        val suma = recaudos.sumOf { it.valor }
        total.text = "Total hoy: ${nf.format(suma)}"
        lista.removeAllViews()
        if (recaudos.isEmpty()) {
            lista.addView(TextView(this).apply { text="No hay recaudos registrados."; textSize=16f; setPadding(0,10,0,10) })
        } else recaudos.asReversed().forEach {
            lista.addView(TextView(this).apply {
                text="${it.cliente}\n${nf.format(it.valor)}  •  ${it.medio}"
                textSize=17f; setPadding(0,14,0,14)
            })
        }
    }

    private fun dialogRecaudo() {
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(40,10,40,0) }
        val cliente=EditText(this).apply { hint="Cliente" }
        val valor=EditText(this).apply { hint="Valor"; inputType=2 }
        val medio=Spinner(this)
        medio.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Efectivo","Transferencia","Tarjeta","Otro"))
        box.addView(cliente); box.addView(valor); box.addView(medio)
        AlertDialog.Builder(this).setTitle("Registrar recaudo").setView(box)
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Guardar"){_,_ ->
                val v=valor.text.toString().toDoubleOrNull() ?: 0.0
                if(cliente.text.isNotBlank() && v>0) { recaudos.add(Recaudo(cliente.text.toString(),v,medio.selectedItem.toString())); actualizar() }
            }.show()
    }

    private fun cierre() {
        val esperado=recaudos.sumOf{it.valor}
        val input=EditText(this).apply { hint="Dinero realmente recibido"; inputType=2 }
        AlertDialog.Builder(this).setTitle("Cierre de caja")
            .setMessage("Total esperado: ${nf.format(esperado)}")
            .setView(input).setNegativeButton("Cancelar",null)
            .setPositiveButton("Comparar"){_,_ ->
                val real=input.text.toString().toDoubleOrNull() ?: 0.0
                val dif=real-esperado
                val estado=when { dif==0.0 -> "Cuadre perfecto ✅"; dif>0 -> "Sobrante: ${nf.format(dif)}"; else -> "Faltante: ${nf.format(-dif)} ⚠️" }
                AlertDialog.Builder(this).setTitle("Resultado").setMessage(estado).setPositiveButton("OK",null).show()
            }.show()
    }
}
