//
// Created by yjwong on 3/7/17.
//

#ifndef PLAYBOOK_PREDICTION_WEB_SOCKET_H
#define PLAYBOOK_PREDICTION_WEB_SOCKET_H

#include "cocos2d.h"
#include "network/WebSocket.h"

class PredictionWebSocket : public cocos2d::network::WebSocket::Delegate {
private:
private:
    std::string _url;
    cocos2d::network::WebSocket* _websocket;
public:
    std::function<void(std::string message)> onMessageReceived;
    std::function<void()> onConnectionClosed;
    std::function<void(const cocos2d::network::WebSocket::ErrorCode &error)> onErrorOccurred;
    std::function<void()> onConnectionOpened;

    PredictionWebSocket(std::string url);

    void connect();

    static PredictionWebSocket* create(std::string url);

    virtual void onOpen(cocos2d::network::WebSocket* ws);
    virtual void onMessage(cocos2d::network::WebSocket* ws, const cocos2d::network::WebSocket::Data& data);
    virtual void onError(cocos2d::network::WebSocket* ws, const cocos2d::network::WebSocket::ErrorCode& error);
    virtual void onClose(cocos2d::network::WebSocket* ws);

    void close();
    void send(std::string message);

    ~PredictionWebSocket();
};


#endif // PLAYBOOK_PREDICTION_WEB_SOCKET_H
