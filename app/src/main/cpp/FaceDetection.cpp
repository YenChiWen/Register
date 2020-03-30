//
// Created by yen on 8/29/19.
//


#include "FaceDetection.h"

cv::Mat drawRectangle(cv::Mat matSrc, std::vector<cv::Rect> faces){

    cv::Mat matDest = matSrc.clone();
    cv::Scalar color = cv::Scalar(0,0,255);

    for(int i=0; i<faces.size(); i++){
        cv::Rect rect = faces[i];
        cv::rectangle(matDest, rect, color, 3);
    }

    return matDest;
}

std::vector<cv::Rect> detect(   cv::Mat &matSrc,
                                cv::Mat &matDest,
                                cv::CascadeClassifier &cascadeClassifier){
    std::vector<cv::Rect> faces;

    cv::Mat matGray = matSrc.clone();
    cv::cvtColor(matSrc, matGray, cv::COLOR_BGR2GRAY);
    cascadeClassifier.detectMultiScale(matGray, faces, 1.1, 1);
//    matDest = drawRectangle(matSrc, faces);

    return faces;
}

std::vector<cv::Rect> faceDetection(cv::Mat &matSrc, cv::Mat &matDest, cv::CascadeClassifier cascade){

    std::vector<cv::Rect> faces;
    if(!matSrc.empty() && !cascade.empty()){
        faces = detect(matSrc, matDest, cascade);
    }

    return faces;
}

