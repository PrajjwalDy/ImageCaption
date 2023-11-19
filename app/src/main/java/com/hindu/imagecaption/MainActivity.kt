package com.hindu.imagecaption

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage

class MainActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileImageRef: StorageReference? = null
    private lateinit var imageView:ImageView
    private lateinit var imageCaption:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectImage = findViewById<Button>(R.id.selectImage)
        imageView = findViewById<ImageView>(R.id.imageView)
        imageCaption = findViewById<TextView>(R.id.imageCaption)


        selectImage.setOnClickListener{
            CropImage.activity()
                .start(this@MainActivity)
        }

    }

    private fun processImageForLabeling(){
        if (imageUri != null) {
            val image = FirebaseVisionImage.fromFilePath(this, imageUri!!)

            val labeler = FirebaseVision.getInstance().onDeviceImageLabeler

            labeler.processImage(image)
                .addOnSuccessListener { labels ->
                    val caption = generateCaptionFromLabels(labels)
                    imageCaption.text = caption
                    Toast.makeText(this, caption, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Labeling failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    imageCaption.text = e.message.toString()
                }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateCaptionFromLabels(labels: List<FirebaseVisionImageLabel>): String {
        val filteredLabels =
            labels.filter { it.confidence >= 0.5 } // Filter labels based on confidence

        if (filteredLabels.isEmpty()) {
            return "No meaningful labels found"
        }

        val labelStrings = filteredLabels.map { it.text }


        return "${labelStrings.joinToString(separator = ", ")}"
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            imageView.setImageURI(imageUri)


            processImageForLabeling()
        }
    }
}