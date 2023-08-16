package com.example.aston

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.aston.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), RainbowDrumView.OnSetPhotoListener {

    private var binding: ActivityMainBinding? = null
    private var requestManager: RequestManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        requestManager = Glide.with(this)

        binding?.run {

            radiusChanged(seekbar, rainbowDrum)

            btSpin.setOnClickListener {
                rainbowDrum.spin()
                rainbowDrum.updatePhotoListener = this@MainActivity
            }

            btReset.setOnClickListener {
                rainbowDrum.clearText()
                rainbowDrum.updateRadius(100 * resources.displayMetrics.density)
                seekbar.progress = 50
                ivImage.setImageDrawable(null)
                Toast.makeText(this@MainActivity, "Reset data", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun setPhoto(currentColor: Int) {
        val image = findViewById<ImageView>(R.id.iv_image)
        if (currentColor in listOf(1, 3, 5)) {
            requestManager
                ?.load("https://loremflickr.com/640/360")
                ?.diskCacheStrategy(DiskCacheStrategy.NONE)
                ?.skipMemoryCache(true)
                ?.into(image)
        } else {
            image.setImageDrawable(null)
        }
    }

    private fun radiusChanged(seekBar: SeekBar, rainbowDrum: RainbowDrumView) {

        val minRadiusDp = 50
        val maxRadiusDp = 150
        val density = resources.displayMetrics.density
        val minRadiusPx = minRadiusDp * density
        val maxRadiusPx = maxRadiusDp * density

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newRadius = (progress / 100f) * (maxRadiusPx - minRadiusPx) + minRadiusPx
                rainbowDrum.updateRadius(newRadius)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

}
