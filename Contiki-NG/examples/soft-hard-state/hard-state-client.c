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

#define SWITCH_INTERVAL	        (20 * CLOCK_SECOND)

static struct simple_udp_connection udp_conn;

/*---------------------------------------------------------------------------*/
PROCESS(hard_state_client_process, "Soft state client");
AUTOSTART_PROCESSES(&hard_state_client_process);
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(hard_state_client_process, ev, data)
{
  static struct etimer periodic_timer;
  uip_ipaddr_t dest_ipaddr;

  PROCESS_BEGIN();

  static uint8_t on_off = 0;
  
  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, CLIENT_PORT, NULL, SERVER_PORT, NULL);

  etimer_set(&periodic_timer, SWITCH_INTERVAL);

  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));

    if(NETSTACK_ROUTING.node_is_reachable()
       && NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr)){
      LOG_INFO("Sending current %u state to ", on_off);
      LOG_INFO_6ADDR(&dest_ipaddr);
      LOG_INFO_("\n");
      simple_udp_sendto(&udp_conn, &on_off, sizeof(uint8_t), &dest_ipaddr);

      LOG_INFO("Switching state...\n");
      on_off = !on_off;
    } else {
      LOG_INFO("Not reachable yet!\n");
    }
    etimer_set(&periodic_timer, SWITCH_INTERVAL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
