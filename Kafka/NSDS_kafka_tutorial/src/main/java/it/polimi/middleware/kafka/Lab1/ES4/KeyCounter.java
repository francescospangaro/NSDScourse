package it.polimi.middleware.kafka.Lab1.ES4;

import java.util.HashMap;
import java.util.Map;

public class KeyCounter {
    private Map<String,Integer> values;

    public KeyCounter(){
        values = new HashMap<>();
    }

    public void incrementKeyCount(String key){
        int counter=0;
        if(values.containsKey(key)){
            counter = values.get(key);
        }
        values.put(key, counter+1);
    }

    public int getKeyCount(String key){
        return values.get(key);
    }

}
