package com.wonder.mapbox.mapboxnavigationv2demo.ui.base

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.wonder.mapbox.mapboxnavigationv2demo.databinding.LayoutProgressDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * author jiangjay on  27-07-2021
 */
abstract class BaseActivity<B : ViewBinding> : AppCompatActivity(), CoroutineScope by MainScope() {

    protected lateinit var binding: B

    private var _dialogBinding: LayoutProgressDialogBinding? = null

    private val dialogBinding: LayoutProgressDialogBinding
        get() = _dialogBinding!!

    private val dialog: Dialog by lazy {
        _dialogBinding = LayoutProgressDialogBinding.inflate(layoutInflater)
        Dialog(this).apply {
            setCancelable(true)
            setContentView(dialogBinding.root)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding()
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    abstract fun inflateBinding(): B

    @UiThread
    open fun showProgressDialog(prompt: String = "") {
        if (!dialog.isShowing) {
            if (!TextUtils.isEmpty(prompt)) {
                dialogBinding.loadingText.text = prompt
                dialogBinding.loadingText.visibility = View.VISIBLE
            }
            dialog.show()
        }
    }

    @UiThread
    open fun dismissProgressDialog() {
        if (dialog.isShowing) {
            dialogBinding.loadingText.visibility = View.GONE
            dialog.dismiss()
        }
    }
}