package com.example.yinfuapp.ui.fragment

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yinfuapp.R
import com.example.yinfuapp.databinding.FragmentClassroomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ClassroomFragment : Fragment() {

    private var _binding: FragmentClassroomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassroomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadVideoDurations()
    }

    private fun setupClickListeners() {
        binding.phoneBasic.setOnClickListener {
            onCategoryClicked("phone_basic")
        }

        binding.socialMedia.setOnClickListener {
            onCategoryClicked("social_media")
        }

        binding.lifeService.setOnClickListener {
            onCategoryClicked("life_service")
        }

        binding.course1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                    VideoPlayerFragment.newInstance("videos/Wechat_shoufukuan.mp4", "微信收付款"))
                .addToBackStack(null)
                .commit()
        }

        binding.course2.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                    VideoPlayerFragment.newInstance("videos/Pay_shoufukuan.mp4", "支付宝收付款"))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadVideoDurations() {
        CoroutineScope(Dispatchers.Main).launch {
            val duration1 = withContext(Dispatchers.IO) {
                getVideoDurationFromAssets("videos/Wechat_shoufukuan.mp4")
            }
            binding.course1Duration.text = if (duration1.isNotEmpty()) "视频教学 | $duration1" else "视频教学"

            val duration2 = withContext(Dispatchers.IO) {
                getVideoDurationFromAssets("videos/Pay_shoufukuan.mp4")
            }
            binding.course2Duration.text = if (duration2.isNotEmpty()) "视频教学 | $duration2" else "视频教学"
        }
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

    fun onCategoryClicked(category: String) {
        val fragment = when (category) {
            "phone_basic" -> PhoneBasicFragment.newInstance()
            "social_media" -> SocialMediaFragment.newInstance()
            "life_service" -> LifeServiceFragment.newInstance()
            else -> return
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}