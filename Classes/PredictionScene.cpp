#include "network/HttpClient.h"
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

#ifndef PLAYBOOK_SECTION_API_HOST
#define PLAYBOOK_SECTION_API_HOST localhost
#endif //PLAYBOOK_SECTION_API_HOST

#ifndef PLAYBOOK_SECTION_API_PORT
#define PLAYBOOK_SECTION_API_PORT 9002
#endif // PLAYBOOK_SECTION_API_PORT

#define STR_VALUE(arg) #arg
#define STR_VALUE_VAR(arg) STR_VALUE(arg)
#define PLAYBOOK_WEBSOCKET_URL "ws://" \
    STR_VALUE_VAR(PLAYBOOK_API_HOST) \
    ":" STR_VALUE_VAR(PLAYBOOK_API_PORT)
#define PLAYBOOK_SECTION_API_URL "http://" \
    STR_VALUE_VAR(PLAYBOOK_SECTION_API_HOST) \
    ":" STR_VALUE_VAR(PLAYBOOK_SECTION_API_PORT)

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

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::HOME_RUN), {
        Vec2(720.0f, 672.0f),
        Vec2(644.0f, 704.0f),
        Vec2(614.0f, 780.0f),
        Vec2(644.0f, 856.0f),
        Vec2(720.0f, 888.0f),
        Vec2(796.0f, 856.0f),
        Vec2(826.0f, 780.0f),
        Vec2(796.0f, 704.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::TRIPLE), {
        Vec2(530.0f, 598.0f),
        Vec2(476.0f, 678.0f),
        Vec2(456.0f, 780.0f),
        Vec2(476.0f, 882.0f),
        Vec2(530.0f, 962.0f),
        Vec2(632.0f, 860.0f),
        Vec2(602.0f, 780.0f),
        Vec2(632.0f, 700.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::DOUBLE), {
        Vec2(538.0f, 970.0f),
        Vec2(616.0f, 1020.0f),
        Vec2(720.0f, 1042.0f),
        Vec2(824.0f, 1020.0f),
        Vec2(902.0f, 970.0f),
        Vec2(800.0f, 868.0f),
        Vec2(720.0f, 896.0f),
        Vec2(640.0f, 868.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::SINGLE), {
        Vec2(910.0f, 598.0f),
        Vec2(808.0f, 700.0f),
        Vec2(838.0f, 780.0f),
        Vec2(808.0f, 860.0f),
        Vec2(910.0f, 962.0f),
        Vec2(964.0f, 882.0f),
        Vec2(984.0f, 780.0f),
        Vec2(964.0f, 678.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::STEAL), {
        Vec2(538.0f, 588.0f),
        Vec2(640.0f, 690.0f),
        Vec2(720.0f, 660.0f),
        Vec2(800.0f, 690.0f),
        Vec2(902.0f, 588.0f),
        Vec2(824.0f, 536.0f),
        Vec2(720.0f, 514.0f),
        Vec2(616.0f, 536.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::MOST_IN_INFIELD), {
        Vec2(12.0f, 784.0f),
        Vec2(42.0f, 984.0f),
        Vec2(134.0f, 1188.0f),
        Vec2(238.0f, 1298.0f),
        Vec2(320.0f, 1362.0f),
        Vec2(414.0f, 1418.0f),
        Vec2(514.0f, 1454.0f),
        Vec2(720.0f, 1488.0f),
        Vec2(926.0f, 1454.0f),
        Vec2(1026.0f, 1418.0f),
        Vec2(1120.0f, 1362.0f),
        Vec2(1202.0f, 1298.0f),
        Vec2(1306.0f, 1188.0f),
        Vec2(1398.0f, 984.0f),
        Vec2(1428.0f, 784.0f),

        Vec2(1268.0f, 784.0f),
        Vec2(720.0f, 1328.0f),
        Vec2(172.0f, 784.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::MOST_IN_RIGHT_OUTFIELD), {
        Vec2(1428.0f, 920.0f),
        Vec2(1328.0f, 1182.0f),
        Vec2(1126.0f, 1374.0f),
        Vec2(900.0f, 1464.0f),
        Vec2(728.0f, 1498.0f),
        Vec2(728.0f, 1738.0f),
        Vec2(1428.0f, 1738.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::MOST_IN_LEFT_OUTFIELD), {
        Vec2(12.0f, 920.0f),
        Vec2(12.0f, 1738.0f),
        Vec2(712.0f, 1738.0f),
        Vec2(712.0f, 1498.0f),
        Vec2(500.0f, 1464.0f),
        Vec2(314.0f, 1374.0f),
        Vec2(112.0f, 1182.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::SHUTOUT_INNING), {
        Vec2(726.0f, 1308.0f),
        Vec2(1252.0f, 784.0f),
        Vec2(996.0f, 784.0f),
        Vec2(916.0f, 972.0f),
        Vec2(726.0f, 1054.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::RUN_SCORED), {
        Vec2(714.0f, 1308.0f),
        Vec2(714.0f, 1054.0f),
        Vec2(524.0f, 972.0f),
        Vec2(444.0f, 784.0f),
        Vec2(188.0f, 784.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::FLY_OUT), {
        Vec2(726.0f, 224.0f),
        Vec2(726.0f, 500.0f),
        Vec2(920.0f, 582.0f),
        Vec2(1000.0f, 772.0f),
        Vec2(1252.0f, 772.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::GROUND_OUT), {
        Vec2(714.0f, 224.0f),
        Vec2(188.0f, 772.0f),
        Vec2(440.0f, 772.0f),
        Vec2(520.0f, 582.0f),
        Vec2(714.0f, 500.0f)
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::BATTER_COUNT_5), {
        Vec2(1010.0f, 508.0f),
        Vec2(1270.0f, 770.0f),
        Vec2(1428.0f, 770.0f),
        Vec2(1428.0f, 508.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::BATTER_COUNT_4), {
        Vec2(720.0f, 238.0f),
        Vec2(998.0f, 498.0f),
        Vec2(1428.0f, 498.0f),
        Vec2(1428.0f, 238.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::PITCH_COUNT_17), {
        Vec2(12.0f, 508.0f),
        Vec2(12.0f, 770.0f),
        Vec2(170.0f, 770.0f),
        Vec2(430.0f, 508.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::PITCH_COUNT_16), {
        Vec2(12.0f, 238.0f),
        Vec2(12.0f, 498.0f),
        Vec2(442.0f, 498.0f),
        Vec2(720.0f, 238.0f),
    }});

    polygons.insert({PlaybookEvent::eventToString(PlaybookEvent::EventType::STRIKEOUT), {
        Vec2(12.0f, 12.0f),
        Vec2(12.0f, 224.0f),
        Vec2(1428.0f, 224.0f),
        Vec2(1428.0f, 12.0f),
    }});

    this->_fieldOverlay = MappedSprite::create("Prediction-Overlay.png", polygons);

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
#if CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID
                    for (const auto& pair : this->_predictionCounts) {
                        JniHelper::callStaticVoidMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "addPrediction",
                                                        static_cast<int>(pair.first), pair.second);
                    }
#endif
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
#if CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID
    JniHelper::callStaticVoidMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "clearPredictions");
#endif
}

void Prediction::reportScore(int score) {
    using namespace rapidjson;
#if CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID
    auto playerID = JniHelper::callStaticStringMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "getPlayerID");
#else
    std::string playerID ("Player");
#endif

    Document document;
    document.SetObject();
    Document::AllocatorType& allocator = document.GetAllocator();

    // Serialize the score report.
    document.AddMember(
        rapidjson::Value("cat", allocator).Move(),
        rapidjson::Value("predict", allocator).Move(),
        allocator
    );

    document.AddMember(
        rapidjson::Value("predictScore", allocator).Move(),
        rapidjson::Value(score).Move(),
        allocator
    );

    document.AddMember(
        rapidjson::Value("id", allocator).Move(),
        rapidjson::Value(playerID.c_str(), allocator).Move(),
        allocator
    );

    // Create the JSON.
    StringBuffer buffer;
    Writer<StringBuffer> writer(buffer);
    document.Accept(writer);

    // Send the JSON to the server.
    CCLOG("Sending score to: %s", PLAYBOOK_SECTION_API_URL "/updateScore");
    network::HttpRequest* request = new network::HttpRequest();
    request->setUrl(PLAYBOOK_SECTION_API_URL "/updateScore");
    request->setRequestType(network::HttpRequest::Type::POST);
    request->setRequestData(buffer.GetString(), buffer.GetSize());
    request->setHeaders({ "Content-Type: application/json" });
    request->setResponseCallback([](network::HttpClient*, network::HttpResponse* response) {
        if (response != nullptr) {
            // Dump the data
            CCLOG("Response: %s", response->getResponseDataString());
        }
    });
    network::HttpClient::getInstance()->send(request);
    request->release();
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

    // Re-register the predictions with the Java side.
#if CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID
    if (this->_state == SceneState::CONFIRMED) {
        JniHelper::callStaticVoidMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "clearPredictions");
        for (const auto& pair : this->_predictionCounts) {
            JniHelper::callStaticVoidMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "addPrediction",
                                            static_cast<int>(pair.first), pair.second);
        }
    }
#endif
}

void Prediction::saveState() {
    auto preferences = UserDefault::getInstance();
    preferences->setStringForKey("Prediction", this->serialize());
    preferences->flush();
}

int Prediction::getScoreForEvent(PlaybookEvent::EventType event) {
    std::unordered_map<PlaybookEvent::EventType, int, PlaybookEvent::EventTypeHash> map = {
        {PlaybookEvent::EventType::SHUTOUT_INNING, 4},
        {PlaybookEvent::EventType::RUN_SCORED, 4},
        {PlaybookEvent::EventType::FLY_OUT, 2},
        {PlaybookEvent::EventType::TRIPLE_PLAY, 1400},
        {PlaybookEvent::EventType::GROUND_OUT, 2},
        {PlaybookEvent::EventType::DOUBLE_PLAY, 20},
        {PlaybookEvent::EventType::DOUBLE, 5},
        {PlaybookEvent::EventType::STEAL, 5},
        {PlaybookEvent::EventType::PICK_OFF, 7},
        {PlaybookEvent::EventType::STRIKEOUT, 2},
        {PlaybookEvent::EventType::WALK, 3},
        {PlaybookEvent::EventType::TRIPLE, 20},
        {PlaybookEvent::EventType::SINGLE, 3},
        {PlaybookEvent::EventType::HIT_BY_PITCH, 2},
        {PlaybookEvent::EventType::HOME_RUN, 10},
        {PlaybookEvent::EventType::PITCH_COUNT_16, 2},
        {PlaybookEvent::EventType::BLOCKED_RUN, 10},
        {PlaybookEvent::EventType::PITCH_COUNT_17, 2},
        {PlaybookEvent::EventType::BATTER_COUNT_4, 2},
        {PlaybookEvent::EventType::BATTER_COUNT_5, 2},
        {PlaybookEvent::EventType::MOST_IN_LEFT_OUTFIELD, 2},
        {PlaybookEvent::EventType::MOST_IN_RIGHT_OUTFIELD, 2},
        {PlaybookEvent::EventType::MOST_IN_INFIELD, 2}
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
        auto score = Prediction::getScoreForEvent(event) * count;
        this->_score += score;

        // Show overlay.
        std::stringstream ss;
        ss << "Prediction correct: " << PlaybookEvent::eventToString(event) << std::endl;
        ss << "Your score is: " << this->_score << std::endl;
        this->createNotificationOverlay(ss.str());
        this->reportScore(score);
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
