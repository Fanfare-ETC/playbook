#include "HelloWorldScene.h"
#include "SimpleAudioEngine.h"
#include "MappedSprite.h"

USING_NS_CC;
using namespace std;
//struct type_rect { float  array[4][2]; };
//typedef struct type_rect type_rect;

Scene* HelloWorld::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::createWithPhysics();
    scene->setName("HelloWorld");

    // 'layer' is an autorelease object
    auto layer = HelloWorld::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool HelloWorld::init()
{
    //////////////////////////////
    // 1. super init first
    if ( !Layer::init() )
    {
        return false;
    }

    auto visibleSize = Director::getInstance()->getVisibleSize();
    Vec2 origin = Director::getInstance()->getVisibleOrigin();

    // Create Node that represents the visible portion of the screen.
    auto node = Node::create();
    node->setContentSize(visibleSize);
    node->setPosition(origin);
    this->addChild(node);

    // add grass to screen
    auto grass = Sprite::create("Prediction-BG-Grass.png");
    grass->setPosition(0.0f, 0.0f);
    grass->setAnchorPoint(Vec2(0.0f, 0.0f));
    grass->setScaleX(visibleSize.width / grass->getContentSize().width);
    grass->setScaleY(visibleSize.height / grass->getContentSize().height);
    node->addChild(grass, 0);

    // add banner on top to screen
    auto banner = Sprite::create("Prediction-Banner.png");
    auto bannerScale = visibleSize.width / banner->getContentSize().width;
    banner->setPosition(0.0f, visibleSize.height);
    banner->setAnchorPoint(Vec2(0.0f, 1.0f));
    banner->setScale(bannerScale);
    auto bannerHeight = bannerScale * banner->getContentSize().height;
    node->addChild(banner, 1);


    // add ball slot to screen
    this->_ballSlot = Sprite::create("Prediction-Holder-BallsSlot.png");
    auto ballSlotScale = visibleSize.width / this->_ballSlot->getContentSize().width;
    this->_ballSlot->setPosition(0.0f, 0.0f);
    this->_ballSlot->setAnchorPoint(Vec2(0.0f, 0.0f));
    this->_ballSlot->setScale(ballSlotScale);
    auto ballSlotHeight = ballSlotScale * this->_ballSlot->getContentSize().height;
    node->addChild(this->_ballSlot, 2);

    // Add balls to ball slot.
    for (int i = 0; i < 5; i++) {
        auto ball = Sprite::create("Item-Ball.png");
        auto ballScale = this->_ballSlot->getContentSize().height / ball->getContentSize().height / 1.5f;
        ball->setPosition(110.0f + (ball->getContentSize().width * i * ballScale), this->_ballSlot->getContentSize().height / 2.0f);
        ball->setScale(ballScale);
        ball->setName("Ball " + std::to_string(i));
        this->_ballSlot->addChild(ball);

        // Add tracking information to the ball.
        BallState state {
            .dragState = false,
            .dragTouchID = 0,
            .dragOrigPosition = ball->getPosition(),
            .dragTargetState = false,
            .dragTarget = ""
        };
        this->_ballStates.push_back(state);
    }

    // Add continue banner.
    this->_continueBanner = Sprite::create("Prediction-Button-Continue.png");
    auto continueBannerScale = visibleSize.width / this->_continueBanner->getContentSize().width;
    this->_continueBanner->setPosition(0.0f, 0.0f);
    this->_continueBanner->setAnchorPoint(Vec2(0.0f, 0.0f));
    this->_continueBanner->setScale(continueBannerScale);
    this->_continueBanner->setVisible(false);
    node->addChild(this->_continueBanner, 2);

    // add overlay to screen
    this->initFieldOverlay();
    auto fieldOverlayScaleX = visibleSize.width / this->_fieldOverlay->getContentSize().width;
    auto fieldOverlayScaleY = (visibleSize.height - bannerHeight - ballSlotHeight) / this->_fieldOverlay->getContentSize().height;
    auto fieldOverlayScale = std::min(fieldOverlayScaleX, fieldOverlayScaleY);
    this->_fieldOverlay->setPosition(visibleSize.width / 2.0f, visibleSize.height - bannerHeight);
    this->_fieldOverlay->setAnchorPoint(Vec2(0.5f, 1.0f));
    this->_fieldOverlay->setScale(fieldOverlayScale);
    node->addChild(this->_fieldOverlay, 1);

    // Create event listeners.
    this->initEvents();
    this->scheduleUpdate();

    return true;
}

void HelloWorld::initFieldOverlay() {
    // add overlay to screen
    std::map<std::string, MappedSprite::Polygon> polygons;

    polygons.insert({"error", {
            Vec2(12.0f, 1908.0f),
            Vec2(408.0f, 1908.0f),
            Vec2(456.0f, 1732.0f),
            Vec2(12.0f, 1474.0f),
    }});

    polygons.insert({"grand_slam", {
            Vec2(424.0f, 1908.0f),
            Vec2(1016.0f, 1908.0f),
            Vec2(970.0f, 1736.0f),
            Vec2(728.0f, 1768.0f),
            Vec2(470.0f, 1736.0f),
    }});

    polygons.insert({"shutout_inning", {
            Vec2(1030.0f, 1908.0f),
            Vec2(1426.0f, 1908.0f),
            Vec2(1426.0f, 1472.0f),
            Vec2(984.0f, 1734.0f)
    }});

    polygons.insert({"long_out", {
            Vec2(12.0f, 1448.0f),
            Vec2(224.0f, 1618.0f),
            Vec2(352.0f, 1394.0f),
            Vec2(144.0f, 1208.0f),
            Vec2(12.0f, 918.0f)
    }});

    polygons.insert({"runs_batted_in", {
            Vec2(238.0f, 1626.0f),
            Vec2(466.0f, 1720.0f),
            Vec2(716.0f, 1754.0f),
            Vec2(976.0f, 1720.0f),
            Vec2(1202.0f, 1626.0f),
            Vec2(1074.0f, 1404.0f),
            Vec2(716.0f, 1494.0f),
            Vec2(364.0f, 1404.0f)
    }});

    polygons.insert({"pop_fly", {
            Vec2(1216.0f, 1618.0f),
            Vec2(1428.0f, 1448.0f),
            Vec2(1428.0f, 918.0f),
            Vec2(1296.0f, 1208.0f),
            Vec2(1088.0f, 1394.0f)
    }});

    polygons.insert({"triple_play", {
            Vec2(216.0f, 1269.0f),
            Vec2(462.0f, 1018.0f),
            Vec2(322.0f, 880.0f),
            Vec2(366.0f, 784.0f),
            Vec2(332.0f, 784.0f),
            Vec2(204.0f, 908.0f),
            Vec2(76.0f, 784.0f),
            Vec2(12.0f, 784.0f),
            Vec2(64.0f, 1038.0f)
    }});

    polygons.insert({"grounder", {
            Vec2(1224.0f, 1269.0f),
            Vec2(1376.0f, 1038.0f),
            Vec2(1428.0f, 784.0f),
            Vec2(1364.0f, 784.0f),
            Vec2(1236.0f, 908.0f),
            Vec2(1108.0f, 784.0f),
            Vec2(1074.0f, 784.0f),
            Vec2(1118.0f, 880.0f),
            Vec2(976.0f, 1018.0f)
    }});

    polygons.insert({"double_play", {
            Vec2(224.0f, 1278.0f),
            Vec2(442.0f, 1426.0f),
            Vec2(720.0f, 1480.f),
            Vec2(998.0f, 1426.0f),
            Vec2(1216.0f, 1278.0f),
            Vec2(964.0f, 1032.0f),
            Vec2(832.0f, 1172.0f),
            Vec2(720.0f, 1128.0f),
            Vec2(608.0f, 1172.0f),
            Vec2(476.0f, 1032.0f)
    }});

    polygons.insert({"second_base", {
            Vec2(720.0f, 1404.0f),
            Vec2(834.0f, 1292.0f),
            Vec2(720.0f, 1180.0f),
            Vec2(606.0f, 1292.0f)
    }});

    polygons.insert({"steal", {
            Vec2(484.0f, 1022.0f),
            Vec2(616.0f, 1152.0f),
            Vec2(720.0f, 1112.0f),
            Vec2(824.0f, 1152.0f),
            Vec2(956.0f, 1022.0f),
            Vec2(808.0f, 874.0f),
            Vec2(720.0f, 906.0f),
            Vec2(632.0f, 874.0f)
    }});

    polygons.insert({"pick_off", {
            Vec2(474.0f, 1010.0f),
            Vec2(622.0f, 864.0f),
            Vec2(588.0f, 774.0f),
            Vec2(622.0f, 688.0f),
            Vec2(474.0f, 540.0f),
            Vec2(342.0f, 670.0f),
            Vec2(382.0f, 774.0f),
            Vec2(342.0f, 880.0f)
    }});

    polygons.insert({"strike_out", {
            Vec2(720.0f, 892.0f),
            Vec2(804.0f, 860.0f),
            Vec2(838.0f, 776.0f),
            Vec2(804.0f, 692.0f),
            Vec2(720.0f, 658.0f),
            Vec2(636.0f, 692.0f),
            Vec2(602.0f, 776.0f),
            Vec2(636.0f, 860.0f),
    }});

    polygons.insert({"walk", {
            Vec2(966.0f, 1010.0f),
            Vec2(1098.0f, 880.0f),
            Vec2(1058.0f, 774.0f),
            Vec2(1098.0f, 670.0f),
            Vec2(966.0f, 540.0f),
            Vec2(818.0f, 688.0f),
            Vec2(852.0f, 774.0f),
            Vec2(818.0f, 864.0f)
    }});

    polygons.insert({"third_base", {
            Vec2(204.0f, 888.0f),
            Vec2(92.0f, 776.0f),
            Vec2(204.0f, 660.0f),
            Vec2(316.0f, 776.0f)
    }});

    polygons.insert({"first_base", {
            Vec2(1236.0f, 888.0f),
            Vec2(1124.0f, 776.0f),
            Vec2(1236.0f, 660.0f),
            Vec2(1348.0f, 776.0f)
    }});

    polygons.insert({"hit", {
            Vec2(12.0f, 768.0f),
            Vec2(76.0f, 768.0f),
            Vec2(204.0f, 644.0f),
            Vec2(332.0f, 768.0f),
            Vec2(368.0f, 768.0f),
            Vec2(322.0f, 672.0f),
            Vec2(616.0f, 378.0f),
            Vec2(714.0f, 422.0f),
            Vec2(714.0f, 334.0f),
            Vec2(600.0f, 334.0f),
            Vec2(600.0f, 186.0f),
            Vec2(714.0f, 74.0f),
            Vec2(714.0f, 12.0f),
            Vec2(488.0f, 12.0f),
            Vec2(12.0f, 488.0f)
    }});

    polygons.insert({"home_run", {
            Vec2(612.0f, 322.0f),
            Vec2(828.0f, 322.0f),
            Vec2(828.0f, 190.0f),
            Vec2(720.0f, 90.0f),
            Vec2(612.0f, 190.0f)
    }});

    polygons.insert({"pitch_count_16", {
            Vec2(1428.0f, 768.0f),
            Vec2(1428.0f, 488.0f),
            Vec2(952.0f, 12.0f),
            Vec2(726.0f, 12.0f),
            Vec2(726.0f, 74.0f),
            Vec2(840.0f, 186.0f),
            Vec2(840.0f, 334.0f),
            Vec2(726.0f, 334.0f),
            Vec2(726.0f, 422.0f),
            Vec2(824.0f, 378.0f),
            Vec2(1118.0f, 672.0f),
            Vec2(1072.0f, 768.0f),
            Vec2(1108.0f, 768.0f),
            Vec2(1236.0f, 644.0f),
            Vec2(1364.0f, 768.0f)
    }});

    polygons.insert({"blocked_run", {
            Vec2(632.0f, 678.0f),
            Vec2(720.0f, 640.0f),
            Vec2(808.0f, 678.0f),
            Vec2(956.0f, 530.0f),
            Vec2(824.0f, 398.0f),
            Vec2(720.0f, 436.0f),
            Vec2(616.0f, 398.0f),
            Vec2(484.0f, 530.0f)
    }});

    polygons.insert({"walk_off", {
            Vec2(12.0f, 466.0f),
            Vec2(466.0f, 12.0f),
            Vec2(12.0f, 12.0f)
    }});

    polygons.insert({"pitch_count_17", {
            Vec2(1428.0f, 466.0f),
            Vec2(1428.0f, 12.0f),
            Vec2(974.0f, 12.0f)
    }});

    this->_fieldOverlay = MappedSprite::create("Prediction-Overlay-Field.png", polygons);

    this->_fieldOverlay->onTouchPolygonBegan = [this](const std::string& name,
                                                      MappedSprite::Polygon polygon,
                                                      const Touch* touch) {
        // Highlight the section with translucent black.
        this->_fieldOverlay->highlight(name, Color4F(Color3B::BLACK, 0.2f), 0, Color4F::WHITE);

        // Store the currently hovered item.
        auto it = std::find_if(this->_ballStates.begin(), this->_ballStates.end(), [touch](BallState state) {
            return state.dragTouchID == touch->getID() && state.dragState;
        });

        if (it != this->_ballStates.end()) {
            it->dragTargetState = true;
            it->dragTarget = name;
        }
    };

    this->_fieldOverlay->onTouchPolygonEnded = [this](const std::string& name,
                                                      MappedSprite::Polygon polygon,
                                                      const Touch* touch) {
        // Clear the previously created highlight.
        this->_fieldOverlay->clearHighlight(name);

        // Remove the currently covered item.
        auto it = std::find_if(this->_ballStates.begin(), this->_ballStates.end(), [touch](BallState state) {
            return state.dragTouchID == touch->getID() && state.dragState;
        });

        if (it != this->_ballStates.end()) {
            it->dragTargetState = false;
            it->dragTarget = "";
        }
    };
}

void HelloWorld::initEvents() {
    auto listener = EventListenerTouchAllAtOnce::create();

    listener->onTouchesBegan = [this](const std::vector<Touch*>& touches, Event* event) {
        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            auto balls = this->_ballSlot->getChildren();
            for (auto it = balls.begin(); it != balls.end(); ++it) {
                auto localLocation = (*it)->getParent()->convertTouchToNodeSpace(touch);
                auto box = (*it)->getBoundingBox();

                // Save the tracking information needed for multi-touch to work.
                if (box.containsPoint(localLocation)) {
                    auto ballIdx = it - balls.begin();
                    this->_ballStates[ballIdx].dragState = true;
                    this->_ballStates[ballIdx].dragTouchID = touch->getID();
                }
            }
        }
    };

    listener->onTouchesEnded = [this](const std::vector<Touch*>& touches, Event* event) {
        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            auto balls = this->_ballSlot->getChildren();
            for (auto it = balls.begin(); it != balls.end(); ++it) {
                auto ballIdx = it - balls.begin();

                // If the removed touch ID is matched to a ball, it means that
                // a finger was lifted off the ball.
                auto& state = this->_ballStates[ballIdx];
                if (state.dragState && touch->getID() == state.dragTouchID) {
                    // If the ball was being dragged and has a target, we remove
                    // the ball from the scene.
                    if (state.dragTargetState) {
                        (*it)->setVisible(false);
                    } else {
                        auto moveTo = MoveTo::create(0.25f, state.dragOrigPosition);
                        (*it)->runAction(moveTo);
                    }

                    state.dragState = false;
                }
            }
        }
    };

    listener->onTouchesMoved = [this](const std::vector<Touch*>& touches, Event* event) {
        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            auto balls = this->_ballSlot->getChildren();
            for (auto it = balls.begin(); it != balls.end(); ++it) {
                auto ballIdx = it - balls.begin();

                // If the removed touch ID is matched to a ball, it means that
                // a finger was lifted off the ball.
                auto state = this->_ballStates[ballIdx];
                if (state.dragState && touch->getID() == state.dragTouchID) {
                    auto localLocation = (*it)->getParent()->convertTouchToNodeSpace(touch);
                    (*it)->setPosition(localLocation);
                }
            }
        }
    };

    listener->onTouchesCancelled = [this](const std::vector<Touch*>& touches, Event* event) {
        CCLOG("onTouchesCancelled: touches.size(): %d", touches.size());
    };

    this->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, this);
}

void HelloWorld::update(float delta) {
    switch (this->_state) {
        case SceneState::INITIAL: {
            auto balls = this->_ballSlot->getChildren();
            auto shouldContinue = std::all_of(balls.begin(), balls.end(), [](Node *ball) {
                return !ball->isVisible();
            });

            if (shouldContinue) {
                this->_state = SceneState::CONTINUE;
                this->_continueBanner->setVisible(true);
            }
            break;
        }

        case SceneState::CONTINUE:
            break;
    }



}

void HelloWorld::menuCloseCallback(Ref* pSender)
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


