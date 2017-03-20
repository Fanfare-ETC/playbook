//
// Created by ramya on 3/2/17.
//

#include <rapidjson/document.h>
#include "CollectionScreen.h"
#include "PredictionScene.h"
#include "PredictionWebSocket.h"
#include "MappedSprite.h"
#include "rapidjson/rapidjson.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

USING_NS_CC;
using namespace std;

//struct type_rect { float  array[4][2]; };
//typedef struct type_rect type_rect;

Scene* CollectionScreen::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::createWithPhysics();
    scene->setName("CollectionScreen");
    //scene->getPhysicsWorld()->setDebugDrawMask(PhysicsWorld::DEBUGDRAW_ALL);

    // 'layer' is an autorelease object
    auto layer = CollectionScreen::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool CollectionScreen::init()
{
    //////////////////////////////
    // 1. super init first
    if ( !Layer::init() )
    {
        return false;
    }
    
    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto winSize = Director::getInstance()->getWinSize();
    Vec2 origin = Director::getInstance()->getVisibleOrigin();
    auto scale =  Director::getInstance()->getContentScaleFactor();

    // Create Node that represents the visible portion of the screen.
    auto node = Node::create();
    node->setContentSize(visibleSize);
    node->setPosition(origin);
    this->addChild(node);

    // add grass to screen
    auto grass = Sprite::create("Collection-BG-Wood.png");
    grass->setPosition(0.0f, 0.0f);
    grass->setAnchorPoint(Vec2(0.0f, 0.0f));
    grass->setScaleX(visibleSize.width / grass->getContentSize().width);
    grass->setScaleY(visibleSize.height / grass->getContentSize().height);
    node->addChild(grass, 0);

    // add banner on top to screen
    auto banner = Sprite::create("Collection-Banner.png");
    auto bannerScale = visibleSize.width / banner->getContentSize().width;
    banner->setPosition(0.0f, visibleSize.height);
    banner->setAnchorPoint(Vec2(0.0f, 1.0f));
    banner->setScaleX(bannerScale);
    banner->setScaleY(bannerScale);
    auto bannerHeight = bannerScale * banner->getContentSize().height;
    node->addChild(banner, 0);

    //add ball tray in the bottom
     auto holder = Sprite::create("Collection-Holder-HandTray.png");
    auto holderScale = visibleSize.width /holder->getContentSize().width;
    holder->setPosition(0.0f, 0.0f);
    holder->setAnchorPoint(Vec2(0.0f, 0.0f));
    holder->setScaleX(holderScale);
    holder->setScaleY(holderScale);
    auto holderHeight =holderScale * holder->getContentSize().height;
    node->addChild(holder, 0);

    //add give to section button
    auto givetosection = Sprite::create("Collection-Button-GiveSection.png");
    auto givetosectionScale = visibleSize.width /givetosection->getContentSize().width;
    givetosection->setPosition(0.0f, visibleSize.height/3.0);
    givetosection->setAnchorPoint(Vec2(0.0f, 0.0f));
    givetosection->setScaleX(givetosectionScale/2);
    givetosection->setScaleY(givetosectionScale/2);
    auto givetosectionHeight =givetosectionScale * givetosection->getContentSize().height;
    node->addChild(givetosection, 0);

    //add dragtoscore button
    auto dragtoscore = Sprite::create("Collection-Button-ScoreSet.png");
    auto dragtoscoreScale = visibleSize.width /dragtoscore->getContentSize().width;
    dragtoscore->setPosition(visibleSize.width/2.0, visibleSize.height/3.0);
    dragtoscore->setAnchorPoint(Vec2(0.0f, 0.0f));
    dragtoscore->setScaleX(dragtoscoreScale/2);
    dragtoscore->setScaleY(dragtoscoreScale/2);
    auto dragtoscoreHeight =dragtoscoreScale * dragtoscore->getContentSize().height;
    node->addChild(dragtoscore, 0);

    //generate a random goal each time

    /* initialize random seed: */
    srand (time(NULL));

    /* generate secret number between 1 and 10: */
    int goal_number = rand() % 15 + 1;
    string file_1= "goal/goal";
    string file_2= std::to_string(goal_number);
    string file_3= ".png";
    string filename = file_1+file_2+file_3;
    CCLOG("filename:%s",filename.c_str());
    //add goals
    auto goal = Sprite::create(filename);
    auto goalScale = visibleSize.width /goal->getContentSize().width;
    goal->setPosition(visibleSize.width/1.75, visibleSize.height/1.4);
    goal->setAnchorPoint(Vec2(0.0f, 0.0f));
    goal->setScaleX(goalScale/3);
    goal->setScaleY(goalScale/3);
    auto goalHeight =goalScale * goal->getContentSize().height;
    node->addChild(goal, 0);
    receiveCard();
    return true;
}

void CollectionScreen::receiveCard()
{
   /** using namespace rapidjson;
    //get notified of a play on field
    //reusing preditcion_notifier
    // Create websocket client.
    auto websocket = PredictionWebSocket::create("ws://128.237.140.116:8080");
    websocket->connect();
    websocket->onConnectionOpened = []() {
        CCLOG("Connection to server established");
    };
    websocket->onMessageReceived = [this](std::string message) {
        CCLOG("Message received from server: %s", message.c_str());

        rapidjson::Document document;
        document.Parse(message.c_str());
        if (document.IsArray()) {
            for (auto it = document.Begin(); it != document.End(); ++it) {
                Prediction::PredictionEvent event = Prediction::intToEvent(it->GetInt());
                CCLOG("Events: %s", Prediction::eventToString(event).c_str());

            }
        } else {
            CCLOG("Received message is not an array!");
        }
    };
    websocket->onErrorOccurred = [](const cocos2d::network::WebSocket::ErrorCode& errorCode) {
        CCLOG("Error connecting to server: %d", errorCode);
    }; **/

}

void CollectionScreen::menuCloseCallback(Ref* pSender)
{
    //Close the cocos2d-x game scene and quit the application
    Director::getInstance()->end();

#if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
    exit(0);
#endif

    /*To navigate back to native iOS screen(if present) without quitting the application  ,do not use Director::getInstance()->end() and exit(0) as given above,instead trigger a custom event created in RootViewController.mm as below*/

    //EventCustom customEndEvent("game_scene_close_event");
    //_eventDispatcher->dispatchEvent(&customEndEvent);
}


