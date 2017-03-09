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
    return Layer::init();
}
