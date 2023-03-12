package com.example.workmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

//worker class basically defines worker's task

class DownloadWorker(
    private val context : Context,
    private val workerParams : WorkerParameters
) : CoroutineWorker(context , workerParams){
    //fun that performs actual work

    //workManager will work on long running task as Foreground service
    //Foreground Service - it is a service that a user is aware..means if this service executing user can see it running on notification
    override suspend fun doWork(): Result {
        startForegroundService()
        delay(5000L)
        val response = FileApi.instance.downloadImage()
        response.body()?.let {body ->
            return withContext(Dispatchers.IO){
                val file = File(context.cacheDir , "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use {stream ->
                    try{
                        stream.write(body.bytes())
                    }
                    catch (e : IOException){
                        return@withContext Result.failure(
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage //to function basically assigns value to key..
                            )
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toUri().toString()
                    )
                )
            }
        }
        if(!response.isSuccessful){//errors start with 5 are server side errors means in that case retrying can solve error
            if (response.code().toString().startsWith("5")){
                return Result.retry()
            }
            //error starts with 3 or 4 means client side error in that case we don't want to retry
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network Error!"
                )
            )
        }
        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Unknown Error!"
            )
        )
    }

    private suspend fun startForegroundService(){
        setForeground(
            ForegroundInfo(
                kotlin.random.Random.nextInt(),
                NotificationCompat.Builder(
                    context,
                    "notificationChannel"
                )
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Download in Progress")
                    .setContentText("Downloading....")
                    .build()
            )
        )
    }

}