/*
 * Copyright (c) 2014, Texas Instruments Incorporated - http://www.ti.com/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*---------------------------------------------------------------------------*/
/**
 * \addtogroup cc1350-bp-platform
 * @{
 *
 * \defgroup cc1350-bp-examples C1350-bp Example Projects
 *
 * Example projects for C1350-bp-based platform.
 * @{
 *
 * \defgroup bme280-demo C1350-bp Demo Project
 *
 *   Example project demonstrating the C1350-bp platform
 *
 *   This example will work for the following boards:
 *   - CC1350 LaunchPads with Sensors BoosterPack
 *
 *   This is an IPv6/RPL-enabled example. Thus, if you have a border router in
 *   your installation (same RDC layer, same PAN ID and RF channel), you should
 *   be able to ping6 this demo node.
 *
 * - sensors      : BME280 sensor is read asynchronously.
 *                  This example will print out readings in a staggered fashion
 *                  every 20 seconds
 *
 * @{
 *
 * \file
 *     Example demonstrating the C1350-bp platform
 */
#include "contiki.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "dev/leds.h"
#include "dev/watchdog.h"
#include "dev/button-hal.h"
#include "random.h"
#include "button-sensor.h"
#include "batmon-sensor.h"
#include "board-peripherals.h"
#include "rf-core/rf-ble.h"
#include "bme280.h"
#include "ti-lib.h"

#include <stdio.h>
#include <stdint.h>
/*---------------------------------------------------------------------------*/
#define CC26XX_DEMO_LOOP_INTERVAL       (CLOCK_SECOND * 20)
#define CC26XX_DEMO_LEDS_PERIODIC       LEDS_YELLOW
#define CC26XX_DEMO_LEDS_BUTTON         LEDS_RED
#define CC26XX_DEMO_LEDS_REBOOT         LEDS_ALL
/*---------------------------------------------------------------------------*/
#define CC26XX_DEMO_TRIGGER_1     BOARD_BUTTON_HAL_INDEX_KEY_LEFT
#define CC26XX_DEMO_TRIGGER_2     BOARD_BUTTON_HAL_INDEX_KEY_RIGHT
/*---------------------------------------------------------------------------*/
static struct etimer et, timer2;
/*---------------------------------------------------------------------------*/
PROCESS(cc26xx_demo_process, "cc26xx demo process");
AUTOSTART_PROCESSES(&cc26xx_demo_process);
/*---------------------------------------------------------------------------*/
/*
 * Update sensor readings in a staggered fashion every SENSOR_READING_PERIOD
 * ticks + a random interval between 0 and SENSOR_READING_RANDOM ticks
 */
#define SENSOR_READING_PERIOD (CLOCK_SECOND * 20)
#define SENSOR_READING_RANDOM (CLOCK_SECOND << 4)
#define TIMESENSWAIT (CLOCK_SECOND * 2)
static void get_bme_reading()
{
    int value;

    //Pressure: 1; Temperature: 2; Humidity: 4;

    value = bme_280_sensor.value(1);
    int dec=value/100;
    int vir=value - dec*100;
    printf("PRESS: %u . %u\n hPa", dec,vir);

   value = bme_280_sensor.value(2);
    int msb = value/100;
   int lsb = value - msb*100; //normalizzazione della temperatura
    printf("TMP: %d . %d\n °C", msb,lsb);

    value = bme_280_sensor.value(4);
    int hd = value/1024;
    int ld = value - hd*1024;
    printf("HUM: %d . %d\n", hd,ld);
}
/*---------------------------------------------------------------------------*/
static void init_sensor_readings(void)
{
    
    SENSORS_ACTIVATE(bme_280_sensor);
     
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(cc26xx_demo_process, ev, data)
{

    PROCESS_BEGIN();
    printf("BME280 sensor demo\n");
    
    etimer_set(&et, CC26XX_DEMO_LOOP_INTERVAL);
etimer_set(&timer2, TIMESENSWAIT);
    init_sensor_readings();
PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&timer2));
    printf("calib %u\n",start_get_calib());
    while(1) {
        //SENSORS_ACTIVATE(bme_280_sensor);
        PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
        etimer_reset(&et);
        get_bme_reading();
    }

    PROCESS_END();
}
/*---------------------------------------------------------------------------*/
/**
 * @}
 * @}
 * @}
 */
