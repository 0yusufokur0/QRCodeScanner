package com.resurrection.qrcodescanner


import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.resurrection.base.core.activity.BaseActivity
import com.resurrection.qrcodescanner.databinding.ActivityMainBinding
import com.resurrection.qrcodescanner.databinding.DetectionResultLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityViewModel>(R.layout.activity_main,MainActivityViewModel::class.java) {

    private val taskHandler = Handler(Looper.getMainLooper())
    private lateinit var detector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialogBinding : DetectionResultLayoutBinding
    private lateinit var alertDialog: AlertDialog

    override fun init(savedInstanceState: Bundle?) {
        setUpDetector()
        setUpCamera()
        setUpSurface()
        setUpAlertDialog()

    }

    private fun setUpAlertDialog() {
        builder = AlertDialog.Builder(this)
        dialogBinding = DetectionResultLayoutBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)
        builder.setCancelable(false)
        builder.setPositiveButton("continue") { dialog, which ->
            startCamera()
        }

        alertDialog = builder.create()

    }

    private fun setUpDetector(){
        detector = BarcodeDetector
            .Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(p0: Detector.Detections<Barcode>?) = detectionResult(p0?.detectedItems)
        })
    }

    private fun setUpCamera(){
        cameraSource = CameraSource
            .Builder(this, detector)
            .setRequestedPreviewSize(1024, 768)
            .setRequestedFps(30f)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun setUpSurface(){
        binding.barcodeSurfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceCreated(p0: SurfaceHolder) = checkPermission()
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) = Unit
            override fun surfaceDestroyed(p0: SurfaceHolder) = cameraSource.stop()
            override fun surfaceRedrawNeeded(p0: SurfaceHolder) = println()
        })
    }

    @SuppressLint("MissingPermission")
    private fun startCamera(): CameraSource = cameraSource.start(binding.barcodeSurfaceView.holder)

    private fun stopCamera() = cameraSource.stop()

    private fun detectionResult(barcodes: SparseArray<Barcode>?) {
        taskHandler.post {
            try {
                dialogBinding.resultTextView.text = barcodes?.valueAt(0)?.displayValue
                alertDialog.show()
                stopCamera()
                taskHandler.removeCallbacksAndMessages(null)
            } catch (e: Exception) {
                startCamera()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), 123)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCamera()
            else Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        stopCamera()
        cameraSource.release()
    }
}