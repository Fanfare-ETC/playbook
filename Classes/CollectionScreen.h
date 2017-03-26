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
    virtual void update(float delta);

    void onResume();
    void onPause();

    // implement the "static create()" method manually
    CREATE_FUNC(CollectionScreen);

private:
    static const int NUM_SLOTS;

    cocos2d::Node* _visibleNode;
    PredictionWebSocket* _websocket;

    cocos2d::Sprite* _cardsHolder;
    cocos2d::DrawNode* _cardSlotDrawNode;
    std::queue<PlaybookEvent::EventType> _incomingCardQueue;
    cocos2d::Sprite* _activeCard;
    float _activeCardOrigScale;
    cocos2d::Vec2 _activeCardOrigPosition;
    float _activeCardOrigRotation;

    bool _isCardActive;

    void connectToServer();
    void disconnectFromServer();
    void handleServerMessage(const std::string& event,
                             const rapidjson::Value::ConstMemberIterator& data, bool hasData);
    void handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& data, bool hasData);

    void receiveCard(PlaybookEvent::EventType event);
    void startDraggingActiveCard(cocos2d::Touch* touch);
    void stopDraggingActiveCard(cocos2d::Touch* touch);

    float getCardScaleInSlot(cocos2d::Node* card);
    cocos2d::Vec2 getCardPositionForSlot(cocos2d::Node* card, int slot);
    cocos2d::Rect getCardBoundingBoxForSlot(cocos2d::Node* card, int slot);
    int getNearestCardSlot(cocos2d::Node* card, const cocos2d::Vec2& position);
};

#endif //PLAYBOOK_COLLECTION_SCREEN_H
