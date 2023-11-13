# Evaluation lab - Apache Kafka

## Group number: 25

## Group members

- Francesco Spangaro
- Giacomo Orsenigo
- Federico Saccani

## Exercise 1

- Number of partitions allowed for inputTopic (min, max):

  min: 1, max: M

- Number of consumers allowed (min, max)

  min: 1, max: N

  - Consumer 1: groupId
  - Consumer 2: groupId
  - ...
  - Consumer n: groupId

  Il numero minimo di consumer è 1, non c'è un numero massimo.
  Questo perché, tutti i consumers saranno nello stesso gruppo, in quanto
  ogni messaggio deve essere stampato 0 o 1 volta. Se ci fossero più gruppi,
  un messaggio potrebbe essere stampato anche più volte, perché ricevuto
  da più consumer di gruppi diversi. Per ottenere questo il groupId passato come
  argomento deve essere lo stesso per tutti i consumer.

## Exercise 2
- Number of partitions allowed for inputTopic (min, max)

  min: 1, max: M
- Number of consumers allowed (min, max)

  min: 1, max: N

    - Consumer 1: group1
    - Consumer 2: group2
    - ...
    - Consumer n: groupN

  In questo caso tutti i consumers ricevono tutti i messaggi
  e tutti conteggiano le occorrenze in modo indipendente.
  Questo è garantito dal fatto che in ogni gruppo sia presente
  un singolo consumer che legge da tutte le partizioni.

Abbiamo settato offsetResetStrategy a "earliest" in modo che alla prima connessione 
di un consumer in un nuovo gruppo, questo inizi a ricevere i messaggi dal primo
messaggio inviato nel topic.

### In alternativa:
- Number of partitions allowed for inputTopic (min, max)

  min: 1, max: 1
- Number of consumers allowed (min, max)

    min: 1, max: N

    - Consumer 1: group1
    - Consumer 2: group1
    - ...
    - Consumer n: group1
    
  In questo caso è solo un consumer che riceve tutti i messaggi, 
    di conseguenza gli altri N-1 consumers non riceveranno nulla.
    Così facendo il singolo consumer potrà contare correttamente 
    le occorrenze delle chiavi di tutti i messaggi. Questo è garantito 
    dal fatto che sia presente una singola partizione e tutti i 
    consumer appartengono allo stesso gruppo.
    
