package com.example.pestclassifier

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams

//Credits to https://www.flaticon.com/free-icons/tomato for the tomato icon

class DisplayDiagnosisActivity : AppCompatActivity(){
    private var selectedPest: String = ""
    private lateinit var pestImageURI: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        actionBar?.hide()
        setContentView(R.layout.activity_display_diagnosis)
        val pests = resources.getStringArray(R.array.pests)
        val common_names = resources.getStringArray(R.array.commonNames)
        val pest_drawables = arrayOf(
            R.drawable.se,
            R.drawable.sl,
            R.drawable.tu,
            R.drawable.ba,
            R.drawable.mp,
            R.drawable.ha
        )
        val referenceImage: ImageView = findViewById(R.id.referenceImage)
        val banner : TextView = findViewById(R.id.banner)
        val confidence = intent.getStringExtra(EXTRA_CONFIDENCE)
        val pest_index = intent.getIntExtra(EXTRA_PEST_INDEX, -1)

        val tips = arrayOf(
            resources.getStringArray(R.array.SE_tips),
            resources.getStringArray(R.array.SL_tips),
            resources.getStringArray(R.array.TU_tips),
            resources.getStringArray(R.array.BA_tips),
            resources.getStringArray(R.array.MP_tips),
            resources.getStringArray(R.array.HA_tips)
        )

        if (pest_index > -1) {
            val txtConf: TextView = findViewById(R.id.txtConf)
            txtConf.text = confidence
            val pest_drawable = pest_drawables[pest_index]
            referenceImage.setImageResource(pest_drawable)
            val scientific_name: TextView = findViewById(R.id.txtSN)
            val common_name: TextView = findViewById(R.id.txtCN)
            scientific_name.text = pests[pest_index]
            common_name.text = common_names[pest_index]

            val tip1: TextView = findViewById(R.id.tip1)
            val rowtip1: LinearLayout = findViewById(R.id.rowTip1)
            tip1.text = tips[pest_index][0]
            val parms1_tip = tip1.getLayoutParams()
            parms1_tip.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms1_tip.width = LinearLayout.LayoutParams.MATCH_PARENT
            var parms1_row = rowtip1.getLayoutParams()
            parms1_row.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms1_row.width = LinearLayout.LayoutParams.MATCH_PARENT
            rowtip1.setLayoutParams(parms1_row)

            val tip2: TextView = findViewById(R.id.tip2)
            val rowtip2: LinearLayout = findViewById(R.id.rowTip2)
            tip2.text = tips[pest_index][1]
            val parms2_tip = tip2.getLayoutParams()
            parms2_tip.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms2_tip.width = LinearLayout.LayoutParams.MATCH_PARENT
            var parms2_row = rowtip2.getLayoutParams()
            parms2_row.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms2_row.width = LinearLayout.LayoutParams.MATCH_PARENT
            rowtip2.setLayoutParams(parms2_row)
        } else{
            val scientific_name: TextView = findViewById(R.id.txtSN)
            val common_name: TextView = findViewById(R.id.txtCN)
            val ip : TextView = findViewById(R.id.ip)
            val conf : TextView = findViewById(R.id.conf)
            val txtConf : TextView = findViewById(R.id.txtConf)

            (common_name.parent as ViewGroup).removeView(common_name)
            (ip.parent as ViewGroup).removeView(ip)
            (conf.parent as ViewGroup).removeView(conf)
            (txtConf.parent as ViewGroup).removeView(txtConf)

            referenceImage.setImageResource(android.R.drawable.ic_menu_help)
            scientific_name.text = "Unknown"
            banner.text = "NO PEST FOUND"
            banner.setBackgroundColor(Color.parseColor("#A4C639"))
            val tip1: TextView = findViewById(R.id.tip1)
            val rowtip1: LinearLayout = findViewById(R.id.rowTip1)
            tip1.text = "Subukang i-sentro ang camera para nasa gitna ng picture ang peste. Gamitin din ang soft flash feature ng iyong camera, kung meron, sakaling madilim ang pinagkukuhanan ng picture."
            val parms1_tip = tip1.getLayoutParams()
            parms1_tip.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms1_tip.width = LinearLayout.LayoutParams.MATCH_PARENT
            var parms1_row = rowtip1.getLayoutParams()
            parms1_row.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms1_row.width = LinearLayout.LayoutParams.MATCH_PARENT
            rowtip1.setLayoutParams(parms1_row)

            val tip2: TextView = findViewById(R.id.tip2)
            val rowtip2: LinearLayout = findViewById(R.id.rowTip2)
            tip2.text = "Magkonsulta sa eksperto sakaling hindi matukoy ang peste. Idala ang larawan bilang kaakibat na ebidensya sa pagsusuri."
            val parms2_tip = tip2.getLayoutParams()
            parms2_tip.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms2_tip.width = LinearLayout.LayoutParams.MATCH_PARENT
            var parms2_row = rowtip2.getLayoutParams()
            parms2_row.height = LinearLayout.LayoutParams.WRAP_CONTENT
            parms2_row.width = LinearLayout.LayoutParams.MATCH_PARENT
            rowtip2.setLayoutParams(parms2_row)

            val ht : TextView = findViewById(R.id.howto)
            ht.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = R.id.txtSN
            }


        }

        val pest_image_uri_string = intent.getStringExtra(EXTRA_IMG_URI_STRING)
        pestImageURI = Uri.parse(pest_image_uri_string)
        val pestImage: ImageView = findViewById(R.id.pestImage)
        pestImage.setImageURI(pestImageURI)

        val btn_ack: Button = findViewById(R.id.btnAcknowledge)
        btn_ack.setOnClickListener(View.OnClickListener { finish() })
    }

    fun sendFeedback(view: View) {
//        val rootNode = FirebaseDatabase.getInstance()
//        val databaseReference = rootNode.getReference("images");
//        val storage = FirebaseStorage.getInstance();
//        val storageReference = storage.getReference();
//
//        val key = databaseReference.push().key
//        if (key != null) {
//            databaseReference.child(key).setValue(selectedPest)
//        }
//
//        val ref = storageReference?.child(key.toString())
//        ref.putFile(pestImageURI)
//            .addOnSuccessListener(
//               OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
//                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
//                        val imageUrl = it.toString()
//                   }
//               })
//
//            ?.addOnFailureListener(OnFailureListener { e -> Log.i("error", e.toString())
//            })
//    }

//        val pests = resources.getStringArray(R.array.pests)
//        val pest_image : ImageView = findViewById(R.id.imageToLabel)
//        val bitmap = getScaledBitmap(pest_image)
//        val input = preprocessImage(bitmap)
//        val (pest_index, confidence) = inferPest(input)
//        val percent_confidence = truncate(confidence * 100).toString()+"%"
//        val results = Bundle()
//        results.putInt(EXTRA_PEST_INDEX, pest_index)
//        results.putString(EXTRA_CONFIDENCE, percent_confidence)
//        results.putString(EXTRA_IMG_URI_STRING, pestImageURI.toString())
        val intent = Intent(this, FeedbackDiagnosis::class.java)
        intent.putExtra(EXTRA_IMG_URI_STRING, pestImageURI.toString())
        startActivity(intent)
    }
}
