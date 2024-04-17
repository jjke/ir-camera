package com.example.android.userapp

import android.os.Bundle
import android.util.Log.d
import android.util.Log.v
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FirebaseImages : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firebase_images)



        v("firebase", "galleria avattu")

        //printList()

        var urls = listOf<String>()
        var toimivaUrl = listOf<String>()

        urls = ListaNimille.imageNimiLista

        d("urls", "urls muuttuja lista: ${urls}")
        d("urls", "size of urls lista: ${urls.size}")

        urls.forEach {
            d("urls", it.toString())

            // Toimiva url formaatti: https://firebasestorage.googleapis.com/v0/b/riista-kamera.appspot.com/o   TÄHÄN NIMI  ?alt=media
            toimivaUrl += ("https://firebasestorage.googleapis.com/v0/b/riista-kamera.appspot.com/o" + it + "?alt=media")
        }

        ListaNimille.toimivaUrlLista = toimivaUrl
        d("Toimiva", "toimiva url lista koko on: ${ListaNimille.toimivaUrlLista.size}")
        ListaNimille.toimivaUrlLista.asReversed().forEach {
            d("Toimiva", it.toString())
        }

        // printti logi toimille urleille
        toimivaUrl.asReversed().forEach {
            d("urls", it.toString())
        }

        var käyttölista = mutableListOf(ListaNimille.toimivaUrlLista)
        val adapter = CustomAdapter(käyttölista)
        var käyttöRv = findViewById<RecyclerView>(R.id.recycler_view)

        käyttöRv.adapter = adapter
        käyttöRv.layoutManager = LinearLayoutManager(this)

        /*var urlNames = listOf<String>()

        // tyhjennä objecti
        ListaNimille.imageNimiLista.clear()
        //d("Lista", "ListaNimille koko: ${ListaNimille.imageNimiLista.size}")

        d("Lista", "init url koko: ${urlNames.size}")
        val storageReference = Firebase.storage.reference
        d("Start", "storageref")


        val gsReference =
            Firebase.storage.getReferenceFromUrl("gs://riista-kamera.appspot.com/-GuEULs5G4SWIEnekTjE")
        d("Start", "ebin :DDDD $gsReference")

        val storage = Firebase.storage
        val listRef = storage.reference//.child("files/uid")

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
        d("Lista", "url koko: $urlNames")   */
    }

    fun printList() {
        d("Lista", "urlnames in firebaseimages: ${ListaNimille.imageNimiLista}")

    }

}