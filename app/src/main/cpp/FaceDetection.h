//
// Created by yen on 8/29/19.
//

#ifndef FACEDETETION_FACEDETECTION_H
#define FACEDETETION_FACEDETECTION_H

#include <string>
#include <opencv2/opencv.hpp>
#include <jni.h>


std::vector<cv::Rect> faceDetection(cv::Mat &matSrc, cv::Mat &matDest, cv::CascadeClassifier cascade);


#endif //FACEDETETION_FACEDETECTION_H

