# TinyMatic

TinyMatic was formerly named `HomeDroid` and was created back when Android 2.1 was the latest version of Android and before I started developing software professionally.

Even though the app has been updated to work with target SDK 30, most of the code is pretty old with a lot of technical debt.

The app is database driven and the DB is synced with the CCU periodically. The entry point from which the flow can be followed is `PeriodSyncManager`.
TinyMatic also supports push notifications by running a local XML-RPC server, entry point `RpcForegroundService`.

On the UI Side, the main screen is `MainActivity`.
All screens that show data are built on top of `ServiceContentActivity` and `DataFragment`.
The UI is notified about new data using broadcast notifications, which trigger a refresh of the UI based on the DB state.

Any CCU data is parsed and translated into UI by `ListViewGenerator`, including widget content.
To add support for a new device type, most likely the only place that needs changes is this class.

## Please Note

TinyMatic is 'All rights reserved', meaning that it is not licenced under an open-source licence and permission is explicitly not given to publish binaries based on any code in this repository to any app store. You are free to copy, fork and modify the source for private use and I welcome contributions and PRs!

(Bottom line, I don't want some weird version of TinyMatic showing up on the Play Store)
