package com.example.pestclassifier

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class FeedbackDiagnosis : AppCompatActivity(), AdapterView.OnItemSelectedListener  {
    private lateinit var selectedPest:String
    private lateinit var pestImageURI: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionBar?.hide()
        setContentView(R.layout.activity_feedback_diagnosis)
        val queriedImage : ImageView = findViewById(R.id.queriedImage)
        pestImageURI = Uri.parse(intent.getStringExtra(EXTRA_IMG_URI_STRING))
        queriedImage.setImageURI(pestImageURI)

        val completePests = arrayOf(R.drawable.se, R.drawable.sl, R.drawable.tu, R.drawable.ba, R.drawable.mp, R.drawable.ha, R.drawable.ha)
        val btnConfirm : Button = findViewById(R.id.btn_confirm)

        val spinner: Spinner = findViewById(R.id.spinner_pest)
        val customAdapter = SpinnerAdapter(
            this,
            R.layout.custom_spinner_adapter,
            resources.getStringArray(R.array.completePests)
        )

        spinner.setAdapter(customAdapter)
        spinner.onItemSelectedListener = this


    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        val completePests = resources.getStringArray(R.array.completePests)
        selectedPest = completePests[pos]
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    fun sendFeedback(view: View) {
        val rootNode = FirebaseDatabase.getInstance()
        val databaseReference = rootNode.getReference("images");
        val storage = FirebaseStorage.getInstance();
        val storageReference = storage.getReference();

        val key = databaseReference.push().key
        val tv :TextView = findViewById(R.id.question)
        if (key != null) {
            val email : EditText = findViewById(R.id.email)
            var email_string = email.getText().toString()
            if (email_string== "")  email_string = "anonymous"
            val value = object {
                val pest = selectedPest
                val email = email_string
            }

            databaseReference.child(key).setValue(value)
                .addOnFailureListener { exception ->
                    // Handle the failure here
                    // You can log the error, display an error message, or take appropriate action
                    // For example:
                    Toast.makeText(this, "Failed to send feedback. Please try again.", Toast.LENGTH_SHORT).show()
                }        }

        else{
        }

        val ref = storageReference?.child(key.toString())
        ref.putFile(pestImageURI)
            .addOnSuccessListener(
               OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        Toast.makeText(this, "Feedback shared with the developers.", Toast.LENGTH_SHORT).show()
                        Handler().postDelayed({ }, 2000) // 1000 milliseconds


                        finish()
                    }
               })

            ?.addOnFailureListener(OnFailureListener { e ->  tv.text = e.toString()
                Toast.makeText(this, "Failed to send feedback. Check your internet connection.", Toast.LENGTH_SHORT).show()

            })
    }



}
