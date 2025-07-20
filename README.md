# GThenewapp - Book Management App

This Android app allows users to manage their book collection with Firebase integration and local SQLite storage.

## Features

- **User Authentication**: Firebase Authentication with local fallback
- **Book Management**: Add, view, and manage books
- **Firebase Integration**: Cloud storage for books with offline support
- **Local Storage**: SQLite database for offline access
- **Sample Data**: Pre-populated with sample books

## Firebase Setup

### 1. Firebase Project Configuration
- The app is configured to use Firebase project: `gthenewapp`
- Firebase configuration file: `app/google-services.json`

### 2. Database Rules
The app includes database rules in `database.rules.json`:
```json
{
  "rules": {
    "users": {
      "$uid": {
        "books": {
          ".read": "$uid === auth.uid",
          ".write": "$uid === auth.uid"
        }
      }
    }
  }
}
```

### 3. Deploy Database Rules
To deploy the database rules to Firebase:
1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login to Firebase: `firebase login`
3. Initialize project: `firebase init database`
4. Deploy rules: `firebase deploy --only database`

## Troubleshooting Firebase Issues

### Issue: No books appearing in the app

**Possible Causes:**
1. **Firebase Authentication**: User not properly authenticated
2. **Database Rules**: Incorrect permissions
3. **Network Issues**: No internet connection
4. **Empty Database**: Firebase database is empty

**Solutions:**

#### 1. Check Authentication Status
- The app shows toast messages indicating authentication status
- If not authenticated, the app falls back to local SQLite storage

#### 2. Add Sample Books to Firebase
- Go to the Home screen
- Click "Add Sample Books to Firebase" button
- This will populate Firebase with 4 sample books

#### 3. Sync Local Books to Firebase
- Go to the Home screen
- Click "Sync Local Books to Firebase" button
- This will copy all local books to Firebase

#### 4. Refresh Books List
- Go to the Books section
- Click the "Refresh" button to reload books

#### 5. Check Firebase Console
- Visit [Firebase Console](https://console.firebase.google.com)
- Select project: `gthenewapp`
- Check Realtime Database for data
- Verify Authentication users

### Sample Books Included

The app includes these sample books:
1. **Atomic Habits 1** by James Clear (ISBN: 0735211299, Publisher: Avery)
2. **Android Programming** by Bryan Sills and Brian Gardner (ISBN: 0137645546, Publisher: Addison-Wesley)
3. **Software Architecture in Practice** by Less Bass and Paul Clements (ISBN: 0136886094, Publisher: Addison-Wesley)
4. **Rich Dad, Poor Dad** by Robert Kiyosaki (ISBN: 1612681131, Publisher: Plata Publishing)

## App Structure

### Key Files
- `MainActivity.java`: Main activity with navigation drawer
- `BooksListFragment.java`: Displays list of books
- `AddBookFragment.java`: Add new books
- `FirebaseDatabaseManager.java`: Firebase database operations
- `BookDatabaseHelper.java`: SQLite database operations
- `AuthHelper.java`: Authentication management

### Data Flow
1. App tries to load books from Firebase first
2. If Firebase fails or is empty, falls back to SQLite
3. If SQLite is empty, populates with sample data
4. All operations sync between Firebase and SQLite

## Building and Running

1. Open project in Android Studio
2. Sync Gradle files
3. Build and run on device/emulator
4. Sign in with email/password
5. Navigate to Books section to view books

## Logs and Debugging

The app includes extensive logging. Check Logcat with these tags:
- `BooksListFragment`: Book loading operations
- `FirebaseDatabaseManager`: Firebase operations
- `AuthHelper`: Authentication operations
- `HomeFragment`: Home screen operations

## Common Issues and Solutions

### "Not connected to Firebase"
- Check internet connection
- Verify Firebase project configuration
- Check if user is authenticated

### "Firebase connection failed"
- Check Firebase database rules
- Verify Firebase project is active
- Check network connectivity

### "User not authenticated"
- Sign in with valid credentials
- Check Firebase Authentication settings
- Verify email/password are correct

## Support

If you continue to experience issues:
1. Check the logs in Android Studio Logcat
2. Verify Firebase project configuration
3. Ensure database rules are properly deployed
4. Test with a fresh user account 