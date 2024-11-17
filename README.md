# React Native Voice to Text

A React Native module that provides voice-to-text capabilities using Android's Speech Recognizer.

## Installation

To install the package, run:

```bash
npm install react-native-voice-to-text-custom
```

## Setup (Android Only)

Make sure you have set up permissions for Android. Add the following permission to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Usage

```javascript
import VoiceRecognition from 'react-native-voice-to-text';

// Request microphone permission first
VoiceRecognition.requestMicrophonePermission().then(() => {
  
// Start voice recognition
  VoiceRecognition.startRecognition('en-US')
    .then(() => console.log('Recognition started'))
    .catch((error) => console.error('Error:', error));
});

// Stop recognition when needed
VoiceRecognition.stopRecognition().then(() => {
  console.log('Recognition stopped');
});
```

## API

### `requestMicrophonePermission()`
Requests microphone permission from the user.

**Returns**: `Promise<string>`

### `startRecognition(locale: string)`
Starts voice recognition with the given locale.

**Arguments**:
- `locale` (string): The language code (e.g., 'en-US') to be used for voice recognition.

**Returns**: `Promise<void>`

### `stopRecognition()`
Stops the voice recognition process.

**Returns**: `Promise<void>`

## Contributing

Contributions are welcome! Please feel free to open issues or submit pull requests.

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for more information.
# react-native-voice-to-text
