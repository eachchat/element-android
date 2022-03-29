package im.vector.app.yiqia.dialog

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SpanUtils
import im.vector.app.R
import im.vector.app.features.webview.NormalWebViewActivity
import im.vector.app.yiqia.cache.AppCache
import im.vector.app.yiqia.span.HighLightTaskClickSpan

class PrivacyPolicyDialog : GravityDialogFragment() {
    private var cancelListener: (() -> Unit)? = null

    companion object {
        fun newInstance() = PrivacyPolicyDialog()

        const val POLICY_CONTENT_LINK = "《亿洽团队沟通与协作平台隐私政策》"
        const val POLICY_START = "尊敬的用户，欢迎使用亿洽！我们非常重视您的隐私和个人信息保护。依据《个人信息保护法》等相关法律法规更新了"
        const val POLICY_MIDDLE = "的相关内容，以便更好地保护您的个人信息安全。请您通过完整阅读更新后的"
        const val POLICY_END = "了解政策更新情况，本隐私政策将帮助您了解以下内容： \n" +
                "1、定义及适用范围 \n" +
                "2、如何收集和使用您的个人信息 \n" +
                "3、如何使用Cookie和同类技术 \n" +
                "4、如何共享、转让、公开披露您的信息 \n" +
                "5、如何保护您的信息 \n" +
                "6、如何访问、修改及删除您的信息 \n" +
                "7、您的信息如何保存 \n" +
                "8、本隐私权政策如何更新 \n" +
                "9、如何联系我们 \n" +
                "如您点击同意，则表示您充分理解相关条款的内容。"
    }

    override fun getLayoutWidth(): Float {
        return 0.84f
    }

    override fun getLayoutHeight(): Float {
        return 0.63f
    }

    override fun getCanceledAble(): Boolean {
        return false
    }

    override fun getCanceledOnTouchOutside(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_privacy_policy_layout
    }

    override fun initView(rootView: View) {
        val contentTv = rootView.findViewById<TextView>(R.id.content_tv)
        val ssb = SpanUtils()
            .append(POLICY_START)
            .setForegroundColor(ContextCompat.getColor(requireContext(), R.color.ff999999))
            .append(POLICY_CONTENT_LINK)
            .setClickSpan(HighLightTaskClickSpan(
                POLICY_CONTENT_LINK,
                ContextCompat.getColor(requireContext(), R.color.ff4e6eed),
                false
            ) { navigationToWebView() })
            .append(POLICY_MIDDLE)
            .setForegroundColor(ContextCompat.getColor(requireContext(), R.color.ff999999))
            .append(POLICY_CONTENT_LINK)
            .setClickSpan(HighLightTaskClickSpan(
                POLICY_CONTENT_LINK,
                ContextCompat.getColor(requireContext(), R.color.ff4e6eed),
                false
            ) { navigationToWebView() })
            .append(POLICY_END)
            .setForegroundColor(ContextCompat.getColor(requireContext(), R.color.ff999999))
            .create()
        contentTv.movementMethod = LinkMovementMethod.getInstance()
        contentTv.text = ssb

        rootView.findViewById<TextView>(R.id.cancel_tv).setOnClickListener {
            cancelListener?.invoke()
            dismissAllowingStateLoss()
        }
        rootView.findViewById<TextView>(R.id.confirm_tv).setOnClickListener {
            AppCache.setShowPrivacyPolicy(false)
            dismissAllowingStateLoss()
        }
    }

    fun setListener(listener: () -> Unit) {
        this.cancelListener = listener
    }

    private fun navigationToWebView() {
        val intent = NormalWebViewActivity.getIntent(
            requireContext(),
            "file:///android_asset/privacy-policy.html",
            getString(R.string.privacy_policy)
        )
        startActivity(intent)
    }
}
