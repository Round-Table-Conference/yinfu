package com.example.yinfuapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yinfuapp.R
import com.example.yinfuapp.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var floatingView: View? = null
    private var windowManager: WindowManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.scanPayCard.setOnClickListener {
            checkOverlayPermission("wechat")
        }

        binding.guideCard.setOnClickListener {
            checkOverlayPermission("alipay")
        }

        binding.bankcardCard.setOnClickListener {
            checkOverlayPermission("alipay_pay")
        }

        binding.transactionCard.setOnClickListener {
            checkOverlayPermission("wechat_pay")
        }
    }

    private fun checkOverlayPermission(type: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            Toast.makeText(context, "需要悬浮窗权限，请在设置中开启", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}"))
            startActivity(intent)
            return
        }
        when (type) {
            "wechat" -> openWeChatWithFloatingWindow()
            "alipay" -> openAlipayWithFloatingWindow()
            "wechat_pay" -> openWeChatPayWithFloatingWindow()
            "alipay_pay" -> openAlipayPayWithFloatingWindow()
        }
    }

    private fun openWeChatWithFloatingWindow() {
        openWeChat()
        showFloatingWindow("wechat")
    }

    private fun openAlipayWithFloatingWindow() {
        openAlipayScan()
    }

    private fun openAlipayScan() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipayqr://platformapi/startapp?saId=10000007"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipay://"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "请先安装支付宝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openWeChatPayWithFloatingWindow() {
        openWeChatPaymentCode()
    }

    private fun openWeChatPaymentCode() {
        try {
            val intent = Intent()
            intent.setClassName("com.tencent.mm", "com.tencent.mm.framework.app.UIPageFragmentActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent("com.nearme.pay.qrcode")
                intent.setPackage("com.oplus.pay")
                intent.putExtra("extra_pkg_name", requireContext().packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {
                try {
                    val intent = Intent()
                    intent.setClassName("com.oplus.pay", "com.oplus.pay.channel.cn.wx.ui.QRCodePayActivity")
                    intent.putExtra("extra_pkg_name", requireContext().packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (e3: Exception) {
                    //openWeChatWithFloatingWindow()
                }
            }
        }
    }

    private fun openAlipayPayWithFloatingWindow() {
        openAlipayPaymentCode()
    }

    private fun openAlipayPaymentCode() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startapp?appId=20000056"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipay://"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "请先安装支付宝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFloatingWindow(windowType: String) {
        if (floatingView != null) return

        windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        val (titleText, guideText) = when (windowType) {
            "alipay" -> Pair("支付宝扫码指引", "请按以下步骤操作：\n\n1. 点击首页正上方【扫码】\n2. 对准二维码扫描")
            "alipay_pay" -> Pair("支付宝付款码指引", "请按以下步骤操作：\n\n1. 点击首页正上方【收付款】\n2. 点击【付款码】\n3. 向商家展示付款码")
            "wechat_pay" -> Pair("微信付款码指引", "请按以下步骤操作：\n\n1. 在微信首页点击右上角【+】\n2. 选择【收付款】\n3. 点击【向商家付款】\n4. 向商家展示付款码")
            else -> Pair("微信扫码指引", "请按以下步骤操作：\n\n1. 在微信首页点击右上角【+】\n2. 选择【扫一扫】\n3. 对准二维码扫描")
        }

        floatingView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
            setBackgroundColor(0xFF333333.toInt())

            val title = TextView(context).apply {
                text = titleText
                textSize = 18f
                setTextColor(0xFFFFFFFF.toInt())
            }

            val guide = TextView(context).apply {
                text = guideText
                textSize = 14f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 20, 0, 20)
            }

            val closeBtn = Button(context).apply {
                text = "知道了"
                setBackgroundColor(0xFF555555.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                setOnClickListener {
                    hideFloatingWindow()
                }
            }

            addView(title)
            addView(guide)
            addView(closeBtn)
        }

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            Toast.makeText(context, "无法显示悬浮窗", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideFloatingWindow() {
        try {
            floatingView?.let {
                windowManager?.removeView(it)
                floatingView = null
            }
        } catch (e: Exception) {
        }
    }

    private fun openWeChat() {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage("com.tencent.mm")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(context, "请先安装微信", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.tencent.mm"))
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(marketIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "请先安装微信", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAlipay() {
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(context, "请先安装支付宝", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.eg.android.AlipayGphone"))
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(marketIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "请先安装支付宝", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideFloatingWindow()
        _binding = null
    }
}