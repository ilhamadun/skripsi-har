LOCAL_PATH := $(call my-dir)

TARGET_ARCH_ABI := armeabi-v7a

include $(CLEAR_VARS)
LOCAL_MODULE := tensorflow-inference
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libtensorflow_inference.so
include $(PREBUILT_SHARED_LIBRARY)