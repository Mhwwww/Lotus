# Message Condesing

Publishers publish huuuuuuge chunks of data (because they are not controlled by us or are edge devices that cant do it differently)

- It creates a JSON Object with 1 important Key (`importantKey`) with a random Int in it, and `numberOfUnnecessaryJson` unnecessary elements

Subscribers only need a very small part of this data (same reasons as above).

- The function just returns `req.body["message"]["importantKey"]`

This time we have multiple subscribers at the same location, so that we also can say that we only perform the comuitation of condensing the message once (maybe this is deduplication?)