package com.example.pestclassifier

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.*
import java.lang.Byte
import java.lang.Float
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyStore.TrustedCertificateEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.Throws
import kotlin.also
import kotlin.apply
import kotlin.math.truncate
import kotlin.toString
import kotlin.with


const val GALLERY_REQUEST_CODE = 71
const val CAMERA_REQUEST_CODE = 72
const val EXTRA_PEST_INDEX = "com.example.pestclassifier.PEST"
const val EXTRA_CONFIDENCE = "com.example.pestclassifier.CONFIDENCE"
const val EXTRA_IMG_RES_ID = "com.example.pestclassifier.IMG_RES_ID"
const val EXTRA_IMG_URI_STRING = "com.example.pestclassifier.IMG_URI_STRING"
const val EXTRA_FEEDBACK = "com.example.pestclassifier.FEEDBACK"
const val HEIGHT = 128
const val WIDTH = 128

class MainActivity : AppCompatActivity() {

    private lateinit var photoURI: Uri
    private lateinit var photoFile: File
    private lateinit var cameraPhotoPath: String
    private lateinit var interpreter: Interpreter
    private lateinit var progressBar: ProgressBar
    private lateinit var galleryButton: Button
    private lateinit var cameraButton: Button
    private lateinit var diagnoseButton: Button

    private lateinit var prompt: TextView
    private lateinit var pestImageURI: Uri
    private var imageSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionBar?.hide()
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        prompt = findViewById(R.id.prompt)
        galleryButton = findViewById(R.id.btn_gallery)
        cameraButton = findViewById(R.id.btn_camera)
        prompt.text = "Model loading for the first time. This may take a minute."
        progressBar.visibility = View.VISIBLE

        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel(
                "Tomato-Pest-Classifier", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions
            )
            .addOnSuccessListener { model: CustomModel? ->
                val modelFile = model?.file
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    progressBar.visibility = View.GONE
                    prompt.text = "Please select a pest image to diagnose."
                    galleryButton.isEnabled=true
                    cameraButton.isEnabled=true
                }
                Log.i("modelTAG", "Loaded model")
            }
            .addOnFailureListener { e ->
                Log.i("modelTAG", e.toString())
                Toast.makeText(this, "Failed to download model", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                prompt.text = "Model failed to load. Try restarting this app."

            }
    }



    fun diagnosePest(view: View){

        val pests = resources.getStringArray(R.array.pests)
        val pest_image : ImageView = findViewById(R.id.imageToLabel)
        val bitmap = getScaledBitmap(pest_image)
        val input = preprocessImage(bitmap)
        val (pest_index, confidence) = inferPest(input)

        var percent_confidence = ""
        var final_pest_index = -1000

        if (confidence < 0.9){
            percent_confidence = "None"
            final_pest_index = -1
        }
        else {
            percent_confidence = truncate(confidence * 100).toInt().toString()+"%"
            final_pest_index = pest_index
        }

        val results = Bundle()
        results.putInt(EXTRA_PEST_INDEX, final_pest_index)
        results.putString(EXTRA_CONFIDENCE, percent_confidence)
        results.putString(EXTRA_IMG_URI_STRING, pestImageURI.toString())
        val intent = Intent(this, DisplayDiagnosisActivity::class.java)
        intent.putExtras(results)
        startActivity(intent)
    }
    fun openGalleryForImage(view: View) {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, GALLERY_REQUEST_CODE)
    }

    private fun getScaledBitmap(view: ImageView): Bitmap? {
        val img: ImageView = findViewById(R.id.imageToLabel)
        val raw_bitmap = (img.drawable as BitmapDrawable).bitmap
        val bitmap = Bitmap.createScaledBitmap(raw_bitmap!!, WIDTH, HEIGHT, true)
        return bitmap
    }

    private fun preprocessImage(bitmap: Bitmap?): ByteBuffer {
        val input = ByteBuffer.allocateDirect(128 * 128 * 3 * 4)
            .order(ByteOrder.nativeOrder())
        for (y in 0 until 128) {
            for (x in 0 until 128) {
                val px = bitmap?.getPixel(x, y)

                val r = Color.red(px!!)
                val g = Color.green(px!!)
                val b = Color.blue(px!!)

                val rf = (r - 127.5f) / 127.5f
                val gf = (g - 127.5f) / 127.5f
                val bf = (b - 127.5f) / 127.5f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }
        return input
    }

    private fun inferPest(input : ByteBuffer): Pair<Int, kotlin.Float> {
        val bufferSize = 6 * Float.SIZE / Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())

        interpreter?.run(input, modelOutput)
        Log.i("step", "B")

        modelOutput.rewind()
        var outputText = ""
        val prob = modelOutput.asFloatBuffer()

        var max_prob = 0f
        var max_index = 0

        try {
            val reader = BufferedReader(
                InputStreamReader(assets.open("custom_labels.txt"))
            )

            for (i in 0 until prob.capacity()) {
                Log.i("step", prob.capacity().toString())
                val label: String = reader.readLine()
                val probability = prob.get(i)
                if (probability > max_prob){
                    max_prob = probability
                    max_index = i
                }
            }

        } catch (e: IOException) {
            Log.i("step", e.toString())
        }

        return Pair(max_index, max_prob)
    }
     fun galleryAddPic() {
        imageSelected = true
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(cameraPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        Log.i("path", cameraPhotoPath)
        Log.i("path", mediaScanIntent.data.toString())
        this.sendBroadcast(mediaScanIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("hello", "hello")
        val prompt : TextView = findViewById(R.id.prompt)
        val img: ImageView = findViewById(R.id.imageToLabel)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            Log.i("gallery", data?.data.toString())
            val btn_label : Button = findViewById(R.id.btn_diagnose)
            btn_label.setEnabled(true)
            val prompt: TextView = findViewById(R.id.prompt)
//            prompt.setText("Specimen selected. Diagnose pest or select another image.")
//            img.setImageURI(data?.data) // handle chosen image
            val galleryImageURI = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), galleryImageURI)
            val thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 500, 500)

            val wrapper = ContextWrapper(this)
            var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
            file = File(file,"${UUID.randomUUID()}.jpg")
            val stream: OutputStream = FileOutputStream(file)
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG,25,stream)
            stream.flush()
            stream.close()

            pestImageURI = Uri.fromFile (file)
            img.setImageURI(pestImageURI);

            prompt.text = "Specimen selected. Diagnose pest or select another image."

        }
        else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val btn_label : Button = findViewById(R.id.btn_diagnose)
            btn_label.setEnabled(true)
            Log.i("camera", data?.data.toString())
            val f = File(cameraPhotoPath)
            val camera_img_uri = Uri.fromFile(f)
            val bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), camera_img_uri)
            val thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 500, 500)

            val wrapper = ContextWrapper(this)
            var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
            file = File(file,"${UUID.randomUUID()}.jpg")
            val stream: OutputStream = FileOutputStream(file)
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG,25,stream)
            stream.flush()
            stream.close()

            pestImageURI = Uri.fromFile (file)
            img.setImageURI(pestImageURI);

            prompt.text = "Specimen selected. Diagnose pest or select another image."
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            cameraPhotoPath = absolutePath
        }
    }

    fun dispatchTakePictureIntent(view: View) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) { File.createTempFile ("",  "") }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.pestclassifier.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

}

fun Context.assetsToBitmap(fileName: String): Bitmap?{
    return try {
        with(assets.open(fileName)){
            BitmapFactory.decodeStream(this)
        }
    } catch (e: IOException) {
        Log.i("picture", e.toString())
        Log.i("picture", "yep it's null")
        null
    }
}

