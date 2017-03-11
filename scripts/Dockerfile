FROM ubuntu:16.04
MAINTAINER Wong Yong Jie <yjwong92@gmail.com>

# Download system pre-requisites
RUN apt update && \
    apt install -y --no-install-recommends \
    git ca-certificates curl unzip python default-jdk build-essential file ant

# Download Cocos2d-x
RUN curl -sL -o cocos2d-x.zip http://www.cocos2d-x.org/filedown/start/364 && \
    unzip cocos2d-x.zip && \
    rm cocos2d-x.zip

# Download Android dependencies
RUN curl -sL -o android-ndk.zip https://dl.google.com/android/repository/android-ndk-r13b-linux-x86_64.zip && \
    unzip android-ndk.zip && \
    rm android-ndk.zip
RUN curl -sL -o android-sdk.zip https://dl.google.com/android/repository/tools_r25.2.3-linux.zip && \
    unzip android-sdk.zip -d /android-sdk && \
    rm android-sdk.zip
WORKDIR /android-sdk
RUN mkdir -p licenses && \
    echo "\n8933bad161af4178b1185d1a37fbf41ea5269c55" > licenses/android-sdk-license && \
    ./tools/bin/sdkmanager --update && \
    ./tools/bin/sdkmanager "platforms;android-25" && \
    ./tools/bin/sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout-solver;1.0.2" && \
    ./tools/bin/sdkmanager "extras;m2repository;com;android;support;constraint;constraint-layout;1.0.2"

# Setup Cocos2d-x
ENV COCOS_CONSOLE_ROOT=/cocos2d-x-3.14.1/tools/cocos2d-console/bin \
    COCOS_X_ROOT=/ \
    COCOS_TEMPLATES_ROOT=/cocos2d-x-3.14.1/templates \
    NDK_ROOT=/android-ndk-r13b \
    ANDROID_SDK_ROOT=/android-sdk \
    ANT_ROOT=/usr/share/ant/bin \
    PATH="/cocos2d-x-3.14.1/tools/cocos2d-console/bin:$PATH"

WORKDIR /cocos2d-x-3.14.1
RUN mkdir -p /root/.cocos && \
    echo "{ \"agreement_shown\": true }" > /root/.cocos/local_cfg.json

