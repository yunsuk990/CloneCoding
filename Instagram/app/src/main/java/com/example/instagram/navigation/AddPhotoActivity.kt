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
import com.example.instagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null
    private lateinit var ActivityResult: ActivityResultLauncher<Intent>

    private val binding by lazy {
        ActivityAddPhotoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDto = ContentDTO()
                //Insert downloadUrl of image
                contentDto.imageUrl = uri.toString()

                //Insert uid of user
                contentDto.uid = auth?.currentUser?.uid

                //Insert userId
                contentDto.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDto.explain = binding.addphotoEditExplain.text.toString()

                //Insert timestamp
                contentDto.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDto)
                setResult(RESULT_OK) //정상적으로 작업 완료했음을 리턴
                finish()
            }


        }?.addOnFailureListener{
            Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_LONG).show()
        }
    }
}