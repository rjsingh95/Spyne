package com.example.spyne.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.spyne.Network.ApiClient
import com.example.spyne.Network.ApiService
import com.example.spyne.Network.Repository
import com.example.spyne.RoomData.AppDatabase
import com.example.spyne.RoomData.PictureModel
import com.example.spyne.UploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar


class MainViewModel(application: Application) : AndroidViewModel(application) {

    var selectedImage: PictureModel?=null
    var repository: Repository? = null
    val allSavedImages: MutableLiveData<List<PictureModel>> = MutableLiveData()
    var uploading: Boolean = false
    var mDb: AppDatabase? = AppDatabase.getInstance(application)
    private val apiService: ApiService

    init {
        val retrofit = ApiClient.client
        apiService = retrofit?.create(ApiService::class.java)!!
        repository = retrofit.let { Repository(it) }
    }

    fun InsertImage(person: PictureModel) {
        viewModelScope.launch(Dispatchers.IO) {
            mDb!!.imageDao()?.insert(person)
            retrieveTasks()
        }
    }

    fun retrieveTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            mDb!!.imageDao()?.loadAllImages()?.let {
                allSavedImages.postValue(it)
            }
        }
    }

    fun uploadImage(imageFile: File, pictureModel: PictureModel) {
        selectedImage =pictureModel
        viewModelScope.launch {
            try {
                val response = repository?.uploadImage(imageFile)
                val responseBodyString = response?.string()

                val gson = Gson()
                val uploadResponse = gson.fromJson(responseBodyString, UploadResponse::class.java)

                val imageUrl = uploadResponse.image
                if (!imageUrl.isNullOrEmpty()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        pictureModel.imageUrl = imageUrl
                        pictureModel.status = "uploaded"
                        val time = Calendar.getInstance().time
                        pictureModel.resultdate = time
                        mDb?.imageDao()?.update(pictureModel)
                        uploading = false
                        retrieveTasks()
                    }
                }
                Log.d("chack", response.toString())
            } catch (e: Exception) {
                // Handle exception or error
                Log.e("ViewModel", "Error uploading image: $e")
            }
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        var inputStream: InputStream? = null
        var file: File? = null
        try {
            val contentResolver: ContentResolver = context.contentResolver
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Create a temporary file in app's cache directory
                val cacheDir = context.cacheDir
                file = File.createTempFile("temp_image", null, cacheDir)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return file
    }

}
