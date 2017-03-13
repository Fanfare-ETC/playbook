#ifndef PLAYBOOK_PREDICTION_H
#define PLAYBOOK_PREDICTION_H

#include "cocos2d.h"
#include "MappedSprite.h"

class Prediction : public cocos2d::Layer
{
public:

    static cocos2d::Scene* createScene();

    virtual bool init();
    virtual void update(float delta);
    void onEnter();
    void onExit();

    // a selector callback
    void menuCloseCallback(cocos2d::Ref* pSender);

    // implement the "static create()" method manually
    CREATE_FUNC(Prediction);

private:
    enum SceneState {
        INITIAL,
        CONTINUE
    };

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

    struct PredictionEventHash {
        template <typename T>
        std::size_t operator()(T t) const {
            return static_cast<std::size_t>(t);
        }
    };

    struct BallState {
        bool dragState;
        int dragTouchID;
        cocos2d::Vec2 dragOrigPosition;

        bool dragTargetState;
        std::string dragTarget;
    };

    cocos2d::Node* _visibleNode;

    SceneState _state;
    cocos2d::Sprite* _ballSlot;
    std::vector<BallState> _ballStates;
    cocos2d::Sprite* _continueBanner;

    MappedSprite* _fieldOverlay;
    std::unordered_map<PredictionEvent, int, PredictionEventHash> _predictionCounts;

    cocos2d::DrawNode* _notificationOverlay;
    int _score = 0;

    void initFieldOverlay();
    void initEvents();

    PredictionEvent stringToEvent(const std::string &);
    std::string eventToString(PredictionEvent event);
    PredictionEvent intToEvent(int event);

    void createNotificationOverlay(const std::string&);
    int getScoreForEvent(PredictionEvent event);
    void increasePredictionCount(PredictionEvent);
    void processPredictionEvent(PredictionEvent event);

    void restoreState();
    void saveState();
    std::string serialize();
    void unserialize(const std::string& data);
};

#endif // PLAYBOOK_PREDICTION_H
