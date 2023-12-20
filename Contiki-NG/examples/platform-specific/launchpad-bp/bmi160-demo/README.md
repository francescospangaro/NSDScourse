##BMI160 Demo
This example demonstrates how read accelerometer and gyroscope samples from BMI160 with CC1350 + Sensors Boosterpack


###Sensor reading

In order to read the BMI160, first of all, you have to declare a variable of this struct

```
 struct bmi160_sensor_data {
 	/*! X-axis sensor data */
 	int16_t x;
 	/*! Y-axis sensor data */
 	int16_t y;
 	/*! Z-axis sensor data */
 	int16_t z;
 	/*! sensor time */
 	uint32_t sensortime;
 };
```
In this struct the BMI160's driver puts the readings for accelerometer and gyroscope, axis by axis

Then you can use this function 

```
int bmi160_custom_value(id, &data_a, &data_g);
```
The function returns 1 or 0 if the operation succeed or not and requires an id field to identify the operating mode, 
the structure where put accelerometer sample and the structure for gyroscope sample.

Id can be:
* 1 To read only Accel data 
* 2 To read only Gyro data
* 3 To read both Accel and Gyro data

Others can be viewed in bmi160.c file or in datasheet


