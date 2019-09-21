package com.dimfcompany.videotrimtest

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import java.lang.Exception

class FFMpegService : Service()
{
    val TAG:String = "FFMpegService"
    
    var fFmpeg: FFmpeg? = null
    var duaration: Int? = null
    lateinit var command: Array<String>
    lateinit var listener: ListenerTrimmer

    private lateinit var percentage: MutableLiveData<Int>
    val myBinder = LocalBinder()

    override fun onCreate()
    {
        super.onCreate()
        loadFFmpegBinary()
        percentage = MutableLiveData()
    }

    private fun loadFFmpegBinary()
    {
        if (fFmpeg == null)
        {
            fFmpeg = FFmpeg.getInstance(this)
        }

        fFmpeg!!.loadBinary(object : FFmpegLoadBinaryResponseHandler
        {
            override fun onFinish()
            {

            }

            override fun onSuccess()
            {
            }

            override fun onFailure()
            {
            }

            override fun onStart()
            {
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        Log.e(TAG, "Called on Start!!!: " )
        if (intent != null)
        {
            duaration = intent.getStringExtra("duration").toInt()
            command = intent.getStringArrayExtra("command")
            loadFFmpegBinary()
            try
            {
                execFFmpegCommand()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun execFFmpegCommand()
    {
        fFmpeg!!.execute(command, object : FFmpegExecuteResponseHandler
        {
            override fun onFinish()
            {
                percentage.value = 100
            }

            override fun onSuccess(message: String?)
            {
            }

            override fun onFailure(message: String?)
            {
            }

            override fun onProgress(message: String?)
            {
                Log.e(TAG, "MEssage on progres is : $message" )


                val arr: List<String>
                if (message!!.contains("time="))
                {
                    arr = message!!.split("time=")

                    val yalo: String = arr.get(1)
                    val abikhama: List<String> = yalo.split(":")

                    val yaenda: List<String> = abikhama.get(2).split(" ")
                    val seconds = yaenda.get(0)

                    var hours = abikhama.get(0).toInt()
                    hours = hours * 3600
                    var min = abikhama.get(1).toInt() * 60
                    val sec = min.toFloat()

                    val time_in_seconds = hours + min + sec

                    percentage.value = ((time_in_seconds / duaration!!) * 100).toInt()
                }
            }

            override fun onStart()
            {
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder?
    {
        return myBinder
    }

    fun getPercentage(): MutableLiveData<Int>
    {
        return percentage
    }

    interface ListenerTrimmer
    {
        fun updateClient(data: Float)
    }

    fun registerClient(listener: ListenerTrimmer)
    {
        this.listener = listener
    }

    inner class LocalBinder : Binder()
    {
        fun getServiceInstance(): FFMpegService
        {
            return this@FFMpegService
        }
    }
}