##TMP007 Demo
This example demonstrates how measure ambient temperature or temperature of an object without having to be in contact with it.



###Sensor reading

In order to read the BMI160, first of all, you have to call the following function

```
 value = tmp_007_sensor.value(TMP_007_SENSOR_TYPE_ALL);
```
and have this check
```
if(value == CC26XX_SENSOR_READING_ERROR) {
        printf("TMP: Ambient Read Error\n");
        return;
    }
```
Then you can read ambient temperature with

```
tmp_007_sensor.value(TMP_007_SENSOR_TYPE_AMBIENT);
```

and the object temperature 

```
 tmp_007_sensor.value(TMP_007_SENSOR_TYPE_OBJECT);
```




