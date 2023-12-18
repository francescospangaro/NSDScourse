##BME280 Demo
This example demonstrates how read temperature, pressure and humidity values from BME280 with CC1350 + Sensors Boosterpack

###Sensor Init
Before read the values the sensor must be initialized calling the following functions:

```
 SENSORS_ACTIVATE(bme_280_sensor);
```
and **after 2 seconds**
```
 start_get_calib()
```

###Sensor reading

Use this function to read the sensor

```
 bme_280_sensor.value(id)
```
Id identifies the type of sensor to read

Id can be:
* 1 to read pressure value
* 2 to read temperature value
* 4 to read humidity value


