#include "PredictionWebSocket.h"

using cocos2d::network::WebSocket;

USING_NS_CC;

PredictionWebSocket* PredictionWebSocket::create(std::string url)
{
    PredictionWebSocket* ws = new PredictionWebSocket(url);

    return ws;
}

PredictionWebSocket::PredictionWebSocket(std::string url)
{
    _url = url;
};

void PredictionWebSocket::connect()
{
    _websocket = new WebSocket();
    _websocket->init(*this, _url.c_str());
}

void PredictionWebSocket::close()
{
    if (_websocket->getReadyState() == WebSocket::State::OPEN)
    {
        _websocket->close();
    }
}

PredictionWebSocket::~PredictionWebSocket()
{
    if (_websocket) {
        _websocket->close();
        delete _websocket;
    }
}

void PredictionWebSocket::send(std::string message)
{
    if (_websocket->getReadyState() == WebSocket::State::OPEN)
    {
        _websocket->send(message);
    }
}

void PredictionWebSocket::onOpen(WebSocket* ws)
{
    CCLOG("WebSocket connection opened: %s", _url.c_str());
    if ( onConnectionOpened )
    {
        onConnectionOpened();
    }
}

void PredictionWebSocket::onMessage(WebSocket* ws, const WebSocket::Data &data)
{
    if ( onMessageReceived )
    {
        onMessageReceived(data.bytes);
    }

}

void PredictionWebSocket::onError(WebSocket* ws, const WebSocket::ErrorCode& error)
{
    if ( onErrorOccurred )
    {
        onErrorOccurred(error);
    }
}

void PredictionWebSocket::onClose(WebSocket* ws)
{
    if ( onConnectionClosed )
    {
        onConnectionClosed();
    }
}