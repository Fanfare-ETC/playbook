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

    void onEnter();
    void onExit();
    void onResume();
    void onPause();

    // implement the "static create()" method manually
    CREATE_FUNC(CollectionScreen);

private:
    static const int NUM_SLOTS;

    struct Card {
        PlaybookEvent::Team team;
        PlaybookEvent::EventType event;
        cocos2d::Sprite* sprite;

        bool operator==(const Card& card) const {
            return this->sprite == card.sprite;
        }

        bool operator!=(const Card& card) const {
            return !this->operator==(card);
        }
    };

    struct CardSlot {
        Card card;
        std::vector<Card> cardSet;
        bool present;
    };

    enum GoalType {
        IDENTICAL_CARDS_3,
        IDENTICAL_CARDS_4,
        IDENTICAL_CARDS_5,
        UNIQUE_OUT_CARDS_3,
        UNIQUE_OUT_CARDS_4,
        WALK_OR_HIT_3,
        OUT_3,
        BASES_3,
        EACH_COLOR_1,
        EACH_COLOR_2,
        SAME_COLOR_3,
        SAME_COLOR_4,
        SAME_COLOR_5,
        BASE_STEAL_RBI
    };

    cocos2d::Node* _visibleNode;
    PredictionWebSocket* _websocket;

    cocos2d::Sprite* _cardsHolder;
    cocos2d::DrawNode* _cardSlotDrawNode;
    std::queue<PlaybookEvent::EventType> _incomingCardQueue;
    std::vector<CardSlot> _cardSlots;
    bool _isCardActive;
    bool _isCardDragged;
    Card _draggedCard;

    cocos2d::Sprite* _giveToSection;
    float _giveToSectionOrigScale;
    bool _giveToSectionHovered;
    cocos2d::EventListener* _giveToSectionListener;

    Card _activeCard;
    float _activeCardOrigScale;
    cocos2d::Vec2 _activeCardOrigPosition;
    float _activeCardOrigRotation;
    cocos2d::EventListener* _activeEventListener;

    void connectToServer();
    void disconnectFromServer();
    void handleServerMessage(const std::string& event,
                             const rapidjson::Value::ConstMemberIterator& data, bool hasData);
    void handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& data, bool hasData);

    void receiveCard(PlaybookEvent::EventType event);
    void startDraggingActiveCard(cocos2d::Touch* touch);
    void stopDraggingActiveCard(cocos2d::Touch* touch);
    void discardCard(const Card& card);

    float getCardScaleInSlot(cocos2d::Node* card);
    cocos2d::Vec2 getCardPositionForSlot(cocos2d::Node* cardNode, int slot);
    cocos2d::Rect getCardBoundingBoxForSlot(cocos2d::Node* cardNode, int slot);
    void drawBoundingBoxForSlot(cocos2d::Node* cardNode, int slot);
    int getNearestAvailableCardSlot(cocos2d::Node *card, const cocos2d::Vec2 &position);
    void assignActiveCardToSlot(int slot);

    bool cardSetMeetsGoal(std::vector<Card> cardSet, GoalType goal);
};

#endif //PLAYBOOK_COLLECTION_SCREEN_H
