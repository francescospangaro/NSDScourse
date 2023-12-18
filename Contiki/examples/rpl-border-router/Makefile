CONTIKI_PROJECT = border-router
all: $(CONTIKI_PROJECT)
CONTIKI = ../..

# The BR is either native or embedded, and in the latter case must support SLIP
PLATFORMS_EXCLUDE = nrf52dk

# Include RPL BR module
MODULES += os/services/rpl-border-router
# Include webserver module
MODULES_REL += webserver
# Include optional target-specific module
include $(CONTIKI)/Makefile.identify-target
MODULES_REL += $(TARGET)

include $(CONTIKI)/Makefile.include

PREFIX ?= fd00::1/64

connect-router-ACM0:	$(CONTIKI)/tools/serial-io/tunslip6
	sudo $(CONTIKI)/tools/serial-io/tunslip6 -L -s ttyACM0 $(PREFIX)

connect-router-ACM2:	$(CONTIKI)/tools/serial-io/tunslip6
	sudo $(CONTIKI)/tools/serial-io/tunslip6 -L -s ttyACM2 $(PREFIX)
