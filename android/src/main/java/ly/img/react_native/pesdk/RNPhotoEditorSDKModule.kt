package ly.img.react_native.pesdk

import android.app.Activity
import android.support.v7.app.AlertDialog;
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import java.util.List
import java.util.Map
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.bridge.ReactApplicationContext
import ly.img.android.IMGLY
import ly.img.android.PESDK
import ly.img.android.pesdk.PhotoEditorSettingsList
import ly.img.android.pesdk.backend.decoder.ImageSource
import ly.img.android.pesdk.backend.model.state.LoadSettings
import ly.img.android.pesdk.backend.model.state.manager.SettingsList
import ly.img.android.pesdk.kotlin_extension.continueWithExceptions
import ly.img.android.pesdk.ui.activity.PhotoEditorBuilder
import ly.img.android.pesdk.ui.utils.PermissionRequest
import ly.img.android.pesdk.utils.MainThreadRunnable
import ly.img.android.pesdk.utils.SequenceRunnable
import ly.img.android.pesdk.utils.UriHelper
import ly.img.android.sdk.config.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import ly.img.android.pesdk.backend.encoder.Encoder
import ly.img.android.pesdk.backend.model.EditorSDKResult
import ly.img.android.serializer._3.IMGLYFileReader
import ly.img.android.serializer._3.IMGLYFileWriter

import ly.img.react_native.pesdk.TRTCCalling
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config

import sun.net.ext.ExtendedSocketOptions.options
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config

import sun.net.ext.ExtendedSocketOptions.options
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config

import sun.net.ext.ExtendedSocketOptions.options
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config

import sun.net.ext.ExtendedSocketOptions.options
import org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config

import sun.net.ext.ExtendedSocketOptions.options



















class RNPhotoEditorSDKModule(val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ActivityEventListener, PermissionListener {

    companion object {
        // This number must be unique. It is public to allow client code to change it if the same value is used elsewhere.
        var EDITOR_RESULT_ID = 29064
    }

    init {
        reactContext.addActivityEventListener(this)
    }

    private var currentSettingsList: PhotoEditorSettingsList? = null
    private var currentPromise: Promise? = null
    private var currentConfig: Configuration? = null

    @ReactMethod
    fun login(options: ReadableMap, promise: Promise) {
        val sdkAppId: Int = options.getInt("sdkAppId")
        val userId: String = options.getString("userId")
        val userSig: String = options.getString("userSig")
        TRTCCalling.sharedInstance(reactContext).addDelegate(this)
        TRTCCalling.sharedInstance(reactContext).login(sdkAppId, userId, userSig, object : ActionCallBack() {
            fun onError(code: Int, msg: String?) {
                promise.reject(code.toString() + "", msg)
            }

            fun onSuccess() {
                promise.resolve("")
            }
        })
    }

    @ReactMethod
    fun logout(promise: Promise) {
        TRTCCalling.sharedInstance(reactContext).logout(object : ActionCallBack() {
            fun onError(code: Int, msg: String?) {
                promise.reject(code.toString() + "", msg)
            }

            fun onSuccess() {
                promise.resolve("")
            }
        })
    }

    // 呼叫他人
    @ReactMethod
    fun call(options: ReadableMap) {
        val userId: String = options.getString("userId")
        val callType: Int = options.getInt("callType")
        TRTCCalling.sharedInstance(reactContext).call(userId, callType)
    }

    // 接听来电回调
    @ReactMethod
    fun accept() {
        TRTCCalling.sharedInstance(reactContext).accept()
    }

    // 结束通话 挂断
    @ReactMethod
    fun hangup() {
        TRTCCalling.sharedInstance(reactContext).hangup()
    }

    // 拒绝来电回调
    @ReactMethod
    fun reject() {
        TRTCCalling.sharedInstance(reactContext).reject()
    }

    // 是否开免提
    @ReactMethod
    fun setHandsFree(isHandsFree: Boolean) {
        TRTCCalling.sharedInstance(reactContext).setHandsFree(isHandsFree)
    }

    override fun onError(code: Int, msg: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putInt("code", code)
        map.putString("message", msg)
        sendEvent(reactContext, "onError", map)
    }

    override fun onInvited(sponsor: String?, userIdList: List<String?>?, isFromGroup: Boolean, callType: Int) {
        val map: WritableMap = Arguments.createMap()
        map.putString("sponsor", sponsor)
        //    map.putArray("userIdList", Arguments.fromArray(userIdList));
        map.putBoolean("isFromGroup", isFromGroup)
        map.putInt("callType", callType)
        sendEvent(reactContext, "onInvited", map)
    }

    override fun onGroupCallInviteeListUpdate(userIdList: List<String?>?) {
        val map: WritableMap = Arguments.createMap()
        map.putArray("userIds", Arguments.fromArray(userIdList))
        sendEvent(reactContext, "onGroupCallInviteeListUpdate", map)
    }

    override fun onUserEnter(userId: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        sendEvent(reactContext, "onUserEnter", map)
    }

    override fun onUserLeave(userId: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        sendEvent(reactContext, "onUserLeave", map)
    }

    override fun onReject(userId: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        sendEvent(reactContext, "onReject", map)
    }

    override fun onNoResp(userId: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        sendEvent(reactContext, "onNoResp", map)
    }

    override fun onLineBusy(userId: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        sendEvent(reactContext, "onLineBusy", map)
    }

    override fun onCallingCancel() {
        val map: WritableMap = Arguments.createMap()
        sendEvent(reactContext, "onCallingCancel", map)
    }

    override fun onCallingTimeout() {
        val map: WritableMap = Arguments.createMap()
        sendEvent(reactContext, "onCallingTimeout", map)
    }

    override fun onCallEnd() {
        val map: WritableMap = Arguments.createMap()
        sendEvent(reactContext, "onCallEnd", map)
    }

    override fun onUserVideoAvailable(userId: String?, isVideoAvailable: Boolean) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        map.putBoolean("available", isVideoAvailable)
        sendEvent(reactContext, "onUserVideoAvailable", map)
    }

    override fun onUserAudioAvailable(userId: String?, isVideoAvailable: Boolean) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uid", userId)
        map.putBoolean("available", isVideoAvailable)
        sendEvent(reactContext, "onUserAudioAvailable", map)
    }

    override fun onUserVoiceVolume(volumeMap: Map<String?, Int?>?) {
        val map: WritableMap = Arguments.createMap()
        sendEvent(reactContext, "onUserVoiceVolume", map)
    }

    @ReactMethod
    fun unlockWithLicense(license: String) {
        PESDK.initSDKWithLicenseData(license)
        IMGLY.authorize()
    }

    @ReactMethod
    fun theShow(message: String, duration: Int) {
        Toast.makeText(ReactApplicationContext, message, duration).show()
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, intent: Intent?) {
        val data = try {
            intent?.let { EditorSDKResult(it) }
        } catch (e: EditorSDKResult.NotAnImglyResultException) {
            null
        } ?: return // If data is null the result is not from us.

        when (requestCode) {
            EDITOR_RESULT_ID -> {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> {
                        currentPromise?.resolve(null)
                    }
                    Activity.RESULT_OK -> {
                        SequenceRunnable("Export Done") {
                            val sourcePath = data.sourceUri
                            val resultPath = data.resultUri

                            val serializationConfig = currentConfig?.export?.serialization
                            val settingsList = data.settingsList

                            val serialization: Any? = if (serializationConfig?.enabled == true) {
                                skipIfNotExists {
                                    settingsList.let { settingsList ->
                                        if (serializationConfig.embedSourceImage == true) {
                                            Log.i("ImgLySdk", "EmbedSourceImage is currently not supported by the Android SDK")
                                        }
                                        when (serializationConfig.exportType) {
                                            SerializationExportType.FILE_URL -> {
                                                val uri = serializationConfig.filename?.let {
                                                    Uri.parse(it)
                                                }
                                                        ?: Uri.fromFile(File.createTempFile("serialization", ".json"))
                                                Encoder.createOutputStream(uri).use { outputStream ->
                                                    IMGLYFileWriter(settingsList).writeJson(outputStream)
                                                }
                                                uri.toString()
                                            }
                                            SerializationExportType.OBJECT -> {
                                                ReactJSON.convertJsonToMap(
                                                        JSONObject(
                                                                IMGLYFileWriter(settingsList).writeJsonAsString()
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } ?: run {
                                    Log.i("ImgLySdk", "You need to include 'backend:serializer' Module, to use serialisation!")
                                    null
                                }
                            } else {
                                null
                            }

                            currentPromise?.resolve(
                                    reactMap(
                                            "image" to when (currentConfig?.export?.image?.exportType) {
                                                ImageExportType.DATA_URL -> resultPath?.let {
                                                    val imageSource = ImageSource.create(it)
                                                    "data:${imageSource.imageFormat.mimeType};base64,${imageSource.asBase64}"
                                                }
                                                ImageExportType.FILE_URL -> resultPath?.toString()
                                                else -> resultPath?.toString()
                                            },
                                            "hasChanges" to (sourcePath?.path != resultPath?.path),
                                            "serialization" to serialization
                                    )
                            )
                        }()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
    }

    @ReactMethod
    fun present(image: String?, config: ReadableMap?, serialization: String?, promise: Promise) {
        IMGLY.authorize()

        val settingsList = PhotoEditorSettingsList()

        currentSettingsList = settingsList
        currentConfig = ConfigLoader.readFrom(config?.toHashMap() ?: mapOf()).also {
            it.applyOn(settingsList)
        }
        currentPromise = promise

        settingsList.configure<LoadSettings> { loadSettings ->
            image?.also {
                if (it.startsWith("data:")) {
                    loadSettings.source = UriHelper.createFromBase64String(it.substringAfter("base64,"))
                } else {
                    val potentialFile = continueWithExceptions { File(it) }
                    if (potentialFile?.exists() == true) {
                        loadSettings.source = Uri.fromFile(potentialFile)
                    } else {
                        loadSettings.source = ConfigLoader.parseUri(it)
                    }
                }
            }
        }

        readSerialisation(settingsList, serialization, image == null)

        if (checkPermissions()) {
            startEditor(settingsList)
        }
    }

    private fun checkPermissions(): Boolean {
        (currentActivity as? PermissionAwareActivity)?.also {
            var haveAllPermissions = true
            for (permission in PermissionRequest.NEEDED_EDITOR_PERMISSIONS) {
                if (it.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    haveAllPermissions = false
                }
            }
            if (!haveAllPermissions) {
                it.requestPermissions(PermissionRequest.NEEDED_EDITOR_PERMISSIONS, 0, this)
                return false
            }
        }

        return true
    }

    private fun readSerialisation(settingsList: SettingsList, serialization: String?, readImage: Boolean) {
        if (serialization != null) {
            skipIfNotExists {
                IMGLYFileReader(settingsList).also {
                    it.readJson(serialization, readImage)
                }
            }
        }
    }

    private fun startEditor(settingsList: PhotoEditorSettingsList?) {
        val currentActivity = this.currentActivity
                ?: throw RuntimeException("Can't start the Editor because there is no current activity")
        if (settingsList != null) {
            (currentActivity as? PermissionAwareActivity)?.also {
                for (permission in PermissionRequest.NEEDED_EDITOR_PERMISSIONS) {
                    if (it.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                }
            }
            MainThreadRunnable {
                PhotoEditorBuilder(currentActivity)
                        .setSettingsList(settingsList)
                        .startActivityForResult(currentActivity, EDITOR_RESULT_ID)
            }()
        }
    }

    operator fun WritableMap.set(id: String, value: Boolean) = this.putBoolean(id, value)
    operator fun WritableMap.set(id: String, value: String?) = this.putString(id, value)
    operator fun WritableMap.set(id: String, value: Double) = this.putDouble(id, value)
    operator fun WritableMap.set(id: String, value: Float) = this.putDouble(id, value.toDouble())
    operator fun WritableMap.set(id: String, value: WritableArray?) = this.putArray(id, value)
    operator fun WritableMap.set(id: String, value: Int) = this.putInt(id, value)
    operator fun WritableMap.set(id: String, value: WritableMap?) = this.putMap(id, value)

    fun reactMap(vararg pairs: Pair<String, Any?>): WritableMap {
        val map = Arguments.createMap()

        for (pair in pairs) {
            val id = pair.first
            when (val value = pair.second) {
                is String? -> map[id] = value
                is Boolean -> map[id] = value
                is Double -> map[id] = value
                is Float -> map[id] = value
                is Int -> map[id] = value
                is WritableMap? -> map[id] = value
                is WritableArray? -> map[id] = value
                else -> if (value == null) {
                    map.putNull(id)
                } else {
                    throw RuntimeException("Type not supported by WritableMap")
                }
            }
        }

        return map
    }


    object ReactJSON {
        @Throws(JSONException::class)
        fun convertJsonToMap(jsonObject: JSONObject): WritableMap? {
            val map: WritableMap = WritableNativeMap()
            val iterator: Iterator<String> = jsonObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val value: Any = jsonObject.get(key)
                when (value) {
                    is JSONObject -> {
                        map.putMap(key, convertJsonToMap(value))
                    }
                    is JSONArray -> {
                        map.putArray(key, convertJsonToArray(value))
                    }
                    is Boolean -> {
                        map.putBoolean(key, value)
                    }
                    is Int -> {
                        map.putInt(key, value)
                    }
                    is Double -> {
                        map.putDouble(key, value)
                    }
                    is String -> {
                        map.putString(key, value)
                    }
                    else -> {
                        map.putString(key, value.toString())
                    }
                }
            }
            return map
        }

        @Throws(JSONException::class)
        fun convertJsonToArray(jsonArray: JSONArray): WritableArray? {
            val array: WritableArray = WritableNativeArray()
            for (i in 0 until jsonArray.length()) {
                when (val value: Any = jsonArray.get(i)) {
                    is JSONObject -> {
                        array.pushMap(convertJsonToMap(value))
                    }
                    is JSONArray -> {
                        array.pushArray(convertJsonToArray(value))
                    }
                    is Boolean -> {
                        array.pushBoolean(value)
                    }
                    is Int -> {
                        array.pushInt(value)
                    }
                    is Double -> {
                        array.pushDouble(value)
                    }
                    is String -> {
                        array.pushString(value)
                    }
                    else -> {
                        array.pushString(value.toString())
                    }
                }
            }
            return array
        }
    }

    override fun getName() = "RNPhotoEditorSDK"

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray): Boolean {
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startEditor(currentSettingsList)
        return false
    }


}
