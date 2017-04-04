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
    struct Card {
        Card(PlaybookEvent::Team team, PlaybookEvent::EventType event, cocos2d::Sprite* sprite);
        PlaybookEvent::Team team;
        PlaybookEvent::EventType event;
        cocos2d::Sprite* sprite;
    };

    struct CardSlot {
        std::shared_ptr<Card> card;
        bool present;
    };

    enum GoalType {
        IDENTICAL_CARDS_3,
        IDENTICAL_CARDS_4,
        IDENTICAL_CARDS_5,
        //UNIQUE_OUT_CARDS_3,
        //UNIQUE_OUT_CARDS_4,
        WALK_OR_HIT_3,
        OUT_3,
        BASES_3,
        EACH_COLOR_1,
        EACH_COLOR_2,
        SAME_COLOR_3,
        SAME_COLOR_4,
        SAME_COLOR_5,
        BASE_STEAL_RBI,
        UNKNOWN
    };

    struct GoalTypeHash {
        template <typename T>
        std::size_t operator()(T t) const {
            return static_cast<std::size_t>(t);
        }
    };

    static const int NUM_SLOTS;
    static const std::unordered_map<GoalType, std::string, GoalTypeHash> GOAL_TYPE_FILE_MAP;
    static const std::unordered_map<GoalType, int, GoalTypeHash> GOAL_TYPE_SCORE_MAP;

    cocos2d::Node* _visibleNode;
    PredictionWebSocket* _websocket;

    cocos2d::Sprite* _cardsHolder;
    cocos2d::DrawNode* _cardSlotDrawNode;
    std::queue<PlaybookEvent::EventType> _incomingCardQueue;
    std::vector<CardSlot> _cardSlots;
    bool _isCardActive;
    bool _isCardDragged;
    std::weak_ptr<Card> _draggedCard;
    bool _draggedCardDropping;
    cocos2d::Vec2 _draggedCardOrigPosition;

    cocos2d::Sprite* _giveToSection;
    float _giveToSectionOrigScale;
    bool _giveToSectionHovered;
    cocos2d::EventListener* _giveToSectionListener;

    cocos2d::Sprite* _dragToScore;
    float _dragToScoreOrigScale;
    bool _dragToScoreHovered;
    cocos2d::EventListener* _dragToScoreListener;

    cocos2d::Sprite* _goalSprite;
    GoalType _activeGoal;
    std::vector<std::weak_ptr<Card>> _cardsMatchingGoal;

    cocos2d::Label* _scoreLabel;

    std::shared_ptr<Card> _activeCard;
    float _activeCardOrigScale;
    cocos2d::Vec2 _activeCardOrigPosition;
    float _activeCardOrigRotation;
    cocos2d::EventListener* _activeEventListener;

    void initEventsGiveToSection();
    void initEventsDragToScore();

    void connectToServer();
    void disconnectFromServer();
    void handleServerMessage(const std::string& event,
                             const rapidjson::Value::ConstMemberIterator& data, bool hasData);
    void handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& data, bool hasData);
    void reportScore(int score);

    void receiveCard(PlaybookEvent::EventType event);
    std::shared_ptr<Card> createCard(PlaybookEvent::EventType event);
    void startDraggingActiveCard(cocos2d::Touch* touch);
    void stopDraggingActiveCard(cocos2d::Touch* touch);
    void discardCard(std::weak_ptr<Card> card);
    void scoreCardSet(GoalType goal, const std::vector<std::weak_ptr<Card>>& cardSet);
    void displayScore(int score);

    float getCardScaleInSlot(cocos2d::Node* card);
    cocos2d::Vec2 getCardPositionForSlot(cocos2d::Node* cardNode, int slot);
    cocos2d::Rect getCardBoundingBoxForSlot(cocos2d::Node* cardNode, int slot);
    void drawBoundingBoxForSlot(cocos2d::Node* cardNode, int slot);
    int getNearestAvailableCardSlot(cocos2d::Node *card, const cocos2d::Vec2 &position);
    void assignActiveCardToSlot(int slot);

    void createGoal();
    void checkIfGoalMet();
    bool cardSetMeetsGoal(const std::vector<std::weak_ptr<Card>>& cardSet,
                          GoalType goal,
                          std::vector<std::weak_ptr<Card>>& outSet);

    void restoreState();
    void saveState();
    std::string serialize();
    void unserialize(const std::string& data);
};

#endif //PLAYBOOK_COLLECTION_SCREEN_H
