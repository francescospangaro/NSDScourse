#include "contiki.h"
#include "net/routing/routing.h"
#include "random.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define CLIENT_PORT	8765
#define SERVER_PORT	5678

static struct simple_udp_connection udp_conn;

#define REFRESH_INTERVAL	(5 * CLOCK_SECOND)
#define SWITCH_INTERVAL	        (60 * CLOCK_SECOND)
/*---------------------------------------------------------------------------*/
PROCESS(soft_state_client_process, "Soft state client");
AUTOSTART_PROCESSES(&soft_state_client_process);
/*---------------------------------------------------------------------------*/
static struct ctimer switch_ctimer;
static void ctimer_callback(void *data)
{
  uint8_t* on_off = (uint8_t*)data;
  *on_off = !*on_off;
  
  /* Reschedule the ctimer. */
  ctimer_set(&switch_ctimer, SWITCH_INTERVAL, ctimer_callback, data);  
  LOG_INFO("Switching state...\n");
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(soft_state_client_process, ev, data)
{
  static struct etimer periodic_timer;
  uip_ipaddr_t dest_ipaddr;

  PROCESS_BEGIN();

  static uint8_t on_off = 0;
  static uint8_t dummy = 0;
  
  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, CLIENT_PORT, NULL, SERVER_PORT, NULL);

  ctimer_set(&switch_ctimer, SWITCH_INTERVAL, ctimer_callback, &on_off);
  etimer_set(&periodic_timer, REFRESH_INTERVAL);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));

    if(NETSTACK_ROUTING.node_is_reachable()
       && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)){
      if (on_off) {
	LOG_INFO("Sending state refresh to ");
	LOG_INFO_6ADDR(&dest_ipaddr);
	LOG_INFO_("\n");
	simple_udp_sendto(&udp_conn, &dummy, sizeof(uint8_t), &dest_ipaddr);
      } else{
	LOG_INFO("Keeping shut!\n");
      }
    } else {
      LOG_INFO("Not reachable yet!\n");
    }
    etimer_set(&periodic_timer, REFRESH_INTERVAL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
