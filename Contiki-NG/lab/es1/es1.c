#include "contiki.h"

#include <stdio.h> /* For printf() */
#include <stdlib.h>

/*---------------------------------------------------------------------------*/
PROCESS(producer, "producer process");
PROCESS(consumer, "consumer process");
AUTOSTART_PROCESSES(&producer, &consumer);

typedef struct
{
  int queue[10];
  int numEl;
} tuple;
static tuple queue;
static process_event_t added_event;
static process_event_t popped_event;

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(producer, ev, data)
{
  static struct etimer et;
  PROCESS_BEGIN();
  queue.numEl = 0;

  added_event = process_alloc_event();
  printf("Hello, producer!!\n");
  srand(0);
  int newData;
  while (1)
  {
    etimer_set(&et, rand() / RAND_MAX);
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et) && queue.numEl < 10);
    newData = rand() % 10;
    queue.queue[queue.numEl++] = newData;
    printf("[PRODUCER] added %d\n", newData);
    process_post(&consumer, added_event, NULL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(consumer, ev, data)
{
  static struct etimer et;

  PROCESS_BEGIN();

  PROCESS_WAIT_EVENT();

  popped_event = process_alloc_event();

  printf("Hello, consumer!!\n");
  srand(0);
  int popped;

  while (1)
  {
    etimer_set(&et, rand() / RAND_MAX);
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et) && queue.numEl > 0);
    popped = queue.queue[0];
    queue.numEl--;
    for (int i = 0; i < queue.numEl; i++)
      queue.queue[i] = queue.queue[i + 1];
    process_post(&producer, popped_event, NULL);
    printf("[CONSUMER] Popped: %d\n", popped);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
