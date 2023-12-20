/*
 * Copyright (c) 2006, Swedish Institute of Computer Science.
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
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

/**
 * \file
 *         Example use of ctimers and rtimers
 * \author
 *         Luca Mottola <luca@sics.se>
 */

#include "contiki.h"

#include <stdio.h> /* For printf() */

/*---------------------------------------------------------------------------*/
static void ctimer_callback(void *data);
#define CTIMER_INTERVAL 2 * CLOCK_SECOND
static struct ctimer print_ctimer;
/*---------------------------------------------------------------------------*/
static void rtimer_callback(struct rtimer *t, void *data);
#define RTIMER_HARD_INTERVAL 2 * RTIMER_SECOND
static struct rtimer print_rtimer;
/*---------------------------------------------------------------------------*/
PROCESS(hello_world_ctimer, "Hello world process");
AUTOSTART_PROCESSES(&hello_world_ctimer);
/*---------------------------------------------------------------------------*/
static void ctimer_callback(void *data){

  printf("%s", (char *)data);
  
  /* Reschedule the ctimer. */
  ctimer_set(&print_ctimer, CTIMER_INTERVAL, ctimer_callback, "Hello world CT\n");  
}
/*---------------------------------------------------------------------------*/
static void rtimer_callback(struct rtimer *t, void *data){

  printf("%s", (char *)data);
  
  /* Reschedule the rtimer. */
  rtimer_set(&print_rtimer, RTIMER_NOW()+RTIMER_HARD_INTERVAL, 0, rtimer_callback, "Hello world RT\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(hello_world_ctimer, ev, data)
{
  PROCESS_BEGIN();


  rtimer_init();

  /* Schedule the rtimer: absolute time! */
  rtimer_set(&print_rtimer, RTIMER_NOW()+RTIMER_HARD_INTERVAL, 0, rtimer_callback, "Hello world RT\n");

  /* Schedule the ctimer. */
  ctimer_set(&print_ctimer, CTIMER_INTERVAL, ctimer_callback, "Hello world CT\n");

  /* Only useful for platform native. */
  PROCESS_WAIT_EVENT();

  PROCESS_END();
}
