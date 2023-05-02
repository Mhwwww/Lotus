# Content Based Filtering Example

## Producer

Produces lots of random temperature/... values at X locations

## Subscriber

X Subscribers are interested in temperature/... values at their location, if they adhere to a set of rules

(so one subscriber per location)

## Our System

Subscribes to events everywhere, forwards them all to the function, republishes them if they match the ruleset
E.g., all subscribers have the same ruleset (for this demonstration at least)

Because the rules filter out 3/4 of all messages, the edge devices have 75% less load. But this can be changed (by changing the rules.json file or by changing the random distribution of values in CbfPublisher.kt)

