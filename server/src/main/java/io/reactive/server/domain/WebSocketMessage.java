package io.reactive.server.domain;

import java.io.Serializable;

/**
 * @author Denis M. Gabaydulin
 * @since 11.01.18
 */
public class WebSocketMessage implements Serializable {
    private final String payload;
    private final long timestamp;

    // options for sender
    protected transient boolean expirable = true;
    private transient RecipientMode mode;
    private transient String sessionId;

    public WebSocketMessage(String payload, long timestamp) {
        this.payload = payload;
        this.timestamp = timestamp;
        // by default, send a message to all connections
        this.mode = RecipientMode.ALL;
    }

    public String getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RecipientMode getMode() {
        return mode;
    }

    public void setMode(RecipientMode mode) {
        this.mode = mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isExpirable() {
        return expirable;
    }
}
