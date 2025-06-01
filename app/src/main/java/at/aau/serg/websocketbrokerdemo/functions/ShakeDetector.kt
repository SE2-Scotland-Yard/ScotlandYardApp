package at.aau.serg.websocketbrokerdemo.functions

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var lastUpdate = 0L
    private var lastShake = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    var onShake: (() -> Unit)? = null

    fun start() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastUpdate) > 100) {
            val diffTime = (currentTime - lastUpdate)
            lastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = (Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime) * 10000
            if (speed > 15 && (currentTime - lastShake) > 2000) {
                lastShake = currentTime
                onShake?.invoke()
            }
            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}