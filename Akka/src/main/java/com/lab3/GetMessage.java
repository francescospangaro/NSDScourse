package com.lab3;

public record GetMessage(String name) {
    // Messages in akka better be immutable, not a standard, but messages may be modified accidentally while
    // working with them, so if they are not immutable it becomes very hard to debug code and communication,
    // since messages changes.

    // We use this class only as a message type to trigger the onMessage clause
}
