# Icon Scaling Solution for Android Release Mode

## Problem Solved
This solution fixes the issue where chart icons appear very small in Android release mode while working correctly in debug mode and iOS.

## What Was Changed

### 1. Enhanced DrawableUtils.java
- **Added Context Support**: The library now accepts and stores the React Native application context
- **Smart Asset Loading**: Tries to load assets directly from the app's assets folder before falling back to data URLs
- **Proper Density Scaling**: Uses device density to scale icons correctly in release mode
- **Multiple Asset Path Support**: Tries common asset paths (`icons/`, `assets/`, `images/`, root)

### 2. Context Initialization
- **MPAndroidChartPackage**: Now initializes DrawableUtils with the React context during package creation

## How It Works

### Debug Mode
- Uses HTTP localhost URLs as before (no changes needed)

### Release Mode
1. **Asset Name Detection**: When the library receives an asset name (not a full URL), it attempts to load directly from the app's assets
2. **Multiple Path Attempts**: Tries loading from:
   - `icons/{assetName}`
   - `{assetName}`
   - `assets/{assetName}`
   - `images/{assetName}`
3. **Proper Scaling**: Uses `DisplayMetrics.density` to scale icons correctly for the device
4. **Fallback Support**: If asset loading fails, falls back to the original data URL approach

### Data URL Mode (Your Current Workaround)
- Now applies proper density-based scaling to prevent tiny icons in release mode
- Uses device density to calculate correct dimensions

## Usage

### Option 1: Direct Asset Loading (Recommended)
Place your icon assets in your React Native project's `android/app/src/main/assets/icons/` folder, then reference them by name:

```javascript
const chartData = {
  dataSets: [{
    values: [
      {
        x: 0,
        y: 10,
        icon: {
          bundle: { uri: "my_icon.png" }, // Just the filename
          width: 24,
          height: 24
        }
      }
    ]
  }]
};
```

### Option 2: Continue Using Your Current Approach
Your existing `resolveChartAssetSource` function will still work, but now icons will be properly scaled:

```javascript
const resolved = await resolveChartAssetSource(myIcon, 'icons/my_icon.png');
// The library will now apply proper scaling to the data URL
```

## Benefits

1. **No More Tiny Icons**: Proper scaling in Android release mode
2. **Better Performance**: Direct asset loading is faster than base64 conversion
3. **Consistent Behavior**: Icons now behave the same across debug/release and iOS/Android
4. **Backward Compatible**: Your existing code continues to work
5. **Flexible Asset Paths**: Supports multiple common asset folder structures

## Testing

To test this solution:

1. **Debug Mode**: Should work exactly as before
2. **Release Mode**: Icons should now appear at the correct size
3. **Different Densities**: Test on devices with different screen densities

The solution maintains all existing functionality while fixing the scaling issues you encountered.