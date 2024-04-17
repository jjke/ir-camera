package com.example.camera2

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.camera2.DeviceProfile.Companion.CHARACTERISTIC_STATE_UUID
import com.example.camera2.DeviceProfile.Companion.SERVICE_UUID
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions
import com.google.firebase.storage.ktx.storage
import java.util.*

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the PeripheralManager
 * For example, the snippet below will open a GPIO pin and set it to HIGH:
 *
 * val manager = PeripheralManager.getInstance()
 * val gpio = manager.openGpio("BCM6").apply {
 *     setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * }
 * gpio.value = true
 *
 * You can find additional examples on GitHub: https://github.com/androidthings
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = "Sinihammas"
        val BLUETOOTH_REQUEST_CODE = 1
    }

    private val bluetoothAdapter : BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    var bluetoothGatt : BluetoothGatt? = null

    private val REQUIRED_PERMISSIONS = arrayOf(
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.CHANGE_WIFI_STATE",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    private val REQUEST_CODE_PERMISSIONS = 1001

    private lateinit var cameraThread : HandlerThread
    private lateinit var cameraHandler : Handler
    private lateinit var demoCamera : DemoCamera

    private val database = Firebase.database
    private val storage = Firebase.storage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (allPermissionsGranted()) {
            Log.v("Nearby", "Permissions OK")
            startAdvertising()
        } else {
            Log.v("Nearby", "No Permissions")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    override fun onPause() {
        super.onPause()
        demoCamera.shutDownCamera()
        cameraThread.quitSafely()
    }

    override fun onResume() {
        super.onResume()

        if (bluetoothAdapter.isEnabled) {
            StartBLEScan()
        } else {
            Log.v(TAG, "Bluetooth is disabled")
            val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(btIntent, BLUETOOTH_REQUEST_CODE)
        }
        
        cameraThread = HandlerThread("CameraThread")
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)

        demoCamera = DemoCamera(onImageAvailableListener, cameraHandler)
        demoCamera.openCamera(this)
        //demoCamera.takePhoto()

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

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()

        Nearby.getConnectionsClient(this).startAdvertising("Things", "1234", connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener { a : Void? ->
                Log.v("Nearby", "startAdvertising - addOnSuccessListener")
            }
            .addOnFailureListener { e : Exception? ->
                Log.v("Nearby", "startAdvertising - addOnSuccessListener")
            }
    }

    fun StartBLEScan() {
        Log.v(TAG, "StartBLEScan")
        val scanFilter = ScanFilter.Builder().setDeviceName("MyESP32").build()
        val scanFilters:MutableList<ScanFilter> = mutableListOf()
        scanFilters.add(scanFilter)
        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        Log.v(TAG, "Start scan")
        bluetoothAdapter.bluetoothLeScanner.startScan(scanFilters, scanSettings, bleScanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.v(TAG, "connectToDevice")
        bluetoothGatt = device.connectGatt(this, false, bleGattCallback)
    }

    private val bleScanCallback : ScanCallback by lazy {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                //super.onScanResult(callbackType, result)
                Log.v(TAG, "onScanResult")

                val bluetoothDevice = result?.device
                if (bluetoothDevice != null) {
                    Log.v(TAG, "Device name ${bluetoothDevice.name} Device address ${bluetoothDevice.uuids}")
                    connectToDevice(bluetoothDevice)
                }
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            Log.d("Nearby", "onConnectionInitiated")
            Nearby.getConnectionsClient(this@MainActivity).acceptConnection(p0, payloadCallback)
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            Log.d("Nearby", "onConnectionResult")

            when(p1.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("Nearby", "STATUS_OK")


                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("Nearby", "STATUS_CONNECTION_REJECTED")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("Nearby", "STATUS_ERROR")
                }
            }
        }

        override fun onDisconnected(p0: String) {
            Log.d("Nearby", "onDisconnected")
        }

    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(p0: String, p1: Payload) {
            Log.d("Nearby", "onPayloadReceived")

            val receivedBytes: ByteArray? = p1.asBytes()
            var message = receivedBytes?.let { String(it) }

            if (message != null) {
                Log.v("Nearby", message)
            }

            if (message == "EnableCamera") {
                CameraCheck.CameraChecker = true
            }

            if (message == "DisableCamera")
                CameraCheck.CameraChecker = false
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
            Log.d("Nearby", "onPayloadTransferUpdate")
        }

    }

    private val bleGattCallback : BluetoothGattCallback by lazy {
        object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                Log.v(TAG, "onConnectionStateChange")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt?.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                Log.v(TAG, "onServicesDiscovered")

                val service = gatt!!.getService(SERVICE_UUID)
                val characteristic = service.getCharacteristic(CHARACTERISTIC_STATE_UUID)
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                gatt.setCharacteristicNotification(characteristic, true)
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                Log.v(TAG, "onCharacteristicRead")
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                //Log.v(TAG, "onCharacteristicChanged")
                val message = characteristic?.getStringValue(0)
                Log.v(TAG, "$message")
                if (message == "1") {
                    runOnUiThread { demoCamera.takePhoto() }
                }
            }
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        Log.v("Camera2", "onImageAvailableListener")
        val image = reader.acquireLatestImage()
        val imageBuf = image.planes[0].buffer
        val imageBytes = ByteArray(imageBuf.remaining())
        imageBuf.get(imageBytes)
        image.close()
        pictureReady(imageBytes)
    }

    private fun pictureReady(imageBytes : ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0,   imageBytes.size)
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionCloudImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.7f)
        .build()
        val labeler = FirebaseVision.getInstance().getCloudImageLabeler(options)

        //Image labeling
        labeler.processImage(image).addOnSuccessListener { labels ->
            Log.v("Camera2", "addOnSuccessListener")
            for (label in labels) {
                val text = label.text
                val entityId = label.entityId
                val confidence = label.confidence
                Log.v("Vision", "label = $text")
                Log.v("Camera2", "entityId = $entityId")
                Log.v("Vision", "confidence = $confidence")
            }
        }.addOnFailureListener { e ->
            Log.v("Camera2", "addOnFailureListener")
        }


        //val imageView = findViewById<ImageView>(R.id.imageView)
        if (imageBytes != null) {
            val log = database.getReference("logs").push()
            val imageRef = storage.reference.child(log.key.toString())

            if (CameraCheck.CameraChecker) {
            //Upload image to storage
            var uploadTask = imageRef.putBytes(imageBytes)
            uploadTask.addOnFailureListener {
                Log.v("Camera2", "unable to upload image")
                log.removeValue()
            }.addOnSuccessListener { taskSnapshot ->
                val downloadUrl = taskSnapshot.uploadSessionUri

                d("Camera2", CameraCheck.CameraChecker.toString())
                Log.v("Camera2", "Image upload successful")
                log.child("timestamp").setValue(ServerValue.TIMESTAMP)
                log.child("image").setValue(downloadUrl.toString())
            }   } else {d("Camera2", "Camera is disabled")}
        }
    }
}

class DeviceProfile {
    companion object {
        var SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        var CHARACTERISTIC_STATE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    }
}
