## Websocket Server
URL: wss://master.tv.labs-exit.de/geover-ws/api

### JSON-Format
{
    "sender": "{SENDER_ID}",
    "target": "{TARGET_URN}",
    "request": "{REQUEST_ID}",
    "receiver": "{SENDER_ID}",
    "receivers": ["{SENDER_ID_1}", "{SENDER_ID_2}", "{SENDER_ID_3}", ...],
    "data": {
        "sensor": "{SENSOR_ID}",
        ...
    }
}

### Example JSON

{
    "target": "urn:tlabs:geover:ws:receiver:all",
    "data": {
        "sensor": "abcd1234",
        ...
    }
}

The **red** properties are managed by the server and should not be set.
The other properties (except data) are all optional (or target dependent).
The data property should contain the actual wind data and sensor info (the wind data has not been specified yet).
The target property specifies how the message should be forwarded. Please see following table

### Target URN 
//Special target. Server sends a message back with some info (e.g. own sender ID, connected clients, etc.).
urn:tlabs:geover:ws:info
//Forward message to all connected clients (including myself). This is also default if no target property is set.
urn:tlabs:geover:ws:receiver:all
//Forward message to all connected clients (excluding myself).
urn:tlabs:geover:ws:receiver:others
//Forward message only back to me.
urn:tlabs:geover:ws:receiver:self
//Forward message to a single client. The receiver property must be set and should contain a sender ID.
urn:tlabs:geover:ws:receiver:single
//Forward message to multiple clients. The receivers property must be set and should contain an array of sender IDs.
urn:tlabs:geover:ws:receiver:multiple

The request property is simply passed through unmodified and can be used to measure round trip times or to handle responses.


### Example JSON
{
    "target": "urn:tlabs:geover:ws:receiver:all",
    "data": {
        "sensor": "abcd1234",
        "wind": {
            "direction": 7,
            "directionType": "id",
            "speed": 16.1234,
            "speedType": "kn"
        }
    }
}

#### Possible values for directionType: "id", "deg"
#### Possible values for speedType: "kn", "mph", "kmh", "ms"

