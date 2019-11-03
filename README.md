## Reactive WebSocket server ##

This WebSocket server implementation demonstrates two awesome ideas from reactive programming:
1. Using a back-pressure in case of slow client.
2. Applying an actor model to manage a data inside the server.

### Actor model ###

Let's see how it helps to achieve greater throughput. The actor model here is used for sending messages to a client.
MessageSenderImpl class is an actor. It has run() method to execute a real message sending.
If there's messages for sending to a particular client, the Send event is emitted and the actor runner is scheduled.
The actor runner is deadly simple. It's based on concurrent queue (should be replaced with a modern one lock-free impl.)
and a regular executor service (which is actually running an actor task).

### Back-pressure ###

Physically, messages are sending by Jetty transport and WebSocket protocol implementation.
We have async send method with a callback in the specification. Each time a message is sent into the channel, demand will decrease. When demand is zero, no more messages will be sent until demand becomes positive.
Keep in mind that we don't have an actual control how many messages are truly delivered to the client (because of TCP/IP), we operates with some buffer. When the buffer is full, our goal is protecting the application from OOM.




