# TinyMatic

TinyMatic was formerly named `HomeDroid` and was created back when Android 2.1 was the lasest version of Android and before I started developing software professionally.

Even though it has been updated to work with the current version of Android, there is a lot of legacy code and techdebt.

The app is database driven and the DB is synced with the CCU periodically, the entry point from where the flow can be followed is `PeriodSyncManager`.
TinyMatic also supports push notifications via RPC, entry point `RpcForegroundService`´.

On the UI Side, the main screen is `MainActivity`.
All screens that show data are built on top of `ServiceContentActivity` and `DataFragment`.
The UI is notified about new data with broadcast notifications from the data layer which triggers a refresh of the UI from the DB.

Any CCU datapoint is parsed and translated into UI by `ListViewGenerator`, including widget content.
To  add support for a new device type, most likely the only place that needs changes is this class.

## Please Note

*TinyMatic is 'All rights reserved', meaning it is NOT licenced under an open-source licence (yet) and permission is explicitly NOT given to redistribute the code in this repository or publish binaries based on said code)

(This sounds scary but I simply want to avoid that someone steals the app. I have no issue with someone forking the code for private use and I welcome any contribution!)
