#include "json/writer.h"
#include "json/stringbuffer.h"

#include "PredictionScene.h"
#include "PredictionWebSocket.h"
#include "PlaybookEvent.h"

USING_NS_CC;

#ifndef PLAYBOOK_API_HOST
#define PLAYBOOK_API_HOST 10.0.2.2
#endif // PLAYBOOK_API_HOST

#ifndef PLAYBOOK_API_PORT
#define PLAYBOOK_API_PORT 8080
#endif // PLAYBOOK_API_PORT

#define STR_VALUE(arg) #arg
#define STR_VALUE_VAR(arg) STR_VALUE(arg)
#define PLAYBOOK_WEBSOCKET_URL "ws://" \
    STR_VALUE_VAR(PLAYBOOK_API_HOST) \
    ":" STR_VALUE_VAR(PLAYBOOK_API_PORT)

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
    auto grass = Sprite::create("Prediction-BG-Grass.jpg");
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
        auto ballPosition = this->getBallPositionForSlot(ball, i);
        ball->setPosition(ballPosition);
        ball->setScale(ballScale * ballSlotScale);
        ball->setName("Ball " + std::to_string(i));
        node->addChild(ball, 3);

        // Add tracking information to the ball.
        Ball state {
            .dragState = false,
            .dragTouchID = 0,
            .dragOrigPosition = ball->getPosition(),
            .dragTargetState = false,
            .dragTarget = "",
            .selectedTargetState = false,
            .selectedTarget = "",
            .sprite = ball
        };
        this->_balls.push_back(state);
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

void Prediction::initFieldOverlay() {
    // add overlay to screen
    std::map<std::string, MappedSprite::Polygon> polygons;

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::error), {
            Vec2(12.0f, 1908.0f),
            Vec2(408.0f, 1908.0f),
            Vec2(456.0f, 1732.0f),
            Vec2(12.0f, 1474.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::grandslam), {
            Vec2(424.0f, 1908.0f),
            Vec2(1016.0f, 1908.0f),
            Vec2(970.0f, 1736.0f),
            Vec2(728.0f, 1768.0f),
            Vec2(470.0f, 1736.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::shutout_inning), {
            Vec2(1030.0f, 1908.0f),
            Vec2(1426.0f, 1908.0f),
            Vec2(1426.0f, 1472.0f),
            Vec2(984.0f, 1734.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::longout), {
            Vec2(12.0f, 1448.0f),
            Vec2(224.0f, 1618.0f),
            Vec2(352.0f, 1394.0f),
            Vec2(144.0f, 1208.0f),
            Vec2(12.0f, 918.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::runs_batted), {
            Vec2(238.0f, 1626.0f),
            Vec2(466.0f, 1720.0f),
            Vec2(716.0f, 1754.0f),
            Vec2(976.0f, 1720.0f),
            Vec2(1202.0f, 1626.0f),
            Vec2(1074.0f, 1404.0f),
            Vec2(716.0f, 1494.0f),
            Vec2(364.0f, 1404.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::pop_fly), {
            Vec2(1216.0f, 1618.0f),
            Vec2(1428.0f, 1448.0f),
            Vec2(1428.0f, 918.0f),
            Vec2(1296.0f, 1208.0f),
            Vec2(1088.0f, 1394.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::triple_play), {
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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::grounder), {
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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::double_play), {
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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::twob), {
            Vec2(720.0f, 1404.0f),
            Vec2(834.0f, 1292.0f),
            Vec2(720.0f, 1180.0f),
            Vec2(606.0f, 1292.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::steal), {
            Vec2(484.0f, 1022.0f),
            Vec2(616.0f, 1152.0f),
            Vec2(720.0f, 1112.0f),
            Vec2(824.0f, 1152.0f),
            Vec2(956.0f, 1022.0f),
            Vec2(808.0f, 874.0f),
            Vec2(720.0f, 906.0f),
            Vec2(632.0f, 874.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::pick_off), {
            Vec2(474.0f, 1010.0f),
            Vec2(622.0f, 864.0f),
            Vec2(588.0f, 774.0f),
            Vec2(622.0f, 688.0f),
            Vec2(474.0f, 540.0f),
            Vec2(342.0f, 670.0f),
            Vec2(382.0f, 774.0f),
            Vec2(342.0f, 880.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::strike_out), {
            Vec2(720.0f, 892.0f),
            Vec2(804.0f, 860.0f),
            Vec2(838.0f, 776.0f),
            Vec2(804.0f, 692.0f),
            Vec2(720.0f, 658.0f),
            Vec2(636.0f, 692.0f),
            Vec2(602.0f, 776.0f),
            Vec2(636.0f, 860.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::walk), {
            Vec2(966.0f, 1010.0f),
            Vec2(1098.0f, 880.0f),
            Vec2(1058.0f, 774.0f),
            Vec2(1098.0f, 670.0f),
            Vec2(966.0f, 540.0f),
            Vec2(818.0f, 688.0f),
            Vec2(852.0f, 774.0f),
            Vec2(818.0f, 864.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::threeb), {
            Vec2(204.0f, 888.0f),
            Vec2(92.0f, 776.0f),
            Vec2(204.0f, 660.0f),
            Vec2(316.0f, 776.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::oneb), {
            Vec2(1236.0f, 888.0f),
            Vec2(1124.0f, 776.0f),
            Vec2(1236.0f, 660.0f),
            Vec2(1348.0f, 776.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::hit), {
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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::homerun), {
            Vec2(612.0f, 322.0f),
            Vec2(828.0f, 322.0f),
            Vec2(828.0f, 190.0f),
            Vec2(720.0f, 90.0f),
            Vec2(612.0f, 190.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::pitchcount_16), {
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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::blocked_run), {
            Vec2(632.0f, 678.0f),
            Vec2(720.0f, 640.0f),
            Vec2(808.0f, 678.0f),
            Vec2(956.0f, 530.0f),
            Vec2(824.0f, 398.0f),
            Vec2(720.0f, 436.0f),
            Vec2(616.0f, 398.0f),
            Vec2(484.0f, 530.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::walk_off), {
            Vec2(12.0f, 466.0f),
            Vec2(466.0f, 12.0f),
            Vec2(12.0f, 12.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::pitchcount_17), {
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
        auto it = std::find_if(this->_balls.begin(), this->_balls.end(), [touch](Ball ball) {
            return ball.dragTouchID == touch->getID() && ball.dragState;
        });

        if (it != this->_balls.end()) {
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
        auto it = std::find_if(this->_balls.begin(), this->_balls.end(), [touch](Ball ball) {
            return ball.dragTouchID == touch->getID() && ball.dragState;
        });

        if (it != this->_balls.end()) {
            it->dragTargetState = false;
            it->dragTarget = "";
        }
    };

    this->_fieldOverlay->onTouchPolygon = [this](const std::string& name,
                                                 MappedSprite::Polygon polygon,
                                                 const Touch* touch) {
        // Handle clicks.
        auto nextBall = std::find_if(this->_balls.begin(), this->_balls.end(), [](Ball ball) {
            return !ball.selectedTargetState;
        });

        auto isDraggingBall = std::any_of(this->_balls.begin(), this->_balls.end(), [](Ball ball) {
            return ball.dragState;
        });

        if (!isDraggingBall && nextBall != this->_balls.end()) {
            this->moveBallToField(PlaybookEvent::stringToEvent(name), *nextBall);
            this->makePrediction(PlaybookEvent::stringToEvent(name), *nextBall);
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
        if (this->_state == SceneState::CONFIRMED) { return; }

        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            for (auto& ball : this->_balls) {
                auto localLocation = ball.sprite->getParent()->convertTouchToNodeSpace(touch);
                auto box = ball.sprite->getBoundingBox();

                // Save the tracking information needed for multi-touch to work.
                if (box.containsPoint(localLocation)) {
                    ball.dragOrigPosition = ball.sprite->getPosition();
                    ball.dragState = true;
                    ball.dragTouchID = touch->getID();
                }
            }
        }

        // If a ball is being dragged, we should hide the continue banner.
        if (this->_state == SceneState::CONTINUE) {
            auto ballIsDragged = std::any_of(this->_balls.begin(), this->_balls.end(), [](Ball ball) {
                return ball.dragState;
            });

            if (ballIsDragged) {
                auto box = this->_continueBanner->getBoundingBox();
                auto moveBy = MoveBy::create(0.25f, Vec2(0.0f, -box.size.height));
                this->_continueBanner->runAction(moveBy);
            }
        }
    };

    listener->onTouchesEnded = [this](const std::vector<Touch*>& touches, Event* event) {
        if (this->_state == SceneState::CONFIRMED) { return; }

        for (const auto& touch : touches) {
            // Check if the continue overlay was tapped.
            if (this->_state == SceneState::CONTINUE) {
                auto localLocation = this->_continueBanner->getParent()->convertTouchToNodeSpace(touch);
                auto box = this->_continueBanner->getBoundingBox();
                auto ballIsDragged = std::any_of(this->_balls.begin(), this->_balls.end(), [](Ball ball) {
                    return ball.dragState;
                });

                if (box.containsPoint(localLocation) && !ballIsDragged) {
                    this->_state = SceneState::CONFIRMED;
                }

                // When a ball is not dragged, we should re-show the continue banner.
                if (ballIsDragged) {
                    auto box = this->_continueBanner->getBoundingBox();
                    auto moveBy = MoveBy::create(0.25f, Vec2(0.0f, box.size.height));
                    this->_continueBanner->runAction(moveBy);
                }
            }

            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            for (auto& ball : this->_balls) {
                // If the removed touch ID is matched to a ball, it means that
                // a finger was lifted off the ball.
                if (ball.dragState && touch->getID() == ball.dragTouchID) {
                    // If the ball was being dragged and has a target, we move the ball to the
                    // target location.
                    if (ball.dragTargetState) {
                        this->moveBallToField(PlaybookEvent::stringToEvent(ball.dragTarget), ball);
                        this->makePrediction(PlaybookEvent::stringToEvent(ball.dragTarget), ball);
                    } else {
                        // Check if the ball is over the slot.
                        auto localLocation = this->_ballSlot->getParent()->convertTouchToNodeSpace(touch);
                        auto box = this->_ballSlot->getBoundingBox();
                        if (box.containsPoint(localLocation)) {
                            this->moveBallToSlot(ball);
                            if (ball.selectedTargetState) {
                                this->undoPrediction(PlaybookEvent::stringToEvent(ball.selectedTarget), ball);
                            }
                        } else {
                            auto moveTo = MoveTo::create(0.25f, ball.dragOrigPosition);
                            ball.sprite->runAction(moveTo);
                        }
                    }

                    ball.dragState = false;
                }
            }
        }
    };

    listener->onTouchesMoved = [this](const std::vector<Touch*>& touches, Event* event) {
        if (this->_state == SceneState::CONFIRMED) { return; }

        for (const auto& touch : touches) {
            // For each ball, check if the touch point is within the bounding box of the ball.
            // If the touch point is within the bounding box, then update the drag state.
            for (auto& ball : this->_balls) {
                // If the removed touch ID is matched to a ball, it means that
                // a finger was lifted off the ball.
                if (ball.dragState && touch->getID() == ball.dragTouchID) {
                    auto localLocation = ball.sprite->getParent()->convertTouchToNodeSpace(touch);
                    ball.sprite->setPosition(localLocation);
                }
            }
        }
    };

    listener->onTouchesCancelled = [this](const std::vector<Touch*>& touches, Event* event) {
        CCLOG("onTouchesCancelled: touches.size(): %d", touches.size());
    };

    this->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, this);
}

void Prediction::connectToServer() {
    // Create websocket client.
    auto websocket = PredictionWebSocket::create(PLAYBOOK_WEBSOCKET_URL);

    CCLOG("Connecting to %s", PLAYBOOK_WEBSOCKET_URL);
    websocket->connect();

    websocket->onConnectionOpened = []() {
        CCLOG("Connection to server established");
    };

    websocket->onMessageReceived = [this](std::string message) {
        CCLOG("Message received from server: %s", message.c_str());

        rapidjson::Document document;
        document.Parse(message.c_str());
        if (document.IsObject()) {
            auto eventIterator = document.FindMember("event");
            if (eventIterator == document.MemberEnd()) {
                CCLOGWARN("Received message doesn't contain property \"event\"!");
                return;
            }

            if (!eventIterator->value.IsString()) {
                CCLOGWARN("Property \"event\" is not a string!");
                return;
            }

            std::string eventStr (eventIterator->value.GetString());
            auto dataIterator = document.FindMember("data");
            this->handleServerMessage(eventStr, dataIterator, dataIterator != document.MemberEnd());
        } else {
            CCLOGWARN("Received message is not an object!");
        }
    };

    websocket->onErrorOccurred = [](const cocos2d::network::WebSocket::ErrorCode& errorCode) {
        CCLOG("Error connecting to server: %d", errorCode);
    };

    this->_websocket = websocket;
}

void Prediction::disconnectFromServer() {
    this->_websocket->close();
}

void Prediction::handleServerMessage(const std::string& event,
                                     const rapidjson::Value::ConstMemberIterator& dataIterator,
                                     bool hasData) {
    CCLOG("Handling event from server: \"%s\"", event.c_str());
    if (event == "server:playsCreated") {
        this->handlePlaysCreated(dataIterator, hasData);
    } else if (event == "server:clearPredictions") {
        this->handleClearPredictions(dataIterator, hasData);
    } else {
        CCLOGWARN("Unknown event \"%s\" received from server!", event.c_str());
    }
}

void Prediction::handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& dataIterator,
                                    bool hasData) {
    if (!hasData) {
        CCLOGWARN("Event \"server:playsCreated\" doesn't have data!");
        return;
    }

    if (!dataIterator->value.IsArray()) {
        CCLOG("Event \"server:playsCreated\" has data that's not an array!");
        return;
    }

    auto plays = dataIterator->value.GetArray();
    for (auto it = plays.Begin(); it != plays.End(); ++it) {
        PlaybookEvent::EventType event = PlaybookEvent::intToEvent(it->GetInt());
        CCLOG("Events: %s", PlaybookEvent::eventToString(event).c_str());
        this->processPredictionEvent(event);
    }
}

void Prediction::handleClearPredictions(const rapidjson::Value::ConstMemberIterator&, bool) {
    CCLOG("Prediction->handleClearPredictions");
    for (auto& ball : this->_balls) {
        if (ball.selectedTargetState) {
            this->undoPrediction(PlaybookEvent::stringToEvent(ball.selectedTarget), ball);
        }
        this->moveBallToSlot(ball);
    }

    this->_score = 0;
    this->_state = SceneState::INITIAL;
}

void Prediction::update(float delta) {
    switch (this->_state) {
        case SceneState::INITIAL:
        case SceneState::CONTINUE: {
            auto balls = this->_balls;
            auto shouldContinue = std::all_of(balls.begin(), balls.end(), [](Ball ball) {
                return ball.selectedTargetState;
            });

            if (shouldContinue) {
                this->_state = SceneState::CONTINUE;
                this->_continueBanner->setVisible(true);
            } else {
                this->_state = SceneState::INITIAL;
                this->_continueBanner->setVisible(false);
            }
            break;
        }

        case SceneState::CONFIRMED:
            this->_continueBanner->setVisible(false);
            break;
    }
}

void Prediction::onResume() {
    CCLOG("Prediction->onResume: Restoring state...");
    PlaybookLayer::onResume();
    this->restoreState();
    this->connectToServer();
}

void Prediction::onPause() {
    CCLOG("Prediction->onPause: Saving state...");
    this->saveState();
    PlaybookLayer::onPause();
    this->disconnectFromServer();
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
    for (auto& ball : this->_balls) {
        if (ball.selectedTargetState) {
            this->moveBallToField(
                PlaybookEvent::stringToEvent(ball.selectedTarget),
                ball,
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

int Prediction::getScoreForEvent(PlaybookEvent::EventType event) {
    std::unordered_map<PlaybookEvent::EventType, int, PlaybookEvent::EventTypeHash> map = {
        {PlaybookEvent::EventType::error, 15},
        {PlaybookEvent::EventType::grandslam, 400},
        {PlaybookEvent::EventType::shutout_inning, 4},
        {PlaybookEvent::EventType::longout, 5},
        {PlaybookEvent::EventType::runs_batted, 4},
        {PlaybookEvent::EventType::pop_fly, 2},
        {PlaybookEvent::EventType::triple_play, 1400},
        {PlaybookEvent::EventType::grounder, 2},
        {PlaybookEvent::EventType::double_play, 20},
        {PlaybookEvent::EventType::twob, 5},
        {PlaybookEvent::EventType::steal, 5},
        {PlaybookEvent::EventType::pick_off, 7},
        {PlaybookEvent::EventType::strike_out, 2},
        {PlaybookEvent::EventType::walk, 3},
        {PlaybookEvent::EventType::threeb, 20},
        {PlaybookEvent::EventType::oneb, 3},
        {PlaybookEvent::EventType::hit, 2},
        {PlaybookEvent::EventType::homerun, 10},
        {PlaybookEvent::EventType::pitchcount_16, 2},
        {PlaybookEvent::EventType::blocked_run, 10},
        {PlaybookEvent::EventType::walk_off, 50},
        {PlaybookEvent::EventType::pitchcount_17, 2}
    };

    return map[event];
}

void Prediction::moveBallToField(PlaybookEvent::EventType event, Ball& ball, bool withAnimation) {
    // Position the ball in the correct place.
    auto eventStr = PlaybookEvent::eventToString(event);
    auto position = this->_fieldOverlay->convertToWorldSpace(this->_fieldOverlay->getPolygonCenter(eventStr));
    position = this->_visibleNode->convertToNodeSpace(position);

    // Determine we need to run an animation.
    if (withAnimation) {
        auto moveTo = MoveTo::create(0.25f, position);
        ball.sprite->runAction(moveTo);
    } else {
        ball.sprite->setPosition(position);
    }
}

void Prediction::moveBallToSlot(Ball &ball, bool withAnimation) {
    // Find the empty slot for this ball.
    auto ballIterator = std::find_if(this->_balls.begin(), this->_balls.end(), [ball](Ball item) {
       return item.sprite == ball.sprite;
    });

    if (ballIterator != this->_balls.end()) {
        auto position = this->getBallPositionForSlot(ball.sprite, ballIterator - this->_balls.begin());
        if (withAnimation) {
            auto moveTo = MoveTo::create(0.25f, position);
            ball.sprite->runAction(moveTo);
        } else {
            ball.sprite->setPosition(position);
        }
    } else {
        CCLOGWARN("Prediction->moveBallToSlot: There should have been an empty slot!");
    }
}

Vec2 Prediction::getBallPositionForSlot(Sprite* ballSprite, int i) {
    auto ballScale = this->_ballSlot->getContentSize().height / ballSprite->getContentSize().height / 1.5f;
    auto ballWorldSpace = this->_ballSlot->convertToWorldSpace(Vec2(
        110.0f + ballSprite->getContentSize().width * i * ballScale,
        this->_ballSlot->getContentSize().height / 2.0f
    ));
    return this->_visibleNode->convertToNodeSpace(ballWorldSpace);
}

void Prediction::makePrediction(PlaybookEvent::EventType event, Ball& state) {
    // If a previous prediction has been made, update the prediction count.
    if (state.selectedTargetState) {
        this->_predictionCounts[event]--;
    }

    state.selectedTarget = PlaybookEvent::eventToString(event);
    state.selectedTargetState = true;

    if (this->_predictionCounts.find(event) != this->_predictionCounts.end()) {
        this->_predictionCounts[event]++;
    } else {
        this->_predictionCounts[event] = 1;
    }
}

void Prediction::undoPrediction(PlaybookEvent::EventType event, Ball& state) {
    this->_predictionCounts[event]--;
    if (this->_predictionCounts[event] == 0) {
        this->_predictionCounts.erase(event);
    }
    state.selectedTarget = "";
    state.selectedTargetState = false;
}

void Prediction::processPredictionEvent(PlaybookEvent::EventType event) {
    if (this->_predictionCounts.find(event) != this->_predictionCounts.end()) {
        auto count = this->_predictionCounts[event];
        CCLOG("Prediction correct: %s", PlaybookEvent::eventToString(event).c_str());

        // Multiply the count with the score.
        auto score = this->getScoreForEvent(event) * count;
        this->_score += score;

        // Show overlay.
        std::stringstream ss;
        ss << "Prediction correct: " << PlaybookEvent::eventToString(event) << std::endl;
        ss << "Your score is: " << this->_score << std::endl;
        this->createNotificationOverlay(ss.str());
    } else {
        CCLOG("Prediction not found: %s", PlaybookEvent::eventToString(event).c_str());
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
        auto event = PlaybookEvent::eventToString(item.first);
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
    for (const auto& item : this->_balls) {
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

    // Serialize the scene state.
    document.AddMember(
        rapidjson::Value("sceneState", allocator).Move(),
        rapidjson::Value(this->_state).Move(),
        allocator
    );

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
                auto event = PlaybookEvent::stringToEvent(item.name.GetString());
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
                this->_balls[idx].selectedTargetState = selectedTargetStateIterator->value.GetBool();
                this->_balls[idx].selectedTarget = selectedTargetIterator->value.GetString();
            }
        }
    }

    // Restore the scene state.
    auto sceneStateIterator = document.FindMember("sceneState");
    if (sceneStateIterator != document.MemberEnd()) {
        if (!sceneStateIterator->value.IsInt()) {
            CCLOGWARN("Prediction->unserialize: sceneState is not an integer!");
        } else {
            this->_state = (SceneState) sceneStateIterator->value.GetInt();
        }
    }
}
