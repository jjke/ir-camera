package com.example.android.userapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Log.d
import android.view.Gravity
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.Strategy.P2P_CLUSTER
import com.google.android.material.circularreveal.CircularRevealHelper.STRATEGY
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class StartScreen : AppCompatActivity() {

    private val REQUIRED_PERMISSIONS = arrayOf(
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_FINE_LOCATION"
    )

    private val REQUEST_CODE_PERMISSIONS = 1001

    lateinit var endID : String

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.startscreen_layout)

        if (allPermissionsGranted()) {
            d("Nearby", "Permissions OK")
            startDiscovery()
        } else {
            d("Nearby", "No Permissions")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val imgButtonSuomi : ImageButton = findViewById(R.id.imgButtonSuomi)

        val storageReference = Firebase.storage.reference
        d("Start", "storageref")

        val gsReference = Firebase.storage.getReferenceFromUrl("gs://riista-kamera.appspot.com/-GuEULs5G4SWIEnekTjE")
        d("Start", "ebin :DDDD $gsReference")

        //Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/riista-kamera.appspot.com/o/-MMKaPwfz4CQN27kovuY?alt=media").into(imageView)

        val storage = Firebase.storage
        val listRef = storage.reference//.child("files/uid")

        imgButtonSuomi.setOnClickListener {
            d("Start", "Suomi painettu")
            //setContentView(R.layout.menu_suomi_layout)
            startActivity(Intent(this@StartScreen, MenuSuomi::class.java))
            KieliSuomi.onkoSuomi = true
    }

    val imgButtonRuotsi : ImageButton = findViewById(R.id.imgButtonRuotsi)
    imgButtonRuotsi.setOnClickListener {
            d("Start", "Ruotsi painettu")

        startActivity(Intent(this@StartScreen, MenuSuomi::class.java))
        //d("kieli", getString(R.string.ohje).toString())
        KieliSuomi.onkoSuomi = false

    }

        val switchButtonCamera : Switch = findViewById(R.id.switchCameraEnabler)
        switchButtonCamera.setOnCheckedChangeListener { buttonView, isChecked ->
            d("switch", "switch changed")
            var checkState : Boolean = switchButtonCamera.isChecked()
            SwitchState.switchStateBool = checkState
            d("switch", "switch is $checkState")
            d("switch", "object switch bool is : ${SwitchState.switchStateBool}")
            sendPayload(endID)
            Toast.makeText(this, "Camera capture state changed", Toast.LENGTH_SHORT).show()
        }


    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {

            }
        }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()

        Nearby.getConnectionsClient(this).startDiscovery(
                "1234", endpointDiscoveryCallback, discoveryOptions
        )
                .addOnSuccessListener {
                    a : Void? -> d("Nearby", "addOnSuccessListener")
                }
                .addOnFailureListener {
                    e : Exception -> d("Nearby", "addOnFailureListener")
                }
    }


    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(p0: String, p1: DiscoveredEndpointInfo) {
            d("Nearby", "onEndpointFound")

            Nearby.getConnectionsClient(this@StartScreen).requestConnection("Laite", p0, connectionLifecycleCallback)
                    .addOnSuccessListener {
                        a : Void? ->  d("Nearby", "endpointDiscoveryCallback - addOnSuccessListener")
                    }
                    .addOnFailureListener {
                        e : Exception? -> d("Nearby", "endpointDiscoveryCallback - addOnSuccessListener")
                    }
        }

        override fun onEndpointLost(p0: String) {
            d("Nearby", "onEndpointLost")
        }

    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            d("Nearby", "onConnectionInitiated")
            Nearby.getConnectionsClient(this@StartScreen).acceptConnection(p0, payloadCallback)
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            d("Nearby", "onConnectionResult")

            when(p1.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    d("Nearby", "STATUS_OK")
                    endID = p0
                    sendPayload(p0)

                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    d("Nearby", "STATUS_CONNECTION_REJECTED")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    d("Nearby", "STATUS_ERROR")
                }
            }
        }

        override fun onDisconnected(p0: String) {
            d("Nearby", "onDisconnected")
        }

    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            d("Nearby", "onPayloadReceived")
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            d("Nearby", "onPayloadTransferUpdate")
        }

    }

    private fun sendPayload(endId : String) {

        var message : String = "DisableCamera"
        if (SwitchState.switchStateBool) {
            message = "EnableCamera"
        }
        d("Nearby", "nearby message is: $message")
        val bytesPayload = Payload.fromBytes(message.toByteArray())
        Nearby.getConnectionsClient(this).sendPayload(endId, bytesPayload)
                .addOnSuccessListener {
                    d("Nearby", "sendPayLoad - addOnSuccessListener")
                }
                .addOnFailureListener {
                    d("Nearby", "sendPayLoad - addOnSuccessListener")
                }
    }

    fun EnableCamera(endId : String) {
        var message : String = "EnableCamera"
        val bytesPayload = Payload.fromBytes(message.toByteArray())
        Nearby.getConnectionsClient(this).sendPayload(endId, bytesPayload)
            .addOnSuccessListener {
                d("Nearby", "sendPayLoad - addOnSuccessListener")
            }
            .addOnFailureListener {
                d("Nearby", "sendPayLoad - addOnSuccessListener")
            }
    }

    fun DisableCamera(endId : String) {
        var message : String = "DisableCamera"
        val bytesPayload = Payload.fromBytes(message.toByteArray())
        Nearby.getConnectionsClient(this).sendPayload(endId, bytesPayload)
            .addOnSuccessListener {
                d("Nearby", "sendPayLoad - addOnSuccessListener")
            }
            .addOnFailureListener {
                d("Nearby", "sendPayLoad - addOnSuccessListener")
            }

    }
}
