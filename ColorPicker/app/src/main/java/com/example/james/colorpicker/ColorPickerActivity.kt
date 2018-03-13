package com.example.james.colorpicker

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.SeekBar
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.View.VISIBLE
import android.widget.Toast
import com.example.james.colorpicker.R.id.*
import kotlinx.android.synthetic.main.activity_color_picker.*
import java.io.File

class ColorPickerActivity : AppCompatActivity() {

    //Raw color values
    var red = 0
    var green = 0
    var blue = 0
    //Color values formatted for hex codes
    var redHex = "00"
    var greenHex = "00"
    var blueHex = "00"
    //Color name
    var name = ""
    //Canvas for drawing colors
    var canvas = Canvas()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val info = intent.extras
        setContentView(R.layout.activity_color_picker)
        supportActionBar!!.setLogo(R.drawable.logo)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayUseLogoEnabled(true)

        //Add return button if launched from Color Blender
        if(info != null){
            if(info.containsKey("GET_COLOR")) {
                sendButton.visibility = VISIBLE
            }
        }

        createDefaults()

        seekRed.max = 255
        seekGreen.max = 255
        seekBlue.max = 255

        //Allow the color to be changed when it needs to and
        //persist when the app is paused
        display.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                showColor()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                showColor()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }
        })

        //Set red seekbar
        seekRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                textRed.text = value.toString()
                redHex = java.lang.Integer.toHexString(value).toUpperCase()
                if(redHex.length < 2){
                    redHex = ("0" + redHex)
                }
                colorValue.text = ("#" + redHex + greenHex + blueHex)

                red = value
                showColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","started")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","stopped")
            }
        })

        //Set green seekbar
        seekGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                textGreen.text = value.toString()
                greenHex = java.lang.Integer.toHexString(value).toUpperCase()
                if(greenHex.length < 2){
                    greenHex = ("0" + greenHex)
                }
                colorValue.text = ("#" + redHex + greenHex + blueHex)

                green = value
                showColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","started")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","stopped")
            }
        })

        //Set blue seekbar
        seekBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                textBlue.text = value.toString()
                blueHex = java.lang.Integer.toHexString(value).toUpperCase()
                if(blueHex.length < 2){
                    blueHex = ("0" + blueHex)
                }
                colorValue.text = ("#" + redHex + greenHex + blueHex)

                blue = value
                showColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","started")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i("Log: ","stopped")
            }
        })

        //Send intent to Color Blender
        sendButton.setOnClickListener { view ->
            finish()
        }
    }

    override fun finish(){
        val toReturn = Intent()
        toReturn.action = "RECEIVE_COLOR"
        toReturn.putExtra("COLOR",red.toString() + "," + green.toString() + "," + blue.toString())
        setResult(RESULT_OK, toReturn)
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        action_save -> {
            //Save R,G,B,name to file
            name = colorName.text.toString()
            saveColor()

            true
        }
        action_load -> {
            //Load RGB, convert to hex, load name
            loadColor()
            canvas = display.holder.lockCanvas()
            canvas.drawRGB(red, green, blue)
            display.holder.unlockCanvasAndPost(canvas)

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun createDefaults(){
        //Used if there is no .txt
        val file = File(filesDir,"colors.txt")

        if(!file.exists()){
            file.appendText("255,0,0,Red\n")
            file.appendText("0,255,0,Green\n")
            file.appendText("0,0,255,Blue\n")
        }
    }

    private fun saveColor(){
        val file = File(filesDir,"colors.txt")

        if(name != ""){
            file.appendText(red.toString() + "," + green.toString() + "," + blue.toString() + "," + name + "\n")
            Toast.makeText(this, name + " saved", Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(this, "Color name cannot be blank", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadColor(){
        val file = File(filesDir,"colors.txt")
        var tempParts: List<String> //Store parts from file line
        val colorList = ArrayList<List<String>>() //List of colors, contains list of parts
        var formattedList = ArrayList<String>() //Formatted for display
        var displayList: Array<CharSequence> //List to display
        val popup = AlertDialog.Builder(this)
        var tempString = ""

        if(file.exists()) {
            val reader = file.bufferedReader()
            val lines = reader.readLines()

            //Process color information from file
            lines.withIndex().forEach { (index,item: String) ->
                tempParts = lines[index].split(",", limit=4)
                colorList.add(tempParts)
                tempString = ("#")
                for (i in 0..2){
                    if(tempParts[i].toInt() < 2){
                        tempString += "0"
                    }
                    tempString += java.lang.Integer.toHexString(tempParts[i].toInt()).toUpperCase()
                }
                tempString += " " + tempParts[3]
                formattedList.add(tempString)
            }

            //Get list ready and populate alert
            displayList = formattedList.toTypedArray<CharSequence>()
            popup.setTitle("Colors").setItems(displayList, DialogInterface.OnClickListener{
                interf,select: Int ->
                red = colorList[select][0].toInt()
                green = colorList[select][1].toInt()
                blue = colorList[select][2].toInt()
                name = colorList[select][3]
                colorName.setText(name)
                seekRed.progress = red
                seekGreen.progress = green
                seekBlue.progress = blue
            })
            popup.show()

            reader.close()
        }
        else{ //Here just in case. This shouldn't need to be shown due to createDefaults()
            Toast.makeText(this, "No colors found", Toast.LENGTH_LONG).show()
        }
    }

    fun showColor(){
        //Display the color
        if(display.holder.surface.isValid) {
            canvas = display.holder.lockCanvas()
            canvas.drawRGB(red, green, blue)
            display.holder.unlockCanvasAndPost(canvas)
        }
    }
}
