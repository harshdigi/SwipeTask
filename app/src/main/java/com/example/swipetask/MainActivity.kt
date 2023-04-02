package com.example.swipetask

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.swipetask.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var viewModel: ProductViewModel
    private val adapter = ProductAdapter()
    lateinit var binding: ActivityMainBinding
    val productUpdatedList : MutableList<ProductModel> = mutableListOf()
    var productList : MutableList<ProductModel> = mutableListOf()
    lateinit var dialog : Dialog


    lateinit var dialog_button : LinearLayout
    lateinit var status_anim : LottieAnimationView
    lateinit var dialog_msg : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val retrofitService = RetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService)
        binding.productRecycler.adapter = adapter
        val layoutManager = GridLayoutManager(this, 2)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_dialog_box)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.setCancelable(false)
        dialog_button = dialog.findViewById<LinearLayout>(R.id.dialog_button)
        dialog_msg = dialog.findViewById<TextView>(R.id.dialog_msg)
        status_anim = dialog.findViewById<LottieAnimationView>(R.id.status_anim)
        status_anim.setAnimation(R.raw.loading)
        dialog_button.visibility = View.GONE
        dialog_msg.text = "Please wait, We are processing your products."
        dialog.show()

        binding.productRecycler.layoutManager = layoutManager

        viewModel = ViewModelProvider(this, ProductViewModelFactory(mainRepository))[ProductViewModel::class.java]


        viewModel.productList.observe(this) {
            Log.e("data", it.toString())
            adapter.setProduct(it)
            productList = it
            dialog.dismiss()
        }

        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }


        viewModel.getAllProducts()
        binding.addProductBtn.setOnClickListener{
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        binding.searchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                productUpdatedList.clear()
                Log.e("product List", productList.toString())
                for (i in productList) {
                    if (i.product_name.contains(s.toString(),true)){
                        productUpdatedList.add(i)
                    }
                }
                adapter.update(productUpdatedList)

            }

        })

    }

}