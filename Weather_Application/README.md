# Weather Application

A simple weather application for Android devices.

## Technologies Used
- Android Studio
- Kotlin
- Open-Meteo API

## Application Overview
The application initially provides weather data for five default cities.

Features include:
- Adding custom locations by entering coordinates and a custom name
- Saving custom locations locally
- Removing previously added locations
- Selecting a default location shown when the app is opened

> Adding locations using coordinates is not ideal from a usability perspective, but this was a project requirement.

Weather data is retrieved from the Open-Meteo API.  
The app displays temperature and rain amount:
- hourly for the current day
- at 15:00 for the following seven days

## Source Code
The Android application source code is located under:

**Weather_Application/app/src/main/java/com/example/weather_application/**

