# OBP3 DES

The DES semantics expose one action for each event at the next event time.
Enumerating actions does not modify the configuration. Executing an action
processes only its selected event; any other events scheduled for that same
time remain queued and are available as actions at the next step.
