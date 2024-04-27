package com.example.spyne.Network

import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Repository(private val retrofit: Retrofit) {
    private val service: ApiService = retrofit.create(ApiService::class.java)

    private var uploadJob: Job? = null

    suspend fun uploadImage(imageFile: File, progressCallback: ((Int) -> Unit)? = null): ResponseBody {
        return suspendCancellableCoroutine { continuation ->
            uploadJob = CoroutineScope(Dispatchers.IO).launch {
                val requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                try {
                    val response = service.uploadFile(body)
                    continuation.resume(response)
                } catch (e: Exception) {
                    // Handle network or other errors
                    continuation.resumeWithException(e)
                }
            }


            if (progressCallback != null) {
                uploadJob?.let { job ->
                    job.invokeOnCompletion {
                        // Ensure that the job is still active before accessing its progress
                        if (job.isActive) {
                            // Access progress and update UI or perform any action as needed
                            progressCallback.invoke(50) // Example: Update progress to 50%
                        }
                    }
                }
            }

            continuation.invokeOnCancellation {
                uploadJob?.cancel()
            }
        }
    }

    fun cancelUpload() {
        uploadJob?.cancel()
    }
}