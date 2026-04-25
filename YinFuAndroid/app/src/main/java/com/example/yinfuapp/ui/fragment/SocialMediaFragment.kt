package com.example.yinfuapp.ui.fragment

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.yinfuapp.R
import com.example.yinfuapp.databinding.FragmentCourseListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SocialMediaFragment : Fragment() {

    private var _binding: FragmentCourseListBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): SocialMediaFragment = SocialMediaFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.categoryTitle.text = "社交媒体"
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val courses = listOf(
            Course("微信收付款", "videos/Wechat_shoufukuan.mp4"),
            Course("微信扫码付款", "videos/Wechat_saoma.mp4"),
            Course("支付宝收付款", "videos/Pay_shoufukuan.mp4"),
            Course("支付宝扫码付款", "videos/Pay_fuk.mp4")
        )

        binding.courseList.removeAllViews()
        courses.forEach { course ->
            addCourseItem(course)
        }
    }

    private fun addCourseItem(course: Course) {
        val itemView = layoutInflater.inflate(
            R.layout.item_course,
            binding.courseList,
            false
        )

        val titleView = itemView.findViewById<TextView>(R.id.course_title)
        val infoView = itemView.findViewById<TextView>(R.id.course_info)

        titleView.text = course.title
        infoView.text = "加载中..."

        CoroutineScope(Dispatchers.Main).launch {
            val duration = withContext(Dispatchers.IO) {
                getVideoDurationFromAssets(course.videoPath)
            }
            infoView.text = if (duration.isNotEmpty()) "视频教学 | $duration" else "视频教学"
        }

        itemView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                    VideoPlayerFragment.newInstance(course.videoPath, course.title))
                .addToBackStack(null)
                .commit()
        }

        binding.courseList.addView(itemView)
    }

    private fun getVideoDurationFromAssets(videoPath: String): String {
        return try {
            val inputStream = requireContext().assets.open(videoPath)
            val tempFile = File(requireContext().cacheDir, "temp_${videoPath.hashCode()}.mp4")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(tempFile.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            retriever.release()
            tempFile.delete()

            val minutes = durationMs / 1000 / 60
            val seconds = durationMs / 1000 % 60
            "${minutes}分${seconds}秒"
        } catch (e: Exception) {
            ""
        }
    }

    data class Course(
        val title: String,
        val videoPath: String
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}