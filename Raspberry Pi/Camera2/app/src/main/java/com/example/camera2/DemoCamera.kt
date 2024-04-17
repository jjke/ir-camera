package com.example.camera2

import android.content.AsyncQueryHandler
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import java.util.*

class CompareByArea : Comparator<Size> {
    override fun compare(o1: Size?, o2: Size?): Int {
        return o1!!.width*o1.height - o2!!.width*o2.height
    }

}

class DemoCamera(private val onImageAvailableListener: ImageReader.OnImageAvailableListener, private val cameraHandler : Handler) {

    private var cameraDevice : CameraDevice? = null
    private lateinit var imageReader : ImageReader
    private lateinit var cameraCaptureSession : CameraCaptureSession

    fun openCamera(context : Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var camIds : Array<String> = emptyArray()

        camIds = cameraManager.cameraIdList

        cameraManager.openCamera(camIds[0], stateCallBack, cameraHandler)

        var characteristic = cameraManager.getCameraCharacteristics(camIds[0])
        val map = characteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        if (map == null) {
            Log.v("Camera2", "map == null")
        }

        val outPutSizes = map?.getOutputSizes(ImageFormat.JPEG)?.asList()

        outPutSizes?.forEach {
            Log.v("Camera2", "outPutSizes $it")
        }

        val largestRes = Collections.max(outPutSizes, CompareByArea())
        Log.v("Camera2", "largestRes ${largestRes.height} , ${largestRes.width}")

        imageReader = ImageReader.newInstance(largestRes.width, largestRes.height, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener(onImageAvailableListener, cameraHandler)

    }

    fun takePhoto() {

        if (cameraDevice == null || imageReader == null) {
            Log.v("Camera2", "cameraDevice == null || imageReader == null")
        }

        try {
            cameraDevice!!.createCaptureSession(Collections.singletonList(imageReader.surface),
                    object : CameraCaptureSession.StateCallback(){
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.v("Camera2", "onConfiguredFailed")
                        }

                        override fun onConfigured(session: CameraCaptureSession) {
                            if (cameraDevice == null) {
                                Log.v("Camera2", "cameraDevice == null")
                                return
                            }

                            cameraCaptureSession = session
                            doImageCapture()
                        }

                    },null)


        } catch (ex : CameraAccessException) {
            Log.v("Camera2", "CameraAccessException")
        }

    }

    private fun doImageCapture() {
        Log.v("Camera2", "doImageCapture")

        try {

            var captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader!!.surface)
            cameraCaptureSession.capture(captureBuilder.build(), captureCallBack, null)

        } catch (ex : CameraAccessException) {
            Log.v("Camera2", "CameraAccessException")
        }
    }

    private val stateCallBack = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.v("Camera2", "onOpened")
            cameraDevice = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.v("Camera2", "onDisconnected")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.v("Camera2", "onError")
        }

    }

    private val captureCallBack = object: CameraCaptureSession.CaptureCallback() {

    }

    fun shutDownCamera() {
        imageReader?.close()
        cameraCaptureSession?.close()
        cameraDevice?.close()
    }
}