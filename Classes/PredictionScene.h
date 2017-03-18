#ifndef PLAYBOOK_PREDICTION_H
#define PLAYBOOK_PREDICTION_H

#include "cocos2d.h"
#include "PlaybookLayer.h"
#include "MappedSprite.h"
#include "PredictionWebSocket.h"

class Prediction : public PlaybookLayer
{
public:
    enum PredictionEvent {
        error,
        grandslam,
        shutout_inning,
        longout,
        runs_batted,
        pop_fly,
        triple_play,
        double_play,
        grounder,
        steal,
        pick_off,
        walk,
        blocked_run,
        strike_out,
        hit,
        homerun,
        pitchcount_16,
        walk_off,
        pitchcount_17,
        oneb,
        twob,
        threeb,
        unknown
    };

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

    struct PredictionEventHash {
        template <typename T>
        std::size_t operator()(T t) const {
            return static_cast<std::size_t>(t);
        }
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
    std::unordered_map<PredictionEvent, int, PredictionEventHash> _predictionCounts;
    cocos2d::DrawNode* _notificationOverlay;

    PredictionWebSocket* _websocket;

    int _score = 0;

    void initFieldOverlay();
    void initEvents();

    void connectToServer();
    void disconnectFromServer();

    static PredictionEvent stringToEvent(const std::string &);
    static std::string eventToString(PredictionEvent event);
    static PredictionEvent intToEvent(int event);

    void createNotificationOverlay(const std::string&);
    int getScoreForEvent(PredictionEvent event);
    void moveBallToField(PredictionEvent event, Ball& ball, bool withAnimation = true);
    void moveBallToSlot(Ball& ball);
    cocos2d::Vec2 getBallPositionForSlot(cocos2d::Sprite* ballSprite, int slot);
    void makePrediction(PredictionEvent event, Ball&);
    void undoPrediction(PredictionEvent event, Ball&);
    void processPredictionEvent(PredictionEvent event);

    void restoreState();
    void saveState();
    std::string serialize();
    void unserialize(const std::string& data);
};

#endif // PLAYBOOK_PREDICTION_H
