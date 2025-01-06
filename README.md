# Melanoma Scanner Mobile Application

A Kotlin-based Android application that uses machine learning to detect melanoma in skin lesions. This app integrates with a TensorFlow Lite model for real-time skin lesion classification and provides user authentication, scan history, and profile management features.

## Features

- Real-time skin lesion classification using TensorFlow Lite
- User authentication and account management
- Email verification system
- Camera integration for capturing images
- Gallery image selection
- Scan history with result tracking
- Profile management
- Password reset functionality
- Secure data storage using Firebase

## Technologies Used

- Kotlin
- Jetpack Compose for UI
- Firebase Authentication
- Firebase Firestore
- TensorFlow Lite
- Android CameraX
- Coil for image loading
- Material Design 3

## Prerequisites

- Android Studio Arctic Fox or newer
- Kotlin 1.5.0 or newer
- Android SDK 21 or higher
- Google Services configuration file (google-services.json)
- Firebase project setup
- TensorFlow Lite model file (melanoma_classifier.tflite)

## Project Setup

1. Clone the repository:
```bash
git clone [your-repository-url]
cd [repository-name]
```

2. Add your `google-services.json` file to the app module directory:
```
app/
  google-services.json
```

3. Place the TensorFlow Lite model in the assets folder:
```
app/src/main/assets/
  melanoma_classifier.tflite
```

4. Sync project with Gradle files

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/skin_melanoma_mobile_scanning_application/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MelanomaClassifier.kt
│   │   │   ├── screens/
│   │   │   │   ├── Home.kt
│   │   │   │   ├── Login.kt
│   │   │   │   ├── Registration.kt
│   │   │   │   ├── Profile.kt
│   │   │   │   ├── Scan.kt
│   │   │   │   └── ScanHistory.kt
│   │   │   └── components/
│   │   │       ├── CameraPreview.kt
│   │   │       └── ImagePreview.kt
│   │   └── assets/
│   │       └── melanoma_classifier.tflite
```

## Firebase Configuration

1. Create a new Firebase project
2. Enable Authentication with email/password
3. Set up Cloud Firestore
4. Download and add google-services.json
5. Configure security rules for Firestore

### Firestore Data Structure

```
users/
  ├── userId/
  │   ├── firstName: string
  │   ├── lastName: string
  │   ├── email: string
  │   ├── isEmailVerified: boolean
  │   └── tempAccount: boolean

scans/
  ├── scanId/
  │   ├── userId: string
  │   ├── imageUrl: string
  │   ├── diagnosis: string
  │   ├── confidence: float
  │   ├── timestamp: timestamp
  │   └── isMalignant: boolean
```

## Key Components

### Authentication Flow
- Email/Password registration
- Email verification
- Password reset functionality
- Secure login system

### Scanning Features
- Real-time camera preview
- Image capture capabilities
- Gallery image selection
- ML model integration
- Result visualization

### History Management
- Scan result storage
- Historical data viewing
- Result deletion capability
- Timestamp tracking

## Usage

### User Registration
1. Launch the app
2. Click "Register"
3. Fill in required information
4. Verify email through link
5. Login with credentials

### Scanning Process
1. Click "Start New Scan"
2. Choose camera or gallery
3. Capture/select image
4. Confirm image selection
5. View analysis results

### Viewing History
1. Navigate to "Scan History"
2. View all previous scans
3. Check detailed results
4. Delete unwanted entries

## Model Information

The app integrates a TensorFlow Lite model converted from the main melanoma classification model:
- Input Size: 224x224 pixels
- Input Format: RGB normalized (-1 to 1)
- Output: Binary classification (benign/malignant)
- Model Size: [size] MB

## Security Features

- Email verification requirement
- Secure password storage
- Firebase Authentication integration
- User-specific data isolation
- Secure image storage

## Performance Considerations

- Optimized image processing
- Background threading for ML operations
- Efficient data caching
- Minimal memory footprint

## Troubleshooting

Common issues and solutions:
1. Camera not working
   - Check permissions
   - Verify device compatibility
2. Login issues
   - Verify email confirmation
   - Check internet connection
3. Scan failures
   - Ensure proper image focus
   - Check lighting conditions
