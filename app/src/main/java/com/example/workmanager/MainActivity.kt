package com.example.workmanager

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.work.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()
            )
            .build()

        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>().build()
        WorkManager.getInstance(applicationContext).beginUniqueWork("download",ExistingWorkPolicy.APPEND,downloadRequest).then(colorFilterRequest).enqueue()

        val imageView = findViewById<ImageView>(R.id.imageView)
        val drawable = Drawable.createFromPath(this.cacheDir.path+"/new_img.jpg")
        imageView.setImageDrawable(drawable)



    }
}