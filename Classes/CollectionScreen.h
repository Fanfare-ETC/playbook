//
// Created by ramya on 3/2/17.
//

#ifndef PLAYBOOK_COLLECTION_SCREEN_H
#define PLAYBOOK_COLLECTION_SCREEN_H

#include "cocos2d.h"
#include "json/rapidjson.h"
#include "json/document.h"

#include "PlaybookLayer.h"
#include "PlaybookEvent.h"
#include "PredictionWebSocket.h"

class CollectionScreen : public PlaybookLayer
{
public:

    static cocos2d::Scene* createScene();

    virtual bool init();

    void onResume();
    void onPause();

    // implement the "static create()" method manually
    CREATE_FUNC(CollectionScreen);

private:
    cocos2d::Node* _visibleNode;
    PredictionWebSocket* _websocket;

    void connectToServer();
    void disconnectFromServer();
    void handleServerMessage(const std::string& event,
                             const rapidjson::Value::ConstMemberIterator& data, bool hasData);
    void handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& data, bool hasData);

    void receiveCard(PlaybookEvent::EventType event);
};

#endif //PLAYBOOK_COLLECTION_SCREEN_H
