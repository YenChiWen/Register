/*

Copyright 2016 Narrative Nithts Inc. All Rights Reserved.
Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#ifndef ORG_NCNN_JNI_NCNN_JNI_H_  // NOLINT
#define ORG_NCNN_JNI_NCNN_JNI_H_  // NOLINT

#include <jni.h>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/legacy/compat.hpp>
#include <opencv2/core/core.hpp>
#include <string>
#include <map>
#include <android/bitmap.h>
#include <android/log.h>

using namespace std;

#define LOG_TAG "SqueezeNcnn"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define NCNN_METHOD(METHOD_NAME) \
  Java_com_viatech_idlib_DigitDetector_##METHOD_NAME  // NOLINT

#define NCNN_TENSORFLOWMETHOD(METHOD_NAME) \
  Java_com_viatech_viafacedetectlib_TensorFlowObjectDetectionAPIModel_##METHOD_NAME  // NOLINT

#endif  // ORG_NCNN_JNI_NCNN_JNI_H_  // NOLINT
