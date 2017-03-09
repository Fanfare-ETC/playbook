#ifndef PLAYBOOK_ROOT_SCENE_H
#define PLAYBOOK_ROOT_SCENE_H

#include "cocos2d.h"

class RootScene : public cocos2d::Layer {
public:
    static cocos2d::Scene* createScene();

    virtual bool init();

    // implement the "static create()" method manually
    CREATE_FUNC(RootScene);

};


#endif //PLAYBOOK_ROOT_SCENE_H
