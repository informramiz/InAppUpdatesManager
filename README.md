# InAppUpdatesManager
A simple wrapper around Google Play AppUpdateFactory to make in-app updates seamless. Library uses `LiveData` so you don't have to worry about your activity lifecycle.

## Download
```

implementation "io.github.informramiz:inappupdatesmanager:1.1"


```

## Getting Started
In the activity you want to trigger in-app update (BaseActivity in case you want to check in all activities)

```
private var inAppUpdatesManager: InAppUpdatesManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ...
        
        inAppUpdatesManager = InAppUpdatesManager(this)
        ...
    }

	...

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (inAppUpdatesManager?.isInAppUpdateActivityResult(requestCode) == true) {
            inAppUpdatesManager?.onActivityResult(requestCode, resultCode)
        }
    }
```
That's it.