LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PROGUARD_ENABLED := disabled 
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := BluetoothHid
include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
