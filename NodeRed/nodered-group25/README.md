# Evaluation lab - Node-RED

## Group number: 25

## Group members

- Francesco Spangaro
- Giacomo Orsenigo
- Federico Saccani

## Description of message flows
The Telegram_Receiver node receives messages from the User, sent to the telegram bot.
The parse_request function gets the time and type of request, saving all in message.requestInfo.
In the flow is saved the number of times each request type arrives.
If the request doesn't match our criteria, the bot forwards an error message to the user.
We add the city and the country to the msg.location, which is then sent to the openweathermap node that we set to generate a forecast for 5 days from now.
The parse_response node parses openweathermap's message by looking at the msg.requestInfo variables. 
We access msg.payload[8] for tomorrow's data, since openweathermap sets data in an array of 3h timestamps, so the 8th position corresponds to the weather for 24h from now.
The same can be said for the msg.payload[16], that contains the data of two days from now.
The message is then sent through the text node, and then through the Telegram_Sender node, which sends the response to the user.

We used an inject node set to iterate each minute for the file saves.
Each minute the save_into_file node passes the data to write in the file to the File_Writer node, which writes it into the /tmp/stats file.
The save_into_file node also resets to 0 all the data saved in the flow for the counter.

The messages the bot expects are:
What tomorrow's forecast in Milan?
What tomorrow's wind speed in Milan?
What's the expected forecast in two days in Milan?
What's the expected wind speed in two days in Milan?

It works with every italian city.

## Extensions 

## Bot URL 
https://t.me/NR_group25_bot
API KEY: 6513496220:AAHKrlD0zqp_QD2EB6gAGgvL9goYfDHBvyY
