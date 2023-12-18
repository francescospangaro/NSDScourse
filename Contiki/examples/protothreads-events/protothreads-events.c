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
 *         Example showing how to exchange events between protothreads and schedule them accordingly
 * \author
 *         Luca Mottola <luca@sics.se>
 */

#include "contiki.h"

#include <stdio.h> /* For printf() */
/*---------------------------------------------------------------------------*/
PROCESS(ping_process, "Ping process");
PROCESS(pong_process, "Pong process");
AUTOSTART_PROCESSES(&pong_process,&ping_process);
/*---------------------------------------------------------------------------*/
static process_event_t ping_event;
static process_event_t pong_event;
static int count = 0;
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(ping_process, ev, data)
{
  static struct etimer timer;

  PROCESS_BEGIN();

  /* Make sure the other process has time to start... */
  PROCESS_PAUSE();

  /* Start the ping pong... */
  ping_event=process_alloc_event();
  process_post(&pong_process, ping_event, &count);
  printf("Sent the first ping!\n");
  
  while(1) {

    /* Wait for events: either etimers or pong. */
    PROCESS_WAIT_EVENT();

    if (ev == pong_event) {

      printf("Got a pong: %d!\n",*(int*)data);

      /* Responding with a pong in 2 seconds. */
      etimer_set(&timer, CLOCK_SECOND * 2);

    } else if (ev==PROCESS_EVENT_TIMER){

      count++;
      process_post(&pong_process, ping_event, &count);
      printf("Sent a ping event to pong!\n");
	  
    } else {
      printf("Got an unknonw event!\n");
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(pong_process, ev, data)
{

  PROCESS_BEGIN();
  pong_event=process_alloc_event();
  
  while(1) {

    /* Wait for ping events. */
    PROCESS_WAIT_EVENT();

    if (ev == ping_event) {

      printf("Got a ping: %d!\n",*(int*)data);

      count++;
      process_post(&ping_process, pong_event, &count);
      printf("Sent a pong event to ping!\n");
      
    } else {
      printf("Got an unknonw event!\n");
    }
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
