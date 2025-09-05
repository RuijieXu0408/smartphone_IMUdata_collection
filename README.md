# IMU Data Collector App

An IMU (Inertial Measurement Unit) data collection application based on the Android platform, used to collect mobile phone sensor data and save it in CSV format files.

## 📱 Features

- **Real-time IMU data collection**: Collect accelerometer and gyroscope data at the same time
- **High-frequency sampling**: Use `SENSOR_DELAY_FASTEST` mode to ensure the highest sampling rate
- **Data visualization**: Display sensor data and collection status in real time
- **CSV file export**: Automatically save data in standard CSV format for subsequent analysis
- **Application-specific storage**: No additional permissions are required, data is stored securely
- **Sensor information display**: Display detailed information of sensors supported by the device

## 🚀 Quick Start

### System requirements

- Android 5.0 (API level 21) or higher
- Android devices that support accelerometer and gyroscope

### Installation method (Build from source)

```bash

git clone https://github.com/RujieXu0408/smartphone_IMUdata_collection.git
cd smartphone_IMUdata_collection
Use Android Studio to open the project and build the installation
```

## 📊 Instructions for use

1. **Start the application**: Open the IMU Data Collector application
2. **View sensor information**: The application will automatically detect and display device sensor information
3. **Start collection**: Click the "Start Collection" button to start recording data
4. **Real-time monitoring**: The interface will display the collected data and statistical information in real time
5. **Stop collection**: Click the "Stop Collection" button to end data collection
6. **View data files**: After the collection is completed, the data will be automatically saved as a CSV file

**Data file location**
The data file is saved in the application-specific directory:

```bash
/Android/data/com.example.imuapp/files/IMUData/
```

**CSV file format**

```csv
timestamp_ms,acc_x,acc_y,acc_z,gyro_x,gyro_y,gyro_z
0.000,0.123456,-0.234567,9.876543,0.001234,-0.002345,0.003456
...
```

## 🏗️ Project structure
```
app/
├── src/main/
│ ├── java/com/example/imuapp/
│ │ └── MainActivity.kt # Main business logic
│ ├── res/
│ │ ├── layout/
│ │ │ └── activity_main.xml # Main interface layout
│ │ └── values/
│ │ └── strings.xml # String resources
│ └── AndroidManifest.xml # Application configuration file
├── build.gradle.kts # Application-level build configuration
└── proguard-rules.pro # Code obfuscation rules
```

## 🔧 Technology stack

- **Development language**: Kotlin
- **Minimum SDK version**: API 21 (Android 5.0)
- **Target SDK version**: API 34
- **Main components**:
- `SensorManager`: sensor management
- `SensorEventListener`: sensor event listening
- `FileWriter`: file writing operation

## 🎯 Application scenarios

- **Academic research**: kinematic research, behavior analysis
- **Algorithm development**: multi-sensor fusion algorithm testing
- **Device calibration**: IMU sensor calibration
- **Data analysis**: mobile device motion pattern analysis
- **Machine learning**: training data collection

## 🤝 Contribution guide

Welcome to contribute code! Please follow the steps below:

1. Fork this repository

2. Create a feature branch (`git checkout -b feature/AmazingFeature`)

3. Commit changes (`git commit -m 'Add some AmazingFeature'`)

4. Push to branch (`git push origin feature/AmazingFeature`)

5. Open a Pull Request

## 📝 Development plan

- [ ] Add GNSS data collection function
- [ ] Integrate camera data collection
- [ ] Implement cloud data synchronization
- [ ] Add data visualization charts
- [ ] Support multiple file formats for export
- [ ] Add data filtering options

## 📞 Contact Information

If you have any questions or suggestions, please contact us via the following methods:

- Submit [Issue](https://github.com/RuijieXu0408/smartphone_IMUdata_collection/issues)
- Send e-mail to：ruijie.xu@connect.polyu.hk


⭐ If this project is helpful to you, please give a Star to support it!

