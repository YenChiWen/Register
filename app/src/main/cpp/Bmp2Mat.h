//
// Created by yen on 8/28/19.
//

#ifndef FACEDETETION_BMP2MAT_H
#define FACEDETETION_BMP2MAT_H

#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>

bool Bmp2Mat(JNIEnv *env, jobject obj_bitmap, cv::Mat &matrix);
bool Mat2Bmp(JNIEnv *env, jobject &obj_bitmap, cv::Mat &matrix);

#endif //FACEDETETION_BMP2MAT_H
