package com.example.imuapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusText: TextView
    private lateinit var dataDisplay: TextView

    private var isRecording = false
    private val imuDataList = ArrayList<IMUData>()
    private var startTime: Long = 0

    // 用于存储最新的传感器数据
    private val latestAccData = FloatArray(3)
    private val latestGyroData = FloatArray(3)
    private var lastAccTimestamp: Long = 0
    private var lastGyroTimestamp: Long = 0

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    // IMU数据类
    data class IMUData(
        val timestamp: Long,
        val accX: Float,
        val accY: Float,
        val accZ: Float,
        val gyroX: Float,
        val gyroY: Float,
        val gyroZ: Float
    ) {
        fun toCsvString(): String {
            return String.format(
                Locale.US,
                "%.3f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                timestamp / 1000000.0, // 纳秒转毫秒
                accX, accY, accZ, gyroX, gyroY, gyroZ
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSensors()
        setupClickListeners()

        // 检查并请求权限
        if (!checkPermissions()) {
            requestPermissions()
        }
        checkStoragePermission();
    }

    private fun initViews() {
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        statusText = findViewById(R.id.statusText)
        dataDisplay = findViewById(R.id.dataDisplay)
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // 检查传感器可用性
        if (accelerometer == null) {
            Toast.makeText(this, "设备不支持加速度计", Toast.LENGTH_LONG).show()
            startButton.isEnabled = false
        }
        if (gyroscope == null) {
            Toast.makeText(this, "设备不支持陀螺仪", Toast.LENGTH_LONG).show()
            startButton.isEnabled = false
        }

        // 显示传感器信息
        updateSensorInfo()
    }

    private static int STORAGE_PERMISSION_CODE: Int = 1

    // 检查权限
    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，可以保存文件
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要存储权限来保存数据", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSensorInfo() {
        val info = StringBuilder()
        info.append("传感器信息：\n")
        accelerometer?.let {
            info.append("加速度计：${it.name}\n")
        }
        gyroscope?.let {
            info.append("陀螺仪：${it.name}\n")
        }
        dataDisplay.text = info.toString()
    }

    private fun setupClickListeners() {
        startButton.setOnClickListener {
            startRecording()
        }

        stopButton.setOnClickListener {
            stopRecording()
        }
    }

    private fun startRecording() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        isRecording = true
        startTime = System.nanoTime()
        imuDataList.clear()

        // 注册传感器监听器
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }

        // 更新UI
        startButton.isEnabled = false
        stopButton.isEnabled = true
        statusText.text = "状态：正在采集数据..."

        Toast.makeText(this, "开始采集IMU数据", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false

        // 注销传感器监听器
        sensorManager.unregisterListener(this)

        // 更新UI
        startButton.isEnabled = true
        stopButton.isEnabled = false
        statusText.text = "状态：数据采集已停止"

        // 保存数据
        saveDataToFile()

        Toast.makeText(this, "采集完成，共采集 ${imuDataList.size} 条数据", Toast.LENGTH_SHORT).show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRecording || event == null) return

        val currentTime = System.nanoTime() - startTime

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                latestAccData[0] = event.values[0]
                latestAccData[1] = event.values[1]
                latestAccData[2] = event.values[2]
                lastAccTimestamp = currentTime
            }
            Sensor.TYPE_GYROSCOPE -> {
                latestGyroData[0] = event.values[0]
                latestGyroData[1] = event.values[1]
                latestGyroData[2] = event.values[2]
                lastGyroTimestamp = currentTime
            }
        }

        // 记录IMU数据
        val timestamp = maxOf(lastAccTimestamp, lastGyroTimestamp)

        val data = IMUData(
            timestamp,
            latestAccData[0], latestAccData[1], latestAccData[2],
            latestGyroData[0], latestGyroData[1], latestGyroData[2]
        )

        imuDataList.add(data)

        // 每100条数据更新一次显示
        if (imuDataList.size % 100 == 0) {
            updateDisplay(data)
        }
    }

    private fun updateDisplay(data: IMUData) {
        runOnUiThread {
            val displayText = String.format(
                Locale.US,
                "已采集数据：%d 条\n\n" +
                        "最新数据：\n" +
                        "时间戳：%.3f ms\n\n" +
                        "加速度计 (m/s²)：\n" +
                        "  X: %8.4f\n" +
                        "  Y: %8.4f\n" +
                        "  Z: %8.4f\n\n" +
                        "陀螺仪 (rad/s)：\n" +
                        "  X: %8.4f\n" +
                        "  Y: %8.4f\n" +
                        "  Z: %8.4f",
                imuDataList.size,
                data.timestamp / 1000000.0,
                data.accX, data.accY, data.accZ,
                data.gyroX, data.gyroY, data.gyroZ
            )
            dataDisplay.text = displayText
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 传感器精度变化时调用，通常不需要处理
    }

    private fun saveDataToFile() {
        if (imuDataList.isEmpty()) {
            Toast.makeText(this, "没有数据可保存", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 创建文件名
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            val fileName = "IMU_Data_${dateFormat.format(Date())}.csv"

            // 获取外部存储目录
            val externalDir = Environment.getExternalStorageDirectory()
            val imuDir = File(externalDir, "IMUData")
            if (!imuDir.exists()) {
                imuDir.mkdirs()
            }

            val file = File(imuDir, fileName)
            val writer = FileWriter(file)

            // 写入CSV头部
            writer.write("timestamp_ms,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z\n")

            // 写入数据
            for (data in imuDataList) {
                writer.write("${data.toCsvString()}\n")
            }

            writer.close()

            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "数据已保存到：${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "保存失败：${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "需要存储权限来保存数据文件", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            sensorManager.unregisterListener(this)
        }
    }
}