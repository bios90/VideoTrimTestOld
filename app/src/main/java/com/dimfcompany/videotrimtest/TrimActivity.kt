package com.dimfcompany.videotrimtest

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.innovattic.rangeseekbar.RangeSeekBar
import kotlinx.android.synthetic.main.activity_trim.*
import java.io.File
import java.lang.Exception

class TrimActivity : AppCompatActivity()
{
    val TAG: String = "TrimActivity"

    lateinit var uri: Uri
    var duration: Int? = null
    var filePrefix: String? = null
    var command: Array<String>? = null
    var dest: File? = null
    var originalPathString: String? = null
    var isPlaying = false


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim)

        val imgPath = intent.getStringExtra("uri")
        uri = Uri.parse(imgPath)
        videoView.setVideoURI(uri)
        videoView.start()
        isPlaying = true

        setListeners()

    }

    private fun setListeners()
    {
        img_pause_play.setOnClickListener(
            {
                if (isPlaying)
                {
                    img_pause_play.setImageResource(R.drawable.ic_pause)
                    videoView.pause()
                }
                else
                {
                    img_pause_play.setImageResource(R.drawable.ic_play)
                    videoView.start()
                }

                isPlaying = !isPlaying
            })


        videoView.setOnPreparedListener(
            {
                videoView.start()
                duration = it.duration / 1000
                tv_left.setText("00:00:00")
                tv_right.setText(getTime(duration!!))
                it.isLooping = true
                seekbar.minRange = 0
                seekbar.max = duration!!

                seekbar.seekBarChangeListener = object : RangeSeekBar.SeekBarChangeListener
                {
                    override fun onStartedSeeking()
                    {
                        videoView.pause()
                    }

                    override fun onStoppedSeeking()
                    {
                        videoView.seekTo(seekbar.getMinThumbValue())
                    }

                    override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int)
                    {
                        videoView.seekTo(minThumbValue * 1000)
                        tv_left.setText(getTime(minThumbValue))
                        tv_right.setText(getTime(maxThumbValue))
                    }
                }
            })
    }

    fun getTime(seconds: Int): String
    {
        val hr = seconds / 3600
        val rem = seconds % 3600
        val min = rem / 60
        val sec = rem % 60

        return String.format("%02d", hr) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean
    {
        if (item!!.itemId == R.id.trim)
        {
            val alert = AlertDialog.Builder(this)
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(50, 0, 50, 100)
            val edit_text = EditText(this)
            edit_text.layoutParams = params
            edit_text.gravity = Gravity.TOP or Gravity.START
            edit_text.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            linearLayout.addView(edit_text)

            alert.setTitle("Name change")
            alert.setMessage("Set video name")
            alert.setView(linearLayout)

            alert.setNegativeButton("Cancel",
                { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                })

            alert.setPositiveButton("Submit",
                { dialogInterface: DialogInterface, i: Int ->
                    filePrefix = edit_text.text.toString().trim()
                    trimVideo(seekbar.getMinThumbValue() * 1000, seekbar.getMaxThumbValue() * 1000, filePrefix!!)

                    val intent = Intent(this, ProgressBarActivity::class.java)
                    intent.putExtra("duration", duration)
                    intent.putExtra("command", command)
                    intent.putExtra("destination", dest!!.absolutePath)
                    startActivity(intent)

                    dialogInterface.dismiss()
                })

            alert.show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun trimVideo(startMs: Int, endMs: Int, fileName: String)
    {
        val folder = File(Environment.getExternalStorageDirectory().toString() + "/trimmed")
        if (!folder.exists())
        {
            folder.mkdir()
        }

        val file_name = "$filePrefix.mp4"
        dest = File(folder, file_name)
        originalPathString = getRealPathFromString(applicationContext, uri)

        Log.e("TAGGGg", "Destination path is ${dest!!.absolutePath} ")

        duration = (endMs - startMs) / 1000

        command = arrayOf(
            "-ss",
            (startMs / 1000).toString(),
            "-y",
            "-i",
            originalPathString!!,
            "-t",
            ((endMs - startMs) / 1000).toString(),
            "-vcodec",
            "mpeg4",
            "-preset",
            "ultrafast",
            "-maxrate",
            "2M",
            "-b:v",
            "2M",
            "-bufsize",
            "2M",
            "-b:a",
            "48000",
            "-ac",
            "2",
            "-ar",
            "22050",
            dest!!.absolutePath
        )

//        command = arrayOf(
//            "-ss",
//            (startMs/1000).toString(),
//            "-y",
//            "-i",
//            originalPathString!!,
//            "-t",
//            ((endMs-startMs)/1000).toString(),
//            "-vf",
//            "scale=-2:1080",
////            "-preset",
////            "slow",
//            "-codec:v",
//            "libx264",
//            "-x264-params",
//            "nal-hrd=cbr",
//            "-b:v",
//            "2.5M",
//            "-maxrate",
//            "2.5M",
//            "-bufsize",
//            "2.5M",
//            "-b:a",
//            "48000",
//            "-ac",
//            "2",
//            "-ar",
//            "22050",
//            dest!!.absolutePath
//        )

//        command = arrayOf(
//            "-ss",
//            (startMs/1000).toString(),
//            "-y",
//            "-i",
//            originalPathString!!,
//            "-t",
//            ((endMs-startMs)/1000).toString(),
//            "-profile:v",
//            "high",
////            "-preset",
////            "slow",
//            "-codec:v",
//            "libx264",
//            "-b:v",
//            "500k",
//            "-maxrate",
//            "500k",
//            "-bufsize",
//            "1000k",
//            "-b:a",
//            "48000",
//            "-ac",
//            "2",
//            "-ar",
//            "22050",
//            dest!!.absolutePath
//        )

//        command = arrayOf(
//            "-ss",
//            (startMs/1000).toString(),
//            "-y",
//            "-i",
//            originalPathString!!,
//            "-t",
//            ((endMs-startMs)/1000).toString(),
//            "-vcodec",
//            "mpeg4",
//            "-b:v",
//            "2097152",
//            "-b:a",
//            "48000",
//            "-ac",
//            "2",
//            "-ar",
//            "22050",
//            dest!!.absolutePath
//        )
    }

    fun createFile(name: String, extansion: String?, folder: String): File
    {
        var file: File

        val folder_file = File(getExternalFilesDir(null).toString() + "/" + folder)
        if (!folder_file.exists())
        {
            folder_file.mkdirs()
        }

        var file_name = name
        if (extansion != null)
        {
            file_name = file_name + "." + extansion
        }

        file = File(folder_file, file_name)
        if (file.exists())
        {
            file.delete()
        }

        Log.e(TAG, "Filename is : ${file_name}, Folder is ${folder_file.absoluteFile}");

        file.createNewFile()

        return file
    }

    private fun getRealPathFromString(context: Context, contentUri: Uri): String?
    {
        var cursor: Cursor? = null

        try
        {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return null
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close()
            }
        }
    }
}