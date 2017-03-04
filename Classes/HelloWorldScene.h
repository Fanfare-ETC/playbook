#ifndef __HELLOWORLD_SCENE_H__
#define __HELLOWORLD_SCENE_H__

#include "cocos2d.h"
#include "MappedSprite.h"

//using namespace std;

enum PredictionEvents
{
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
    threeb
};

class HelloWorld : public cocos2d::Layer
{
public:

    static cocos2d::Scene* createScene();
    int prediction[30]={0};

    virtual bool init();

    // a selector callback
    void menuCloseCallback(cocos2d::Ref* pSender);

    // implement the "static create()" method manually
    CREATE_FUNC(HelloWorld);

private:
    cocos2d::Sprite* _ballSlot;
    std::vector<bool> _ballDragState;
    std::vector<int> _ballDragTouchID;
    std::vector<cocos2d::Vec2> _ballDragOrigPosition;
    MappedSprite* _fieldOverlay;

    void initFieldOverlay();
    void initEvents();
};

#endif // __HELLOWORLD_SCENE_H__
