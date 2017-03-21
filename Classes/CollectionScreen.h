//
// Created by ramya on 3/2/17.
//

#ifndef PROJ_ANDROID_STUDIO_COLLECTIONSCREEN_H
#define PROJ_ANDROID_STUDIO_COLLECTIONSCREEN_H

#include "cocos2d.h"
class CollectionScreen : public cocos2d::Layer
{
public:

    static cocos2d::Scene* createScene();

    virtual bool init();

    // a selector callback
    void menuCloseCallback(cocos2d::Ref* pSender);

    // receive a card
    void receiveCard();


    // implement the "static create()" method manually
    CREATE_FUNC(CollectionScreen);

};

#endif //PROJ_ANDROID_STUDIO_COLLECTIONSCREEN_H
