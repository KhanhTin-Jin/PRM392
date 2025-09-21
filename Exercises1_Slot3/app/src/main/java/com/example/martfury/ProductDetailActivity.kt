package com.example.martfury

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.martfury.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng DataBinding thay vì setContentView
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_detail)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Set product data sử dụng binding object
        binding.apply {
            productTitle.text = "iPhone 17 Pro Max"
            brandName.text = "Apple"
            ratingText.text = "9.8"
            categoriesText.text = "Smartphones, Mobile Devices, Apple Ecosystem, Flagship Phones"
            skuText.text = "APL-IP17PM-512GB-TITANIUM"
        }
    }


    private fun setupClickListeners() {
        binding.apply {
            // Back button
            backButton.setOnClickListener {
                showToast("Quay lại shop")
                finish()
            }

            // Cart icon - click vào FrameLayout chứa cart icon
            cartIcon.setOnClickListener {
                showToast("Mở giỏ hàng")
            }

            // Compare button
            compareButton.setOnClickListener {
                showToast("Đã thêm vào danh sách so sánh")
            }

            // Favorite button
            favoriteButton.setOnClickListener {
                isFavorite = !isFavorite
                if (isFavorite) {
                    showToast("Đã thêm vào yêu thích ❤️")
                } else {
                    showToast("Đã xóa khỏi yêu thích")
                }
            }

            // Share button
            shareButton.setOnClickListener {
                showToast("Chia sẻ sản phẩm")
            }

            // Add to Cart button
            addToCartButton.setOnClickListener {
                showToast("Đã thêm sản phẩm vào giỏ hàng! 🛒")
            }

            // Buy Now button
            buyNowButton.setOnClickListener {
                showToast("Chuyển đến thanh toán 💳")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}