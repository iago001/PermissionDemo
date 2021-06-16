package com.iago.android.permissiondemo

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.iago.android.permissiondemo.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMS = 1001
    private val PICK_PHOTO = 1002

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMS)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("PermDemo", "Permission granted")
            } else {
                Log.e("PermDemo", "Permission denied")
            }
        }
    }

    fun onImagePick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this)
                        .setMessage("To use this feature, please grant Files Access permission")
                        .setPositiveButton("Go to Settings", DialogInterface.OnClickListener{ dialog, which ->
                            openAppSystemSettings()
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
                return
            }
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PHOTO) {
            if (data?.data != null) {
                binding.imageView.setImageBitmap(loadFromUri(data.data))
            }
        }
    }

    // content://data/data/sdcard/photos/asda.jpg
    fun loadFromUri(photoUri: Uri?): Bitmap? {
        var image: Bitmap? = null

        try {
            image = if (Build.VERSION.SDK_INT > 27) {
                val source = ImageDecoder.createSource(this.contentResolver, photoUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri!!)
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }

        return image
    }

}

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}