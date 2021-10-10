#ifndef _UTIL_HPP_
#define _UTIL_HPP_

#include <android/log.h>
#include <jni.h>

extern "C" {
#include <stdlib.h>
}

#define JNI_ARGS    JNIEnv *env, jobject thiz
#define JNI_FUNC(retType, bindClass, name)  JNIEXPORT retType JNICALL Java_com_ahmer_afzal_pdfium_##bindClass##_##name

#define LOG_TAG "AhmerPdfium"
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif