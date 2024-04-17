package com.example.android.userapp

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions

class CustomAdapter(private val dataSet: MutableList<List<String>>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView : TextView
        val imageView : ImageButton
        val textView2 : TextView

        init {
            imageView = view.findViewById(R.id.item_imageView)
            textView = view.findViewById(R.id.item_textView)
            textView2 = view.findViewById(R.id.textViewDetails)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container,
            parent, false)

        d("Recycler", "onCreateViewHolder")

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        d("Recycler", "onBindViewHolder")
        // tänne glidellä bindataan firebase imaget ja ml tekstit
        // holder.
        //Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/riista-kamera.appspot.com/o/-MMKaPwfz4CQN27kovuY?alt=media").into(imageView)
        //holder.imageView
        //var listaNum = ListaNimille.toimivaUrlLista[position]
        //Glide.with(holder.imageView).load(ListaNimille.toimivaUrlLista).into(holder.imageView)
        holder.textView.text =""
        holder.textView2.text =""
        holder.itemView.apply {

            // position toimii tämän sisällä

            Glide.with(this).load(ListaNimille.toimivaUrlLista.asReversed()[position]).placeholder(R.drawable.loading_gif).into(holder.imageView)
            // testi stringi textviewlle -- holder.textView.text = ListaNimille.toimivaUrlLista[position].toString()

            // tällä napilla haetaan labeler
            holder.imageView.setOnClickListener {
                d("Recycler", "nappi ${ListaNimille.toimivaUrlLista.asReversed()[position]}")

                SwitchState.unknownTest = true
                var myBitMap : Bitmap = holder.imageView.drawable.toBitmap()
                d("labeleri", "${myBitMap.toString()}")

                val image = FirebaseVisionImage.fromBitmap(myBitMap)

                //  käytetään options versiota
                // val labeler = FirebaseVision.getInstance().getCloudImageLabeler()

                // Configure options
                // Or, to set the minimum confidence required:
                 val options = FirebaseVisionCloudImageLabelerOptions.Builder()
                     .setConfidenceThreshold(0.65f)
                     .build()
                 val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)

                labeler.processImage(image)
                        .addOnSuccessListener { labels ->

                            d("labeleri", "toimi")

                           // var loopNum : Int = 0

                            for (label in labels) {
                                //loopNum++
                                val myText = label.text
                                when (myText) {

                                    "Bear" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString()}"
                                        SwitchState.unknownTest = false
                                        d("unknown", "${SwitchState.unknownTest}")
                                    }
                                    "Bird" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Squirrel" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Rat" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Dog" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Cat" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Reindeer" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Horse" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Cow" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                    "Rabbit" -> {
                                        holder.textView.text = myText
                                        holder.textView2.text = "Confidence is: ${label.confidence.toString().substring(0, 5)}"
                                        SwitchState.unknownTest = false
                                    }
                                }
                                //if (loopNum == 1) {
                                    //holder.textView.text = myText
                                //}
                                val entityId = label.entityId
                                val confidence = label.confidence
                                d("labeleri", "label = $myText")
                                //d("labeleri", "entityId = $entityId")
                                //d("labeleri", "confidence = $confidence")

                                // täällä jos asettaa tektiksi se ottaa viimeisen loopista
                                //holder.textView2.text = myText
                            }
                        }
                        .addOnFailureListener { e ->
                            d("labeleri", "ei toimi")

                        }
                if (SwitchState.unknownTest)
            {
                d("unknown", "checki sisällä $SwitchState.unknownTest")
                holder.textView.text = "Unknown"
            }
            }
        }

        // tähän tarvis tehdä imageviewsta bitmap
    }

    override fun getItemCount() = ListaNimille.toimivaUrlLista.size//dataSet.size
    }