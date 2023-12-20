#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY 1
#define UDP_CLIENT_PORT 8765
#define UDP_SERVER_PORT 5678

static struct simple_udp_connection udp_conn;
static struct etimer periodic_timer;
static struct etimer sleep_timer;

#define MAX_READINGS 10
#define SEND_INTERVAL (60 * CLOCK_SECOND)
#define SEND_TIMEOUT (2 * CLOCK_SECOND)
#define FAKE_TEMPS 5

static unsigned readings[MAX_READINGS];
static unsigned next_reading = 0;

/*---------------------------------------------------------------------------*/
PROCESS(udp_client_process, "UDP client");
AUTOSTART_PROCESSES(&udp_client_process);
/*---------------------------------------------------------------------------*/
static unsigned
get_temperature()
{
  static unsigned fake_temps[FAKE_TEMPS] = {30, 25, 20, 15, 10};
  return fake_temps[random_rand() % FAKE_TEMPS];
}
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
                const uip_ipaddr_t *sender_addr,
                uint16_t sender_port,
                const uip_ipaddr_t *receiver_addr,
                uint16_t receiver_port,
                const uint8_t *data,
                uint16_t datalen)
{
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_client_process, ev, data)
{
  static uip_ipaddr_t dest_ipaddr;
  static bool disconnected = false;
  static float temperature;
  static float average;
  static uint8_t i;
  PROCESS_BEGIN();
  for (i = 0; i < MAX_READINGS; i++)
  {
    readings[i] = 0;
  }

  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_CLIENT_PORT, NULL,
                      UDP_SERVER_PORT, udp_rx_callback);

  etimer_set(&periodic_timer, SEND_INTERVAL);

  while (1)
  {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
    temperature = (float)get_temperature();
    if (NETSTACK_ROUTING.node_is_reachable() && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr))
    {
      if (disconnected)
      {
        /* Compute average */
        static unsigned sum = 0;
        static unsigned no = 0;
        for (i = 0; i < MAX_READINGS; i++)
        {
          if (readings[i] != 0)
          {
            sum = sum + readings[i];
            no++;
          }
        }
        average = ((float)sum) / no;
        LOG_INFO_("Sending average: %f\n", average);
        simple_udp_sendto(&udp_conn, &average, sizeof(float), &dest_ipaddr);
        disconnected = false;
        for (i = 0; i < MAX_READINGS; i++)
        {
          readings[i] = 0;
        }
        etimer_set(&sleep_timer, SEND_TIMEOUT);
        PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&sleep_timer));
      }
      LOG_INFO_("Sending temperature: %f\n", temperature);
      simple_udp_sendto(&udp_conn, &temperature, sizeof(float), &dest_ipaddr);
    }
    else
    {
      disconnected = true;
      LOG_INFO_("Disconnected!\n");

      /* Add reading */
      LOG_INFO_("Saving temperature: %f\n", temperature);
      readings[next_reading++] = temperature;
      if (next_reading == MAX_READINGS)
      {
        next_reading = 0;
      }
    }
    etimer_reset(&periodic_timer);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
