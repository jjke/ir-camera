package com.example.android.userapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log.d
import android.util.Log.v
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage

class MenuSuomi : AppCompatActivity() {

    val gsReference =
            Firebase.storage.getReferenceFromUrl("gs://riista-kamera.appspot.com/-GuEULs5G4SWIEnekTjE")

    val storage = Firebase.storage
    val listRef = storage.reference//.child("files/uid")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_suomi_layout)

        val ohjeFragment = OhjeFragment()
        val ohjeFragmentRuotsi = OhjeFragmentRuotsi()
        d("start", "oncreate")

        clearList()
        loadToList()

        d("kieli", KieliSuomi.onkoSuomi.toString())
        d("Lista", "listan koko functio kutsulla: ${ListaNimille.imageNimiLista}")

        val ohjeButton : Button = findViewById(R.id.buttonOhje)
        val ohjeTeksti : TextView = findViewById(R.id.ohjeTextView)
        if (KieliSuomi.onkoSuomi == false) {
            ohjeButton.text = "hjälp"
            ohjeTeksti.text = "Vad vill du göra?"
        }
        ohjeButton.setOnClickListener {
            if (KieliSuomi.onkoSuomi == true) {
                supportFragmentManager.beginTransaction().apply {
                    d("start", "ohje painettu")
                    replace(R.id.galleriafragment, ohjeFragment)
                    addToBackStack(null)
                    commit()
                }
            } else {
                supportFragmentManager.beginTransaction().apply {
                    d("start", "ohje painettu")
                    replace(R.id.galleriafragment, ohjeFragmentRuotsi)
                    addToBackStack(null)
                    commit()
                }
            }
        }


        val galleriaButton : Button = findViewById(R.id.buttonGalleria)
        if (KieliSuomi.onkoSuomi == false) {
            galleriaButton.text = "Galleri"
        }
        galleriaButton.setOnClickListener {
            d("start", "button Galleria hei hei")
            startActivity(Intent(this@MenuSuomi, FirebaseImages::class.java))
            }
        }

    fun loadToList() {
        var urlNames = listOf<String>()

        // tyhjennä objecti
        ListaNimille.imageNimiLista.clear()
        d("Lista", "init url koko: ${urlNames.size}")

        val storageReference = Firebase.storage.reference
        d("Start", "storageref")

        // Listiin nimet ja nimiä manipuloimalla saadan suora glide imageview :DD
        // käytä path
        listRef.listAll()
                .addOnSuccessListener { (items, prefixes) ->
                    prefixes.forEach { prefix ->
                        d("Start", "$prefix")
                        d("Start", "prefixin jälkeinen")
                    }

                    items.forEach { item ->
                        //d("Start", "hei hei")
                        //d("Start", "ITEM ${item}")
                        //d("Start", "PATH ${item.path}")

                        // itemi listaan looppi :D
                        urlNames += item.path
                        ListaNimille.imageNimiLista.add(item.path.toString())
                        d("Lista", "item.path lisätty listaan: ${item.path}")
                        //d("Lista", "${ListaNimille.imageNimiLista.size}")
                    }
                    d("Lista", "addOnSuccessListenerin sisällä: ${ListaNimille.imageNimiLista.size}")

                    // tänne tallennus ennen ku se katoaa
                    d("Lista", "urlnames: $urlNames")
                }
                .addOnFailureListener {
                    d("Start", "addOnFailureListener")
                }
        //d("Lista", "addOnSuccessListener ulkopuolella koko: ${ListaNimille.imageNimiLista.size}")
        d("Lista", "url koko: $urlNames")
    }

    fun clearList() {
        // tyhjennä objecti
        ListaNimille.imageNimiLista.clear()
        d("Lista", "clearList()")
        d("Lista", "ListaNimille koko: ${ListaNimille.imageNimiLista.size}")
    }
}