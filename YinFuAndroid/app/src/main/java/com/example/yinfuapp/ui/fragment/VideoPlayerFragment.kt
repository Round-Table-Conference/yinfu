package com.example.yinfuapp.ui.fragment

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yinfuapp.databinding.FragmentVideoPlayerBinding
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class VideoPlayerFragment : Fragment() {

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding!!

    private var videoPath: String? = null
    private var videoTitle: String? = null
    private var tempVideoFile: File? = null
    private var videoDuration: String = ""

    companion object {
        private const val ARG_VIDEO_PATH = "video_path"
        private const val ARG_VIDEO_TITLE = "video_title"

        fun newInstance(videoPath: String, videoTitle: String): VideoPlayerFragment {
            return VideoPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIDEO_PATH, videoPath)
                    putString(ARG_VIDEO_TITLE, videoTitle)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoPath = arguments?.getString(ARG_VIDEO_PATH)
        videoTitle = arguments?.getString(ARG_VIDEO_TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadVideoFromAssets()
    }

    private fun setupUI() {
        binding.videoTitle.text = videoTitle ?: "课程视频"
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadVideoFromAssets() {
        videoPath?.let { path ->
            try {
                val inputStream = requireContext().assets.open(path)
                val fileName = path.substringAfterLast("/")
                tempVideoFile = File(requireContext().cacheDir, fileName)

                FileOutputStream(tempVideoFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()

                videoDuration = getVideoDuration(tempVideoFile!!)
                binding.videoTitle.text = "${videoTitle ?: "课程视频"} ($videoDuration)"

                binding.videoView.setVideoURI(Uri.fromFile(tempVideoFile))

                val mediaController = MediaController(requireContext())
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setMediaController(mediaController)

                binding.videoView.setOnPreparedListener {
                    binding.progressBar.visibility = View.GONE
                    it.start()
                }

                binding.videoView.setOnErrorListener { _, _, _ ->
                    Toast.makeText(context, "视频播放失败", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    true
                }

                binding.videoView.requestFocus()

            } catch (e: Exception) {
                Toast.makeText(context, "无法加载视频: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        } ?: run {
            Toast.makeText(context, "视频路径无效", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVideoDuration(file: File): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            retriever.release()

            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
            "${minutes}分${seconds}秒"
        } catch (e: Exception) {
            ""
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.videoView.stopPlayback()
        tempVideoFile?.delete()
        _binding = null
    }
}