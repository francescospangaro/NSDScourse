package com.faultTolerance.counter;

public class DataMessage {

    // A different way to realize different behaviours of the data message received
    // just an equivalent way to what is done above, instead of having multiple classes
    // we just switch action based on the parameter's value
    private int code;

    public int getCode() {
        return code;
    }

    public DataMessage(int code) {
        this.code = code;
    }

}
