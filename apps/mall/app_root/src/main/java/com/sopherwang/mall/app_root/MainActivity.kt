package com.sopherwang.mall.app_root

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sopherwang.libaries.ui.base.reduceDragSensitivity
import com.sopherwang.libraries.data_layer.product.ProductViewModel
import com.sopherwang.mall.feature.authorization.OnBoardingFragment
import com.sopherwang.mall.features.product_details.ProductDetailsFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val productViewModel: ProductViewModel by viewModels()

    private lateinit var root: MotionLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var onBoardingFragment: OnBoardingFragment
    private lateinit var productDetailFragment: ProductDetailsFragment

    private var lastProgress = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRoot()
        setupBottomNavView()
        setupViewPager()
        subscribeProductClick()
    }

    override fun onBackPressed() {
        if (!onBoardingFragment.onBackPressed()) {
            if (onBoardingFragment.isAdded) {
                root.transitionToStart()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun setupRoot() {
        onBoardingFragment =
            OnBoardingFragment.newInstance(object : OnBoardingFragment.AuthSuccessListener {
                override fun onAuthSuccess() {
                    if (onBoardingFragment.isAdded) {
                        root.transitionToStart()
                    }
                }
            })
        productDetailFragment = ProductDetailsFragment.newInstance()

        root = findViewById(R.id.activity_main_root)
        root.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                if (p3 - lastProgress > 0) {
                    // from start to end
                    val atEnd = Math.abs(p3 - 1f) < 0.1f
                    if (atEnd) {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction
                            .setCustomAnimations(R.animator.show, 0)
                        transaction
                            .setCustomAnimations(R.animator.show, 0)
                            .replace(R.id.main_page_cart_container, onBoardingFragment)
                            .commitNow()
                    }
                } else {
                    // from end to start
                    val atStart = p3 < 0.9f
                    if (atStart) {
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction
                            .setCustomAnimations(0, R.animator.hide)
                        transaction
                            .remove(onBoardingFragment)
                            .commitNow()
                    }
                }
                lastProgress = p3
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        })
    }

    private fun setupBottomNavView() {
        bottomNavigationView = findViewById(R.id.main_page_bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_nav_tab_main -> {
                    viewPager.currentItem = 0
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_nav_tab_category -> {
                    viewPager.currentItem = 1
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_nav_tab_subject -> {
                    viewPager.currentItem = 2
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.bottom_nav_tab_me -> {
                    viewPager.currentItem = 3
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener false
                }
            }
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.main_page_view_pager)
        viewPager.reduceDragSensitivity()
        with(viewPager) {
            adapter = MainPageViewPagerAdapter(supportFragmentManager, lifecycle)
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomNavigationView.menu.getItem(position).isChecked = true
                }
            })
        }
    }

    private fun subscribeProductClick() {
        productViewModel.productLiveData.observe(this, Observer { product ->
            Timber.tag(javaClass.simpleName).d("Product ${product.name} has clicked")

            val transaction = supportFragmentManager.beginTransaction()
            transaction
                .setCustomAnimations(R.animator.show, 0)
            transaction
                .setCustomAnimations(R.animator.show, 0)
                .replace(R.id.main_page_product_details_container, productDetailFragment)
                .commitNow()
        })
    }
}
