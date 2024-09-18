package com.example.polio

import android.content.Intent
import androidx.annotation.NonNull
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.folioreader.Config
import com.folioreader.FolioReader
import com.folioreader.model.HighLight
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.locators.ReadLocator.Companion.fromJson
import com.folioreader.ui.base.OnSaveHighlight
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import com.folioreader.util.OnHighlightListener
import com.folioreader.util.ReadLocatorListener
import io.flutter.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : FlutterActivity(), OnHighlightListener, ReadLocatorListener,
    FolioReader.OnClosedListener {

    private val LOG_TAG: String = MainActivity::class.java.getSimpleName()
    private var folioReader: FolioReader? = null

    private val CHANNEL = "folio_reader_sdk"
    var methodChannelResult: MethodChannel.Result? = null
    var flutterEngineInstance: FlutterEngine? = null
    lateinit var orderId: String

    @Override
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        folioReader = FolioReader.get()
            .setOnHighlightListener(this)
            .setReadLocatorListener(this)
            .setOnClosedListener(this)
        getHighlightsAndSave()
        flutterEngineInstance = flutterEngine


        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            try {
                methodChannelResult = result
                if (call.method.equals("openFolioSDK")) {
                    Log.d("TAG", "Arguments: " + call.arguments)
                    if (call.arguments != null) {
                        OpenFolio(call.arguments as String)
                    }
                } else {
                    result.notImplemented()
                }
            } catch (e: Exception) {
                Log.d("TAG", e.toString())
            }
        }
    }


    fun OpenFolio(url: String) {
        val readLocator = getLastReadLocator()

        var config = getSavedConfig(applicationContext)
        if (config == null) config = Config()
        config.allowedDirection = Config.AllowedDirection.VERTICAL_AND_HORIZONTAL

        folioReader?.setReadLocator(readLocator)?.setConfig(config, true)?.openBook(url, "true")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getLastReadLocator(): ReadLocator? {
        val jsonString = loadAssetTextAsString("Locators/LastReadLocators/last_read_locator_1.json")
        return fromJson(jsonString)
    }

    override fun saveReadLocator(readLocator: ReadLocator) {
        Log.i(
            LOG_TAG,
            "-> saveReadLocator -> " + readLocator.toJson()
        )
    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */
    private fun getHighlightsAndSave() {
        Thread {
            var highlightList: ArrayList<HighLight?>? = null
            val objectMapper = ObjectMapper()
            try {
                highlightList = objectMapper.readValue(
                    loadAssetTextAsString("highlights/highlights_data.json"),
                    object :
                        TypeReference<List<HighLight?>?>() {})
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (highlightList == null) {
                folioReader?.saveReceivedHighLights(highlightList, OnSaveHighlight {
                    //You can do anything on successful saving highlight list
                })
            }
        }.start()
    }

    private fun loadAssetTextAsString(name: String): String? {
        var `in`: BufferedReader? = null
        try {
            val buf = StringBuilder()
            val `is` = assets.open(name)
            `in` = BufferedReader(InputStreamReader(`is`))
            var str: String?
            var isFirst = true
            while (`in`.readLine().also { str = it } != null) {
                if (isFirst) isFirst = false else buf.append('\n')
                buf.append(str)
            }
            return buf.toString()
        } catch (e: IOException) {
            Log.e("HomeActivity", "Error opening asset $name")
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    Log.e("HomeActivity", "Error closing asset $name")
                }
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        FolioReader.clear()
    }

    override fun onHighlight(highlight: HighLight, type: HighLight.HighLightAction) {

    }

    override fun onFolioReaderClosed() {
        TODO("Not yet implemented")
    }


}