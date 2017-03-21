#ifndef PLAYBOOK_PREDICTION_H
#define PLAYBOOK_PREDICTION_H

#include "cocos2d.h"
#include "PlaybookLayer.h"
#include "MappedSprite.h"
#include "PredictionWebSocket.h"
#include "PlaybookEvent.h"

class Prediction : public PlaybookLayer
{
public:
    static cocos2d::Scene* createScene();

    virtual bool init();
    virtual void update(float delta);

    void onResume();
    void onPause();

    // implement the "static create()" method manually
    CREATE_FUNC(Prediction);

private:
    enum SceneState {
        INITIAL,
        CONTINUE,
        CONFIRMED
    };

    struct Ball {
        bool dragState;
        int dragTouchID;
        cocos2d::Vec2 dragOrigPosition;

        bool dragTargetState;
        std::string dragTarget;

        bool selectedTargetState;
        std::string selectedTarget;

        cocos2d::Sprite* sprite;
    };

    cocos2d::Node* _visibleNode;

    SceneState _state;
    cocos2d::Sprite* _ballSlot;
    std::vector<Ball> _balls;
    cocos2d::Sprite* _continueBanner;

    MappedSprite* _fieldOverlay;
    std::unordered_map<PlaybookEvent::EventType, int, PlaybookEvent::EventTypeHash> _predictionCounts;
    cocos2d::DrawNode* _notificationOverlay;

    PredictionWebSocket* _websocket;

    int _score = 0;

    void initFieldOverlay();
    void initEvents();

    void connectToServer();
    void disconnectFromServer();

    void createNotificationOverlay(const std::string&);
    int getScoreForEvent(PlaybookEvent::EventType event);
    void moveBallToField(PlaybookEvent::EventType event, Ball& ball, bool withAnimation = true);
    void moveBallToSlot(Ball& ball);
    cocos2d::Vec2 getBallPositionForSlot(cocos2d::Sprite* ballSprite, int slot);
    void makePrediction(PlaybookEvent::EventType event, Ball&);
    void undoPrediction(PlaybookEvent::EventType event, Ball&);
    void processPredictionEvent(PlaybookEvent::EventType event);

    void restoreState();
    void saveState();
    std::string serialize();
    void unserialize(const std::string& data);
};

#endif // PLAYBOOK_PREDICTION_H
