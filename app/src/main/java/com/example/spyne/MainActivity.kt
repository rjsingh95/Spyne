package com.example.spyne

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spyne.RoomData.PictureModel
import com.example.spyne.databinding.ActivityMainBinding
import com.example.spyne.viewmodel.MainViewModel
import com.example.spyne.viewmodel.MainViewModelFactory
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
    private var binding: ActivityMainBinding? = null
    val mArrayUri: ArrayList<Uri> = ArrayList()
    var mainViewModel: MainViewModel? = null
    private var typeAdapter: ImaagesAdapter? = null
    private var eventsArrayList: java.util.ArrayList<PictureModel> = java.util.ArrayList()

    private val pickImages =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImages = mutableListOf<String?>()
                val data = result.data
                mArrayUri.clear()
                if (data?.clipData != null) {
                    data.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            selectedImages.add(uri.toString())
                            mArrayUri.add(uri)
                        }
                    }
                } else if (data?.data != null) {
                    data.data?.let { mArrayUri.add(it) }

                } else {
                    Toast.makeText(this, "You haven't picked an image", Toast.LENGTH_LONG).show()
                }

                if (mArrayUri.isNotEmpty()) {

                    var found = false
                    for (uri in mArrayUri) {
                        // Check if the item already exists in eventsArrayList based on ID
                        val existingItem = eventsArrayList.find { it.imageUri == uri.toString()}

                        // Add the item only if it does not already exist in eventsArrayList
                        if (existingItem == null) {
                            // Create a new PictureModel object and add it to eventsArrayList
                            val time = Calendar.getInstance().time
                            val image = PictureModel(
                                "",
                                uri.toString(),
                                time, "", "saved", time

                            )
                            mainViewModel?.InsertImage(image)
                            found = true
                        }
                    }
                    if(!found) Toast.makeText(this, "Select a different image", Toast.LENGTH_SHORT).show()
                    mainViewModel?.retrieveTasks()
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(application)
        ).get(MainViewModel::class.java)

        mainViewModel?.retrieveTasks()
        setRecyclerView()

        mainViewModel?.allSavedImages?.observe(this) { details ->
            if (details != null) {
                eventsArrayList.clear()
                eventsArrayList.addAll(details)
                eventsArrayList.forEach {
                    if (it.status == "saved" && mainViewModel?.uploading == false && it.status != "cancelled") {
                        mainViewModel?.uploading = true
                        it.status = "uploading"
                        uploadFile(it)
                    }
                }
                typeAdapter?.notifyDataSetChanged()
            }
        }

        binding?.addButton?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
                intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 7)
                pickImages.launch(intent)
            } else {
                checkPermissionsAndOpenGallery()
            }
        }
    }

    private fun setRecyclerView() {
        val layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        val itemClickListener = object : ImageItemClickListener {
            override fun onPauseClicked(pos: Int) {
                eventsArrayList.forEach {
                    if (mainViewModel?.selectedImage?.id == it.id && eventsArrayList.get(pos).status == "uploading") {
                        it.status = "cancelled"
                        mainViewModel?.repository?.cancelUpload()
                        mainViewModel?.uploading = false
                    } else if (it.status == "cancelled") {
                        uploadFile(it)
                        mainViewModel?.uploading = true
                        it.status = "uploading"
                    }
                }
                typeAdapter?.notifyDataSetChanged()
            }
        }
        binding?.rvImages?.layoutManager = layoutManager

        typeAdapter = ImaagesAdapter(eventsArrayList, itemClickListener)
        binding?.rvImages?.adapter = typeAdapter
    }

    private fun uploadFile(pos: PictureModel) {
        val file = mainViewModel?.getFileFromUri(this, Uri.parse(pos.imageUri))
        file?.let { mainViewModel?.uploadImage(it, pos) }
    }

    private fun checkPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGalleryForImages()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openGalleryForImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImages.launch(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, proceed with opening the gallery
                openGalleryForImages()
            } else {
                // Permissions not granted, show a message
                Toast.makeText(
                    this,
                    "Permissions required to access gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}