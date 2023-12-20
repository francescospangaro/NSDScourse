# Evaluation lab - Contiki-NG

## Group number: 25

## Group members

- Francesco Spangaro
- Giacomo Orsenigo
- Federico Saccani

## Solution description

The client, every 60 seconds, generates a temperature reading and checks if he's connected to the server.
If so, the client sends the reading to the server. If the client is not connected, he starts batching every temperature he generates while he's disconnected, overwriting the oldest temperatures saved every MAX_READINGS.
Once the client reconnects to the server, he firstly computes, then sends, the average to the server, he then starts sending back temperatures to the server once every 60 seconds.
Since the send buffer only has one slot, the client sends the average, then waits two seconds to let the server read the average he sent before sending the newly read temperature.
The server batches the client's data, computing the average each time he gets a new read. 
Once the server receives more than MAX_READINGS he overwrites the oldest ones and recomputes the average.
The server may receive data from more than MAX_RECEIVERS clients.