# TinyMatic

TinyMatic was formerly named `HomeDroid` and was created back when Android 2.1 was the lastest version of Android. 

Even though it has been updated to work with the current version of Android, there is a lot of legacy code and techdebt.

The app is database driven and the DB is synced with the CCU periodically, the entry point from where the flow can be followed is `PeriodSyncManager`. 
TinyMatic also supports push notifications via RPC, entry point `RpcForegroundService`´.

On the UI Side, the main screen is obviously named `MainActivity`. 
All screens that show data are built on top of `ServiceContentActivity` and `DataFragment`. 
The UI is notified about new data with broadcast notifications from the data layer.

Any CCU datapoint is parsed and translated into UI by `ListViewGenerator`, including widget content.
