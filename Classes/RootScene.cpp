#include "CollectionScreen.h"
#include "RootScene.h"

using namespace cocos2d;

Scene* RootScene::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    scene->setName("Root");

    // 'layer' is an autorelease object
    auto layer = RootScene::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

bool RootScene::init() {
    if (!Layer::init()) {
        return false;
    }

    auto listener = EventListenerKeyboard::create();

    listener->onKeyPressed = [](EventKeyboard::KeyCode keyCode, Event*) {
        if (keyCode == EventKeyboard::KeyCode::KEY_C) {
            auto scene = CollectionScreen::createScene();
            Director::getInstance()->replaceScene(scene);
        }
    };

    this->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, this);

    return true;
}
