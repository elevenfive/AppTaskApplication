# AppTaskApplication

Using parts of the AppTask API on API 32 appears to be broken due to issues with the windowing system involving
null pointer exceptions (NPE's) in `IAppTask` and `ActivityRecord`.

The instrumented test in this project NPE's out in `IAppTask` and after running the test, uninstalling the 
application causes a system reboot due to NPE in `ActivityRecord`.

Similar behavior occurs outside the test environment.  Launching "SecondaryActivity" displays the 
same NPE behavior as the instrumented test, and also causes a system reboot upon app uninstall.

This project was based off Android Studio's "New Project" wizard, and stripping out as much as possible
for clarity.