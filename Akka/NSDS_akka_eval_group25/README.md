# Evaluation lab - Akka

## Group number: 25

## Group members

- Francesco Spangaro
- Giacomo Orsenigo
- Federico Saccani

## Description of message flows
Il SensorDataProcessor (main) inizia inviando ai sensori la referenza del dispatcher.
Il dispatcher inizia in modalità load-balancing.

Il SensorDataProcessor invia ai sensori messaggi GenerateMsg. 
Alla loro ricezione, i sensori inviano al dispatcher dei TemperatureMsg contenenti una temperatura casuale.
Il dispatcher, alla ricezione di ogni TemperatureMsg, esegue azioni diverse in base al suo behaviour:
- Load-Balancer: a ogni sensore è assegnato un processore. I messaggi inviati dal sensore vengono inoltrati al processore a lui assegnato. In caso di ricezione di un messaggio da un sensore senza processore assegnato, gli viene assegnato uno tra i processori con meno sensori.
- Round-Robin: i processori sono contenuti in una lista acceduta in modo sequenziale. A ogni ricezione di messaggio lo inoltriamo al prossimo processo nella lista. Alla fine della lista ricominciamo dal primo.

Il dispatcher, alla ricezione di un DispatchLogicMsg, modifica il suo comportamento in base al valore contenuto nel messaggio.

Alla ricezione di una temperatura negativa, il processore lancia un'eccezione.
Questa viene catturata dal suo supervisore (il dispatcher), che adopera la strategia di resume, in modo da mantenere la vecchia media.