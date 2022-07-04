package com.example.instagram.navigation

import android.content.Intent
import android.net.Uri
import com.example.instagram.databinding.ActivityAddPhotoBinding
import androidx.activity.result.ActivityResultLauncher as ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.instagram.R
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    private lateinit var ActivityResult: ActivityResultLauncher<Intent>

    private val binding by lazy {
        ActivityAddPhotoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Initiate storage
        storage = FirebaseStorage.getInstance()

        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*" //setType
        ActivityResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                //This is path to the selected image
                photoUri = result.data?.data
                binding.addphotoImage.setImageURI(photoUri)
            }else {
                //Exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
        ActivityResult.launch(photoPickerIntent)
        binding.addphotoBtnUpload.setOnClickListener {
            ContentUpload()
        }
    }

    fun ContentUpload() {
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

          //FileUpload
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
        }?.addOnFailureListener{
            Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_LONG).show()
        }
    }
}