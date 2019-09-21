package com.dimfcompany.videotrimtest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_progress_bar.*

class ProgressBarActivity : AppCompatActivity(), FFMpegService.ListenerTrimmer
{
    var duration: Int? = null
    lateinit var command: Array<String>
    lateinit var path: String

    lateinit var mConnection: ServiceConnection
    lateinit var ffMpegService: FFMpegService
    var res: Int? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)
        cirlceProgressBar.max = 100

        duration = intent.getIntExtra("duration", 0)
        command = intent.getStringArrayExtra("command")
        path = intent.getStringExtra("destination")

        val intent = Intent(this, FFMpegService::class.java)
        intent.putExtra("duration", duration.toString())
        intent.putExtra("command", command)
        intent.putExtra("destination", path)

        startService(intent)

        mConnection = object : ServiceConnection
        {
            override fun onServiceDisconnected(name: ComponentName?)
            {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?)
            {
                val binder = service as FFMpegService.LocalBinder
                ffMpegService = binder.getServiceInstance()
                ffMpegService.registerClient(this@ProgressBarActivity)

                val resultObserver = Observer<Int>()
                {
                    res = it
                    if (res!! < 100)
                    {
                        cirlceProgressBar.progress = res!!
                    }

                    if (res == 100)
                    {
                        cirlceProgressBar.progress = res!!
                        stopService(intent)
                        Toast.makeText(this@ProgressBarActivity, "Trimmed Ok!!!", Toast.LENGTH_LONG).show()
                    }
                }

                ffMpegService.getPercentage().observe(this@ProgressBarActivity, resultObserver)
            }
        }

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun updateClient(data: Float)
    {

    }
}
