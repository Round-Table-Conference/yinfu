package com.example.yinfuapp.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceHelper(private val context: Context) {
    
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    fun startRecording(): File? {
        if (isRecording) return null
        
        outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.wav")
        
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            setAudioSamplingRate(8000)
            setAudioEncodingBitRate(6400)
            setOutputFile(outputFile?.absolutePath)
            prepare()
            start()
        }
        
        isRecording = true
        return outputFile
    }
    
    fun stopRecording(): File? {
        if (!isRecording) return null
        
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        recorder = null
        isRecording = false
        return outputFile
    }
    
    fun isRecording() = isRecording
    
    fun release() {
        try {
            recorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        recorder = null
        isRecording = false
    }
}