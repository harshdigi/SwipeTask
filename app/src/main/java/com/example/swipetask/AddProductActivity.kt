package com.example.swipetask

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.swipetask.databinding.ActivityAddProductBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AddProductActivity : AppCompatActivity(){

    private var selectedImageUri: Uri? = null
    lateinit var binding: ActivityAddProductBinding
    lateinit var dialog : Dialog
    lateinit var dialog_button : LinearLayout
    lateinit var status_anim : LottieAnimationView
    lateinit var dialog_msg : TextView
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_box)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.setCancelable(false)
        val home_button = dialog.findViewById<MaterialButton>(R.id.home_btn)
        val add_product_button = dialog.findViewById<MaterialButton>(R.id.upload_btn)
        dialog_button = dialog.findViewById<LinearLayout>(R.id.dialog_button)
        dialog_msg = dialog.findViewById<TextView>(R.id.dialog_msg)
        status_anim = dialog.findViewById<LottieAnimationView>(R.id.status_anim)


        binding.imageView.setOnClickListener {
            opeinImageChooser()
        }

        binding.buttonAddProduct.setOnClickListener {

            uploadProduct()
        }

        binding.buttonBack.setOnClickListener {
            val i = Intent(applicationContext, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        home_button.setOnClickListener {
            val i = Intent(applicationContext, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        add_product_button.setOnClickListener{
            dialog.dismiss()
            dialog_button.visibility = View.GONE
            val intent = intent
            finish()
            startActivity(intent)
        }



    }



    private fun uploadProduct() {

        if (selectedImageUri == null) {
            Toast.makeText(applicationContext,"Please add product image.",Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.editTextProductName.text.isEmpty()){
            binding.editTextProductName.setError("Filed Cannot Be Empty")
            return
        }
        if (binding.editTextProductType.text.isEmpty()){
            binding.editTextProductType.setError("Filed Cannot Be Empty")
            return
        }
        if (binding.editTextSellingPrice.text.isEmpty()){
            binding.editTextSellingPrice.setError("Filed Cannot Be Empty")
            return
        }
        if (binding.editTextTaxRate.text.isEmpty()){
            binding.editTextTaxRate.setError("Filed Cannot Be Empty")
            return
        }

        val parcelFileDescriptor = contentResolver.openFileDescriptor(
            selectedImageUri!!, "r", null
        ) ?: return

        // resetting the dialog box
        dialog_button.visibility = View.GONE
        dialog_msg.text = "Please wait, We are processing your products.."
        dialog.show()

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir,contentResolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        val productName = binding.editTextProductName.text.toString()
        val productType = binding.editTextProductType.text.toString()
        val price = binding.editTextSellingPrice.text.toString()
        val tax = binding.editTextTaxRate.text.toString()
        val imageFile = file

        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("files[]", imageFile.name, requestBody)

        val call = RetrofitService.getInstance().uploadProduct(
            productName.toRequestBody("text/plain".toMediaTypeOrNull()),
            productType.toRequestBody("text/plain".toMediaTypeOrNull()),
            price.toRequestBody("text/plain".toMediaTypeOrNull()),
            tax.toRequestBody("text/plain".toMediaTypeOrNull()),
            listOf(imagePart)
        )

        call.enqueue(object : Callback<ProductResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful) {
                    dialog.dismiss()
                    dialog_button.visibility = View.VISIBLE
                    dialog_msg.text = "Product Added Successfully"
                    status_anim.setAnimation(R.raw.pass)
                    status_anim.playAnimation()
                    dialog.show()

                } else {
                    dialog.dismiss()
                    dialog_button.visibility = View.VISIBLE
                    dialog_msg.text = "Product Not Added"
                    status_anim.setAnimation(R.raw.fail)
                    status_anim.playAnimation()
                    dialog.show()
                    Toast.makeText(applicationContext,response.errorBody().toString(),Toast.LENGTH_SHORT).show()
                    Log.e("Error", response.errorBody().toString() + response.message())
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
            }
        })
    }

    private fun opeinImageChooser() {

        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> {
                    selectedImageUri = data?.data
                    binding.imageView.setImageURI(selectedImageUri)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_IMAGE = 101
    }

}

private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
    var name = ""
    val returnCursor = this.query(selectedImageUri,null,null,null,null)
    if (returnCursor!=null){
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }

    return name
}




