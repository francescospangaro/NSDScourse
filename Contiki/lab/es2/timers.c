#include "contiki.h"

#include <stdio.h> /* For printf() */
#include <stdlib.h>

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

static rtimer_clock_t rtime = 0;
static unsigned long ctime = 0;
static int flag = 0;
static void ctimer_callback(void *data)
{

  printf("%s", (char *)data);
  if (flag == 0)
  {
    long interval = 2;
    rtime = rtimer_arch_now() + (interval * RTIMER_SECOND);
    ctime = (clock_time() / CLOCK_SECOND) + (interval * CLOCK_SECOND);

    ctimer_set(&print_ctimer, ctime - (clock_time() / CLOCK_SECOND), ctimer_callback, "Hello world CT\n");
    flag = 1;
  }
  else
  {
    flag = 0;
    ctimer_set(&print_ctimer, ctime - (clock_time() / CLOCK_SECOND), ctimer_callback, "Hello world CT\n");
  }
}
/*---------------------------------------------------------------------------*/
static void rtimer_callback(struct rtimer *t, void *data)
{
  printf("%s", (char *)data);
  if (flag == 0)
  {
    long interval = 2;
    rtime = rtimer_arch_now() + (interval * RTIMER_SECOND);
    ctime = (clock_time() / CLOCK_SECOND) + (interval * CLOCK_SECOND);
    rtimer_set(&print_rtimer, rtime, 0, rtimer_callback, "Hello world RT\n");
    flag = 1;
  }
  else
  {
    flag = 0;
    rtimer_set(&print_rtimer, rtime, 0, rtimer_callback, "Hello world RT\n");
  }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(hello_world_ctimer, ev, data)
{
  PROCESS_BEGIN();
  srand(0);
  long interval = 2;
  rtime = rtimer_arch_now() + (interval * RTIMER_SECOND);
  ctime = (clock_time() / CLOCK_SECOND) + (interval * CLOCK_SECOND);

  rtimer_init();

  /* Schedule the rtimer: absolute time! */
  rtimer_set(&print_rtimer, rtime, 0, rtimer_callback, "Hello world RT\n");

  /* Schedule the ctimer. */
  ctimer_set(&print_ctimer, ctime - (clock_time() / CLOCK_SECOND), ctimer_callback, "Hello world CT\n");

  /* Only useful for platform native. */
  PROCESS_WAIT_EVENT();

  PROCESS_END();
}
