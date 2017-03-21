#ifndef PLAYBOOK_PLAYBOOK_LAYER_H
#define PLAYBOOK_PLAYBOOK_LAYER_H

#include "cocos2d.h"

class PlaybookLayer : public cocos2d::Layer {
public:
    virtual void onEnter() {
        cocos2d::Layer::onEnter();
        this->onResume();
    };

    virtual void onExit() {
        this->onPause();
        cocos2d::Layer::onExit();
    };

    virtual void onResume() {};
    virtual void onPause() {};
};

#endif //PLAYBOOK_PLAYBOOK_LAYER_H
