FROM ubuntu:16.04
MAINTAINER Wong Yong Jie <yjwong92@gmail.com>

# Download system pre-requisites
RUN apt update && \
    apt install -y --no-install-recommends \
    git ca-certificates curl unzip default-jdk build-essential

# Download Android dependencies
RUN curl -sL -o android-ndk.zip https://dl.google.com/android/repository/android-ndk-r14-linux-x86_64.zip && \
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

