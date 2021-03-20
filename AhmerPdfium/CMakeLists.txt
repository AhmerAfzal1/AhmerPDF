# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)
project(AhmerPdfium)

# shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(pdfium SHARED IMPORTED)
set_target_properties(pdfium PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/libs/${ANDROID_ABI}/libpdfium.so)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/libs/${ANDROID_ABI})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        jniPdfium

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${CMAKE_SOURCE_DIR}/src/main/cmake/mainJNILib.cpp)

target_include_directories(jniPdfium PRIVATE
        ${CMAKE_SOURCE_DIR}/src/main/cmake/utils
        ${CMAKE_SOURCE_DIR}/src/main/cmake/include
        ${CMAKE_SOURCE_DIR}/src/main/cmake/include/cpp)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        jniPdfium
        pdfium
        jnigraphics
        android
        GLESv2
        log)