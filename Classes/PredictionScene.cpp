#include <json/document.h>
#include "PredictionScene.h"
#include "PredictionWebSocket.h"
#include "rapidjson/rapidjson.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

USING_NS_CC;

Scene* Prediction::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::createWithPhysics();
    scene->setName("Prediction");

    // 'layer' is an autorelease object
    auto layer = Prediction::create();

    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool Prediction::init()
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
    this->_visibleNode = node;
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

    // Add balls to scene.
    for (int i = 0; i < 5; i++) {
        auto ball = Sprite::create("Item-Ball.png");
        auto ballScale = this->_ballSlot->getContentSize().height / ball->getContentSize().height / 1.5f;
        auto ballWorldSpace = this->_ballSlot->convertToWorldSpace(Vec2(
            110.0f + ball->getContentSize().width * i * ballScale,
            this->_ballSlot->getContentSize().height / 2.0f
        ));
        auto ballNodeSpace = node->convertToNodeSpace(ballWorldSpace);
        ball->setPosition(ballNodeSpace);
        ball->setScale(ballScale * ballSlotScale);
        ball->setName("Ball " + std::to_string(i));
        this->_balls.push_back(ball);
        node->addChild(ball, 3);

        // Add tracking information to the ball.
        BallState state {
            .dragState = false,
            .dragTouchID = 0,
            .dragOrigPosition = ball->getPosition(),
            .dragTargetState = false,
            .dragTarget = "",
            .selectedTargetState = false,
            .selectedTarget = "",
            .sprite = ball
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
                PredictionEvent event = this->intToEvent(it->GetInt());
                CCLOG("Events: %s", this->eventToString(event).c_str());
                this->processPredictionEvent(event);
            }
        } else {
            CCLOG("Received message is not an array!");
        }
    };
    websocket->onErrorOccurred = [](const cocos2d::network::WebSocket::ErrorCode& errorCode) {
        CCLOG("Error connecting to server: %d", errorCode);
    };

    return true;
}

void Prediction::initFieldOverlay() {
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

void Prediction::createNotificationOverlay(const std::string& text) {
    auto visibleSize = Director::getInstance()->getVisibleSize();

    // Draw background on overlay.
    auto overlay = DrawNode::create();
    overlay->drawSolidRect(Vec2(0.0f, 0.0f), Vec2(visibleSize.width, visibleSize.height), Color4F(Color3B::BLACK, 0.75f));
    overlay->setAnchorPoint(Vec2(0.0f, 0.0f));

    // Use ball as text placeholder.
    auto ball = Sprite::create("Item-Ball-Rotated.png");
    auto ballScale = visibleSize.width / ball->getContentSize().width;
    ball->setScale(ballScale);
    ball->setPosition(visibleSize.width / 2, visibleSize.height / 2);
    overlay->addChild(ball, 0);

    auto label = Label::createWithTTF(text, "fonts/nova1.ttf", 72.0f);
    label->setColor(Color3B::BLACK);
    label->setPosition(visibleSize.width / 2, visibleSize.height / 2);
    label->setAlignment(TextHAlignment::CENTER);
    overlay->addChild(label, 1);

    // Remove notification overlay on tap.
    auto listener = EventListenerTouchOneByOne::create();
    overlay->retain();
    listener->onTouchBegan = [this, overlay](Touch* touch, Event*) {
        this->_visibleNode->removeChild(overlay, true);
        overlay->release();
        return true;
    };
    overlay->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, overlay);

    // Add this overlay to the root node.
    this->_visibleNode->addChild(overlay, 3);
}

void Prediction::initEvents() {
    auto listener = EventListenerTouchAllAtOnce::create();

    listener->onTouchesBegan = [this](const std::vector<Touch*>& touches, Event* event) {
        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            auto balls = this->_balls;
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
            auto balls = this->_balls;
            for (auto it = balls.begin(); it != balls.end(); ++it) {
                auto ballIdx = it - balls.begin();

                // If the removed touch ID is matched to a ball, it means that
                // a finger was lifted off the ball.
                auto& state = this->_ballStates[ballIdx];
                if (state.dragState && touch->getID() == state.dragTouchID) {
                    // If the ball was being dragged and has a target, we remove
                    // the ball from the scene.
                    if (state.dragTargetState) {
                        this->moveBallToField(this->stringToEvent(state.dragTarget), *it);
                        this->makePrediction(this->stringToEvent(state.dragTarget), state);
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
            auto balls = this->_balls;
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

void Prediction::update(float delta) {
    switch (this->_state) {
        case SceneState::INITIAL: {
            auto balls = this->_balls;
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

void Prediction::onEnter() {
    CCLOG("Prediction->onEnter: Restoring state...");
    this->restoreState();
    Layer::onEnter();
}

void Prediction::onExit() {
    CCLOG("Prediction->onExit: Saving state...");
    Layer::onExit();
    this->saveState();
}

void Prediction::restoreState() {
    auto preferences = UserDefault::getInstance();
    auto state = preferences->getStringForKey("Prediction");
    if (!state.empty()) {
        CCLOG("Prediction->restoreState: Restoring: %s", state.c_str());
        this->unserialize(state);
    } else {
        CCLOG("Prediction->restoreState: No state to restore");
    }

    // Move the balls based on ballState.
    for (const auto& state : this->_ballStates) {
        if (state.selectedTargetState) {
            this->moveBallToField(
                this->stringToEvent(state.selectedTarget),
                state.sprite,
                false
            );
        }
    }
}

void Prediction::saveState() {
    auto preferences = UserDefault::getInstance();
    preferences->setStringForKey("Prediction", this->serialize());
    preferences->flush();
}

Prediction::PredictionEvent Prediction::stringToEvent(const std::string &event) {
    std::unordered_map<std::string, PredictionEvent> map = {
        {"error", PredictionEvent::error},
        {"grand_slam", PredictionEvent::grandslam},
        {"shutout_inning", PredictionEvent::shutout_inning},
        {"long_out", PredictionEvent::longout},
        {"runs_batted_in", PredictionEvent::runs_batted},
        {"pop_fly", PredictionEvent::pop_fly},
        {"triple_play", PredictionEvent::triple_play},
        {"grounder", PredictionEvent::grounder},
        {"double_play", PredictionEvent::double_play},
        {"second_base", PredictionEvent::twob},
        {"steal", PredictionEvent::steal},
        {"pick_off", PredictionEvent::pick_off},
        {"strike_out", PredictionEvent::strike_out},
        {"walk", PredictionEvent::walk},
        {"third_base", PredictionEvent::threeb},
        {"first_base", PredictionEvent::oneb},
        {"hit", PredictionEvent::hit},
        {"home_run", PredictionEvent::homerun},
        {"pitch_count_16", PredictionEvent::pitchcount_16},
        {"blocked_run", PredictionEvent::blocked_run},
        {"walk_off", PredictionEvent::walk_off},
        {"pitch_count_17", PredictionEvent::pitchcount_17}
    };

    return map[event];
}

std::string Prediction::eventToString(PredictionEvent event) {
    std::unordered_map<PredictionEvent, std::string, PredictionEventHash> map = {
        {PredictionEvent::error, "error"},
        {PredictionEvent::grandslam, "grand_slam"},
        {PredictionEvent::shutout_inning, "shutout_inning"},
        {PredictionEvent::longout, "long_out"},
        {PredictionEvent::runs_batted, "runs_batted_in"},
        {PredictionEvent::pop_fly, "pop_fly"},
        {PredictionEvent::triple_play, "triple_play"},
        {PredictionEvent::grounder, "grounder"},
        {PredictionEvent::double_play, "double_play"},
        {PredictionEvent::twob, "second_base"},
        {PredictionEvent::steal, "steal"},
        {PredictionEvent::pick_off, "pick_off"},
        {PredictionEvent::strike_out, "strike_out"},
        {PredictionEvent::walk, "walk"},
        {PredictionEvent::threeb, "third_base"},
        {PredictionEvent::oneb, "first_base"},
        {PredictionEvent::hit, "hit"},
        {PredictionEvent::homerun, "home_run"},
        {PredictionEvent::pitchcount_16, "pitch_count_16"},
        {PredictionEvent::blocked_run, "blocked_run"},
        {PredictionEvent::walk_off, "walk_off"},
        {PredictionEvent::pitchcount_17, "pitch_count_17"}
    };

    return map[event];
}

Prediction::PredictionEvent Prediction::intToEvent(int event) {
    std::vector<PredictionEvent> map {
        PredictionEvent::error,
        PredictionEvent::grandslam,
        PredictionEvent::shutout_inning,
        PredictionEvent::longout,
        PredictionEvent::runs_batted,
        PredictionEvent::pop_fly,
        PredictionEvent::triple_play,
        PredictionEvent::double_play,
        PredictionEvent::grounder,
        PredictionEvent::steal,
        PredictionEvent::pick_off,
        PredictionEvent::walk,
        PredictionEvent::blocked_run,
        PredictionEvent::strike_out,
        PredictionEvent::hit,
        PredictionEvent::homerun,
        PredictionEvent::pitchcount_16,
        PredictionEvent::walk_off,
        PredictionEvent::pitchcount_17,
        PredictionEvent::oneb,
        PredictionEvent::twob,
        PredictionEvent::threeb
    };

    return map[event];
}

int Prediction::getScoreForEvent(PredictionEvent event) {
    std::unordered_map<PredictionEvent, int, PredictionEventHash> map = {
        {PredictionEvent::error, 15},
        {PredictionEvent::grandslam, 400},
        {PredictionEvent::shutout_inning, 4},
        {PredictionEvent::longout, 5},
        {PredictionEvent::runs_batted, 4},
        {PredictionEvent::pop_fly, 2},
        {PredictionEvent::triple_play, 1400},
        {PredictionEvent::grounder, 2},
        {PredictionEvent::double_play, 20},
        {PredictionEvent::twob, 5},
        {PredictionEvent::steal, 5},
        {PredictionEvent::pick_off, 7},
        {PredictionEvent::strike_out, 2},
        {PredictionEvent::walk, 3},
        {PredictionEvent::threeb, 20},
        {PredictionEvent::oneb, 3},
        {PredictionEvent::hit, 2},
        {PredictionEvent::homerun, 10},
        {PredictionEvent::pitchcount_16, 2},
        {PredictionEvent::blocked_run, 10},
        {PredictionEvent::walk_off, 50},
        {PredictionEvent::pitchcount_17, 2}
    };

    return map[event];
}

void Prediction::moveBallToField(PredictionEvent event, Sprite* ball, bool withAnimation) {
    // Position the ball in the correct place.
    auto eventStr = this->eventToString(event);
    auto position = this->_fieldOverlay->convertToWorldSpace(this->_fieldOverlay->getPolygonCenter(eventStr));
    position = this->_visibleNode->convertToNodeSpace(position);

    // Determine we need to run an animation.
    if (withAnimation) {
        auto moveTo = MoveTo::create(0.25f, position);
        ball->runAction(moveTo);
    } else {
        ball->setPosition(position);
    }
}

void Prediction::makePrediction(PredictionEvent event, BallState& state) {
    // If a previous prediction has been made, update the prediction count.
    if (state.selectedTargetState) {
        this->_predictionCounts[event]--;
    }

    state.selectedTarget = this->eventToString(event);
    state.selectedTargetState = true;

    if (this->_predictionCounts.find(event) != this->_predictionCounts.end()) {
        this->_predictionCounts[event]++;
    } else {
        this->_predictionCounts[event] = 1;
    }
}

void Prediction::processPredictionEvent(PredictionEvent event) {
    if (this->_predictionCounts.find(event) != this->_predictionCounts.end()) {
        auto count = this->_predictionCounts[event];
        CCLOG("Prediction correct: %s", this->eventToString(event).c_str());

        // Multiply the count with the score.
        auto score = this->getScoreForEvent(event) * count;
        this->_score += score;

        // Show overlay.
        std::stringstream ss;
        ss << "Prediction correct: " << this->eventToString(event) << std::endl;
        ss << "Your score is: " << this->_score << std::endl;
        this->createNotificationOverlay(ss.str());
    } else {
        CCLOG("Prediction not found: %s", this->eventToString(event).c_str());
    }
}

std::string Prediction::serialize() {
    using namespace rapidjson;

    Document document;
    document.SetObject();
    Document::AllocatorType& allocator = document.GetAllocator();

    // Serialize the prediction game state.
    rapidjson::Value predictionCounts (kObjectType);
    for (const auto& item : this->_predictionCounts) {
        auto event = this->eventToString(item.first);
        auto count = item.second;
        predictionCounts.AddMember(
            rapidjson::Value(event.c_str(), allocator).Move(),
            rapidjson::Value(count).Move(),
            allocator
        );
    }
    document.AddMember(rapidjson::Value("predictionCounts", allocator).Move(), predictionCounts, allocator);

    // Serialize the ball state.
    rapidjson::Value ballStates (kArrayType);
    for (const auto& item : this->_ballStates) {
        rapidjson::Value ballState (kObjectType);
        ballState.AddMember(
            rapidjson::Value("selectedTarget", allocator).Move(),
            rapidjson::Value(item.selectedTarget.c_str(), allocator).Move(),
            allocator
        );
        ballState.AddMember(
            rapidjson::Value("selectedTargetState", allocator).Move(),
            rapidjson::Value(item.selectedTargetState).Move(),
            allocator
        );
        ballStates.PushBack(ballState, allocator);
    }
    document.AddMember(rapidjson::Value("ballStates", allocator).Move(), ballStates, allocator);

    // Create the state.
    StringBuffer buffer;
    Writer<StringBuffer> writer(buffer);
    document.Accept(writer);

    return std::string(buffer.GetString());
}

void Prediction::unserialize(const std::string& data) {
    using namespace rapidjson;

    Document document;
    document.Parse(data.c_str());

    // Restore the prediction game state.
    auto predictionCountsIterator = document.FindMember("predictionCounts");
    if (predictionCountsIterator != document.MemberEnd()) {
        rapidjson::Value& predictionCounts = predictionCountsIterator->value;
        if (!predictionCounts.IsObject()) {
            CCLOGWARN("Prediction->unserialize: predictionCounts is not an object!");
        } else {
            for (const auto& item : predictionCounts.GetObject()) {
                auto event = this->stringToEvent(item.name.GetString());
                auto count = item.value.GetInt();
                this->_predictionCounts[event] = count;
            }
        }
    }

    // Restore the ball state.
    auto ballStatesIterator = document.FindMember("ballStates");
    if (ballStatesIterator != document.MemberEnd()) {
        rapidjson::Value& ballStates = ballStatesIterator->value;
        if (!ballStates.IsArray()) {
            CCLOGWARN("Prediction->unserialize: ballStates is not an array!");
        } else {
            auto ballStatesArray = ballStates.GetArray();
            for (auto it = ballStatesArray.Begin(); it != ballStatesArray.End(); ++it) {
                auto idx = it - ballStatesArray.Begin();

                if (!it->IsObject()) {
                    CCLOGWARN("Prediction->unserialize: ballStates[item] is not an object!");
                    continue;
                }

                auto ballState = it->GetObject();
                auto selectedTargetStateIterator = ballState.FindMember("selectedTargetState");
                auto selectedTargetIterator = ballState.FindMember("selectedTarget");

                // Check if serialized ball state is valid.
                if (selectedTargetStateIterator == ballState.MemberEnd()) {
                    CCLOGWARN("Prediction->unserialize: ballStates[item].selectedTargetState doesn't exist!");
                    continue;
                }

                if (selectedTargetIterator == ballState.MemberEnd()) {
                    CCLOGWARN("Prediction->unserialize: ballStates[item].selectedTarget doesn't exist!");
                    continue;
                }

                if (!selectedTargetStateIterator->value.IsBool()) {
                    CCLOGWARN("Prediction->unserialize: ballStates[item].selectedTargetState is not a bool!");
                    continue;
                }

                if (!selectedTargetIterator->value.IsString()) {
                    CCLOGWARN("Prediction->unserialize: ballStates[item].selectedTarget is not a string!");
                    continue;
                }

                // Sanity checks passed.
                this->_ballStates[idx].selectedTargetState = selectedTargetStateIterator->value.GetBool();
                this->_ballStates[idx].selectedTarget = selectedTargetIterator->value.GetString();
            }
        }
    }
}

void Prediction::menuCloseCallback(Ref* pSender)
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


