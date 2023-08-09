package com.example.beacondiscovery.ui.setting

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.beacondiscovery.R
import com.example.beacondiscovery.databinding.ActivitySettingBinding
import com.example.beacondiscovery.utils.Constant
import com.example.beacondiscovery.utils.PreferenceUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.math.min

class SettingActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setInit()

    }

    private fun setInit() {

        PreferenceUtil.getStringSharedPref(this,Constant.URL,"")?.let {
            mBinding.edUrl.setText(it)
        }
        PreferenceUtil.getStringSharedPref(this,Constant.MINUTES,"10")?.let {
            val temp = resources.getStringArray(R.array.time).indexOf(it)
            mBinding.spinnerTimer.setSelection(temp)
        }

        mBinding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        mBinding.btnSave.setOnClickListener {

            if (mBinding.edUrl.text.toString().isEmpty()) {
                Toast.makeText(this, "Please add url!", Toast.LENGTH_SHORT).show()
            } else if (!URLUtil.isValidUrl(mBinding.edUrl.text.toString())) {
                Toast.makeText(this, "Please add valid url!", Toast.LENGTH_SHORT).show()
            } else {
                val url = mBinding.edUrl.text.toString()
                val minute =
                    resources.getStringArray(R.array.time)[mBinding.spinnerTimer.selectedItemPosition]
                PreferenceUtil.setStringSharedPref(this, Constant.URL, url)
                PreferenceUtil.setStringSharedPref(this, Constant.MINUTES, minute)
                lifecycleScope.launch {
                    delay(200)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

    }


}