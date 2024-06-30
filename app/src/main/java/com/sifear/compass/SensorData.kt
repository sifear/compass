package com.sifear.compass

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.round

class SensorData : SensorEventListener {
    private lateinit var context: Context;
    private lateinit var sensorManager: SensorManager
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val gravityReading = FloatArray(3)

    private var onUpdate: (degree: Float) -> Unit = {}

    constructor(ctx: Context, onUpdate: (degree: Float) -> Unit) {
        this.context = ctx
        this.onUpdate = onUpdate
        sensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            Log.d(accelerometer.toString(), "Default rotator")
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magnetormeter ->
            Log.d(magnetormeter.toString(), "Default rotator")
            sensorManager.registerListener(
                this,
                magnetormeter,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)?.also { gravity ->
            Log.d(gravity.toString(), "Default rotator")
            sensorManager.registerListener(
                this,
                gravity,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
        } else
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    event.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.size
                )
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }

        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        this.onUpdate(abs(orientationAngles[1]) * 57.29577f)

        Log.d(
            "RESULT",
            "Z: ${orientationAngles[0]} X: ${abs(orientationAngles[1]) * 57.29577f} Y: ${orientationAngles[2]}"
        )
    }
}