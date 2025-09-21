package com.example.martfury

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.martfury.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var categoriesIndicatorDots: List<ImageView>
    private lateinit var brandsIndicatorDots: List<ImageView>
    private var categoriesTotalPages = 0
    private var brandsTotalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        setupCategoriesRecyclerView()
        setupBrandsRecyclerView()
    }

    private fun setupCategoriesRecyclerView() {
        val categories = listOf(
            CategoryItem("Camera", R.drawable.ic_menu_camera),
            CategoryItem("Tablet", R.drawable.ic_tablet),
            CategoryItem("Headphones", R.drawable.ic_headphones),
            CategoryItem("Macbook", R.drawable.ic_macbook),
            CategoryItem("Smartwatch", R.drawable.ic_smartwatch),
            CategoryItem("Gaming", R.drawable.gaming_console),
            CategoryItem("Drone", R.drawable.ic_drone),
            CategoryItem("Phone", R.drawable.phone_image),
            CategoryItem("Speaker", R.drawable.ic_speaker),
            CategoryItem("Monitor", R.drawable.ic_monitor),
            CategoryItem("VR Headset", R.drawable.ic_vr_headset),
            CategoryItem("Smart TV", R.drawable.ic_smart_tv),
            CategoryItem("Wireless", R.drawable.ic_wireless_charger),
            CategoryItem("Keyboard", R.drawable.ic_keyboard),
            CategoryItem("Projector", R.drawable.ic_projector)
        )

        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        binding.categoriesRecyclerView.layoutManager = layoutManager

        binding.categoriesRecyclerView.adapter = CategoriesAdapter(categories) { category ->
            when (category.name) {
                "Camera", "Electronics" -> {
                    val intent = Intent(this@HomeActivity, ProductDetailActivity::class.java)
                    startActivity(intent)
                    showToast("Mở danh mục ${category.name}")
                }
                else -> showToast("Mở danh mục ${category.name}")
            }
        }

        setupCategoriesScrollIndicator(categories.size)

        binding.categoriesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateCategoriesScrollIndicator()
            }
        })
    }

    private fun setupBrandsRecyclerView() {
        val brands = listOf(
            BrandItem("Xiaomi", R.drawable.logo_xiaomi_phone),
            BrandItem("Vivo", R.drawable.logo_vivo_preview),
            BrandItem("iQOO", R.drawable.logo_iqoo_phone),
            BrandItem("Samsung", R.drawable.logo_samsung),
            BrandItem("Apple", R.drawable.logo_apple),
            BrandItem("Oppo", R.drawable.logo_oppo),
            BrandItem("Huawei", R.drawable.logo_huawei)
        )

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.brandsRecyclerView.layoutManager = layoutManager

        binding.brandsRecyclerView.adapter = BrandsAdapter(brands) { brand ->
            showToast("Mở thương hiệu ${brand.name}")
        }

        setupBrandsScrollIndicator(brands.size)

        binding.brandsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateBrandsScrollIndicator()
            }
        })
    }

    private fun setupCategoriesScrollIndicator(totalItems: Int) {
        categoriesTotalPages = (totalItems + 5) / 6

        if (categoriesTotalPages <= 1) return

        val dots = mutableListOf<ImageView>()
        binding.indicatorContainer.removeAllViews()

        for (i in 0 until categoriesTotalPages) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params

            if (i == 0) {
                dot.setImageResource(R.drawable.indicator_active)
            } else {
                dot.setImageResource(R.drawable.indicator_inactive)
            }

            binding.indicatorContainer.addView(dot)
            dots.add(dot)
        }

        categoriesIndicatorDots = dots
    }

    private fun updateCategoriesScrollIndicator() {
        if (!::categoriesIndicatorDots.isInitialized || categoriesTotalPages <= 1) return

        val layoutManager = binding.categoriesRecyclerView.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val currentPage = minOf(firstVisiblePosition / 6, categoriesTotalPages - 1)

        categoriesIndicatorDots.forEachIndexed { index, dot ->
            if (index == currentPage) {
                dot.setImageResource(R.drawable.indicator_active)
            } else {
                dot.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun setupBrandsScrollIndicator(totalItems: Int) {
        brandsTotalPages = (totalItems + 2) / 3

        if (brandsTotalPages <= 1) return

        val dots = mutableListOf<ImageView>()
        binding.brandsIndicatorContainer.removeAllViews()

        for (i in 0 until brandsTotalPages) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params

            if (i == 0) {
                dot.setImageResource(R.drawable.indicator_active)
            } else {
                dot.setImageResource(R.drawable.indicator_inactive)
            }

            binding.brandsIndicatorContainer.addView(dot)
            dots.add(dot)
        }

        brandsIndicatorDots = dots
    }

    private fun updateBrandsScrollIndicator() {
        if (!::brandsIndicatorDots.isInitialized || brandsTotalPages <= 1) return

        val layoutManager = binding.brandsRecyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val currentPage = minOf(firstVisiblePosition / 3, brandsTotalPages - 1)

        brandsIndicatorDots.forEachIndexed { index, dot ->
            if (index == currentPage) {
                dot.setImageResource(R.drawable.indicator_active)
            } else {
                dot.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }

    data class CategoryItem(val name: String, val imageRes: Int)
    data class BrandItem(val name: String, val logoRes: Int)

    class CategoriesAdapter(
        private val categories: List<CategoryItem>,
        private val onItemClick: (CategoryItem) -> Unit
    ) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

        class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val categoryImage: ImageView = view.findViewById(R.id.categoryImage)
            val categoryName: TextView = view.findViewById(R.id.categoryName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_category_card, parent, false
            )
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = categories[position]

            holder.categoryImage.setImageResource(category.imageRes)
            holder.categoryName.text = category.name

            holder.itemView.setOnClickListener {
                onItemClick(category)
            }
        }

        override fun getItemCount() = categories.size
    }

    class BrandsAdapter(
        private val brands: List<BrandItem>,
        private val onItemClick: (BrandItem) -> Unit
    ) : RecyclerView.Adapter<BrandsAdapter.BrandViewHolder>() {

        class BrandViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val brandLogo: ImageView = view.findViewById(R.id.brandLogo)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_brand_card, parent, false
            )
            return BrandViewHolder(view)
        }

        override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
            val brand = brands[position]

            holder.brandLogo.setImageResource(brand.logoRes)

            holder.itemView.setOnClickListener {
                onItemClick(brand)
            }
        }

        override fun getItemCount() = brands.size
    }

    private fun setupClickListeners() {
        binding.apply {
            searchBar.setOnClickListener {
                showToast("Mở tìm kiếm")
            }

            heartIcon.setOnClickListener {
                showToast("Mở danh sách yêu thích")
            }

            shareIcon.setOnClickListener {
                showToast("Mở tùy chọn chia sẻ")
            }

            iphone17Banner.setOnClickListener {
                showToast("Mở ưu đãi iPhone 17")
            }

            iphoneBanner.setOnClickListener {
                val intent = Intent(this@HomeActivity, ProductDetailActivity::class.java)
                startActivity(intent)
                showToast("Mở ưu đãi iPhone")
            }

            leatherBagsBanner.setOnClickListener {
                showToast("Mở ưu đãi Apple")
            }

            homeTab.setOnClickListener {
                showToast("Đang ở trang chủ")
            }

            categoriesTab.setOnClickListener {
                showToast("Mở danh mục")
            }

            explorerTab.setOnClickListener {
                showToast("Mở khám phá")
            }

            cartTab.setOnClickListener {
                showToast("Mở giỏ hàng")
            }

            profileTab.setOnClickListener {
                showToast("Mở hồ sơ")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}