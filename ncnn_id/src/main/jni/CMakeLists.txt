cmake_minimum_required(VERSION 3.4.1)

message(LOG ${ANDROID_NDK})

############### libncnn -> static library ###############
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/ncnn/lib/${ANDROID_ABI})
add_library(libncnn STATIC IMPORTED)
include_directories(${CMAKE_SOURCE_DIR}/ncnn/include)
set_target_properties(libncnn PROPERTIES IMPORTED_LOCATION
        ${lib_src_DIR}/libncnn.a)
############### libsupc++ -> static library ###############
set(lib_src_DIR ${ANDROID_NDK}/sources/cxx-stl/gnu-libstdc++/4.9/libs/${ANDROID_ABI})
add_library(libsupcpp STATIC IMPORTED)
include_directories(${lib_src_DIR}/include)
set_target_properties(libsupcpp PROPERTIES IMPORTED_LOCATION
        ${lib_src_DIR}/libsupc++.a)
############### opencv -> shared library ###############
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/../libs/${ANDROID_ABI})
include_directories(${CMAKE_SOURCE_DIR}/include)
add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
        ${lib_src_DIR}/libopencv_java3.so)
##############################



add_library(squeezencnn_id SHARED
            squeezencnn_jni.cpp
            cam.cpp)

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)
find_library(gnustl_static-lib gnustl_static)

target_link_libraries(squeezencnn_id
        libncnn
        libsupcpp
        libopencv_java3
        ${jnigraphics-lib}
        ${log-lib})