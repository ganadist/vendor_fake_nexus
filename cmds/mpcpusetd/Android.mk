
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
	CpusetManager.cpp \
	NetlinkHandler.cpp \
	NetlinkManager.cpp \
	main.cpp \

LOCAL_SHARED_LIBRARIES := \
	liblog \
	libbase \
	libcutils \
	libsysutils \
	libutils \

LOCAL_MODULE_TAGS = optional
LOCAL_INIT_RC := mpcpusetd.rc
LOCAL_MODULE := mpcpusetd
include $(BUILD_EXECUTABLE)
