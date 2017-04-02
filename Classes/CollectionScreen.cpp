//
// Created by ramya on 3/2/17.
//

#include "network/HttpClient.h"
#include "json/writer.h"
#include "json/stringbuffer.h"

#include "CollectionScreen.h"
#include "PredictionScene.h"
#include "PredictionWebSocket.h"
#include "MappedSprite.h"
#include "BlurFilter.h"

USING_NS_CC;

#ifndef PLAYBOOK_API_HOST
#define PLAYBOOK_API_HOST localhost
#endif // PLAYBOOK_API_HOST

#ifndef PLAYBOOK_API_PORT
#define PLAYBOOK_API_PORT 9001
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

//struct type_rect { float  array[4][2]; };
//typedef struct type_rect type_rect;

const int CollectionScreen::NUM_SLOTS = 5;

const std::unordered_map<CollectionScreen::GoalType, std::string, CollectionScreen::GoalTypeHash> CollectionScreen::GOAL_TYPE_FILE_MAP = {
    {GoalType::IDENTICAL_CARDS_3, "goal/goal1.png"},
    {GoalType::IDENTICAL_CARDS_4, "goal/goal2.png"},
    //{GoalType::UNIQUE_OUT_CARDS_4, "goal/goal3.png"},
    {GoalType::IDENTICAL_CARDS_5, "goal/goal4.png"},
    {GoalType::WALK_OR_HIT_3, "goal/goal5.png"},
    {GoalType::OUT_3, "goal/goal6.png"},
    {GoalType::BASES_3, "goal/goal7.png"},
    {GoalType::EACH_COLOR_2, "goal/goal8.png"},
    {GoalType::SAME_COLOR_3, "goal/goal9.png"},
    {GoalType::EACH_COLOR_1, "goal/goal11.png"},
    //{GoalType::UNIQUE_OUT_CARDS_3, "goal/goal12.png"},
    {GoalType::SAME_COLOR_4, "goal/goal13.png"},
    {GoalType::SAME_COLOR_5, "goal/goal14.png"},
    {GoalType::BASE_STEAL_RBI, "goal/goal15.png"}
};

const std::unordered_map<CollectionScreen::GoalType, int, CollectionScreen::GoalTypeHash> CollectionScreen::GOAL_TYPE_SCORE_MAP = {
    {GoalType::IDENTICAL_CARDS_3, 4},
    {GoalType::IDENTICAL_CARDS_4, 6},
    //{GoalType::UNIQUE_OUT_CARDS_4, 6},
    {GoalType::IDENTICAL_CARDS_5, 10},
    {GoalType::WALK_OR_HIT_3, 0},
    {GoalType::OUT_3, 3},
    {GoalType::BASES_3, 4},
    {GoalType::EACH_COLOR_2, 6},
    {GoalType::SAME_COLOR_3, 4},
    {GoalType::EACH_COLOR_1, 2},
    //{GoalType::UNIQUE_OUT_CARDS_3, 4},
    {GoalType::SAME_COLOR_4, 6},
    {GoalType::SAME_COLOR_5, 10},
    {GoalType::BASE_STEAL_RBI, 4}
};

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
    this->_visibleNode = node;
    node->setContentSize(visibleSize);
    node->setPosition(origin);
    this->addChild(node);

    // add grass to screen
    auto grass = Sprite::create("Collection-BG-Wood.jpg");
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
    auto holderHeight = holderScale * holder->getContentSize().height;
    this->_cardsHolder = holder;
    node->addChild(holder, 0);

    //add give to section button
    auto giveToSection = Sprite::create("Collection-Button-GiveSection.png");
    auto giveToSectionScale = visibleSize.width /giveToSection->getContentSize().width;
    giveToSection->setPosition(0.0f, visibleSize.height/3.0f);
    giveToSection->setAnchorPoint(Vec2(0.0f, 0.0f));
    giveToSection->setScaleX(giveToSectionScale/2);
    giveToSection->setScaleY(giveToSectionScale/2);
    auto giveToSectionHeight = giveToSectionScale * giveToSection->getContentSize().height;
    this->_giveToSection = giveToSection;
    this->_giveToSectionOrigScale = giveToSectionScale / 2.0f;
    node->addChild(giveToSection, 0);

    //add dragToScore button
    auto dragToScore = Sprite::create("Collection-Button-ScoreSet.png");
    auto dragToScoreScale = visibleSize.width /dragToScore->getContentSize().width;
    dragToScore->setPosition(visibleSize.width/2.0f, visibleSize.height/3.0f);
    dragToScore->setAnchorPoint(Vec2(0.0f, 0.0f));
    dragToScore->setScaleX(dragToScoreScale/2);
    dragToScore->setScaleY(dragToScoreScale/2);
    auto dragToScoreHeight = dragToScoreScale * dragToScore->getContentSize().height;
    this->_dragToScore = dragToScore;
    this->_dragToScoreOrigScale = dragToScoreScale / 2.0f;
    node->addChild(dragToScore, 0);

    //generate a random goal each time
    this->createGoal();

    // Create DrawNode to highlight card slot.
    this->_cardSlotDrawNode = DrawNode::create();
    this->_cardSlotDrawNode->setVisible(false);
    this->_visibleNode->addChild(this->_cardSlotDrawNode, 1);

    // Create the card slots.
    for (int i = 0; i < NUM_SLOTS; i++) {
        CardSlot slot {
            .card = Card(),
            .present = false
        };
        this->_cardSlots.push_back(slot);
    }

    // Create a score overlay.
    auto scoreLabel = Label::createWithTTF("Score", "fonts/SCOREBOARD.ttf", 216.0f);
    scoreLabel->enableShadow(Color4B::BLACK, Size(12.0f, -12.0f), 12);
    scoreLabel->setPosition(visibleSize.width / 2.0f, visibleSize.height / 2.0f);
    scoreLabel->setOpacity(0);
    scoreLabel->setVisible(false);
    this->_scoreLabel = scoreLabel;
    this->_visibleNode->addChild(scoreLabel);

    // Create event listeners.
    this->scheduleUpdate();
    return true;
}

void CollectionScreen::update(float delta) {
    if (!this->_isCardActive && !this->_incomingCardQueue.empty()) {
        auto play = this->_incomingCardQueue.front();
        this->_incomingCardQueue.pop();
        this->receiveCard(play);
    }
}

void CollectionScreen::onEnter() {
    PlaybookLayer::onEnter();
    this->initEventsGiveToSection();
    this->initEventsDragToScore();
}

void CollectionScreen::onExit() {
    this->getEventDispatcher()->removeEventListener(this->_giveToSectionListener);
    this->getEventDispatcher()->removeEventListener(this->_dragToScoreListener);
    PlaybookLayer::onExit();
}

void CollectionScreen::onResume() {
    CCLOG("CollectionScreen->onResume: Restoring state...");
    PlaybookLayer::onResume();
    this->restoreState();
    this->connectToServer();
}

void CollectionScreen::onPause() {
    CCLOG("CollectionScreen->onPause: Saving state...");
    this->saveState();
    PlaybookLayer::onPause();
    this->disconnectFromServer();
}

void CollectionScreen::restoreState() {
    auto preferences = UserDefault::getInstance();
    auto state = preferences->getStringForKey("Collection");
    if (!state.empty()) {
        CCLOG("Collection->restoreState: Restoring: %s", state.c_str());
        this->unserialize(state);
    } else {
        CCLOG("Collection->restoreState: No state to restore");
    }
}

void CollectionScreen::saveState() {
    auto preferences = UserDefault::getInstance();
    preferences->setStringForKey("Collection", this->serialize());
    preferences->flush();
}

void CollectionScreen::initEventsGiveToSection() {
    CCLOG("CollectionScreen->initEventsGiveToSection");
    auto listener = EventListenerTouchOneByOne::create();

    listener->onTouchBegan = [](Touch*, Event*) {
        return true;
    };

    listener->onTouchMoved = [this](Touch* touch, Event*) {
        // Enlarge "Give to Section" if we're hovering over it.
        auto position = this->_giveToSection->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_giveToSection->getBoundingBox();
        if (box.containsPoint(position)) {
            if (!this->_giveToSectionHovered) {
                this->_giveToSectionHovered = true;
                auto tintTo = TintTo::create(0.25f, Color3B::RED);
                this->_giveToSection->runAction(tintTo);
            }
        } else {
            if (this->_giveToSectionHovered) {
                this->_giveToSectionHovered = false;
                auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
                this->_giveToSection->runAction(tintTo);
            }
        }
    };

    listener->onTouchEnded = [this](Touch* touch, Event*) {
        this->_giveToSectionHovered = false;
        auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
        this->_giveToSection->runAction(tintTo);

        // If we were dragging a card, discard it.
        auto position = this->_giveToSection->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_giveToSection->getBoundingBox();
        if (box.containsPoint(position) && this->_isCardDragged) {
            this->discardCard(this->_draggedCard);
        }
    };

    this->getEventDispatcher()->addEventListenerWithFixedPriority(listener, -1);
    this->_giveToSectionListener = listener;
}

void CollectionScreen::initEventsDragToScore() {
    CCLOG("CollectionScreen->initEventsDragToScore");
    auto listener = EventListenerTouchOneByOne::create();

    listener->onTouchBegan = [](Touch*, Event*) {
        return true;
    };

    listener->onTouchMoved = [this](Touch* touch, Event*) {
        // Enlarge "Drag Set to Score" if we're hovering over it.
        auto position = this->_dragToScore->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_dragToScore->getBoundingBox();
        if (box.containsPoint(position)) {
            if (!this->_dragToScoreHovered) {
                this->_dragToScoreHovered = true;
                if (this->_cardsMatchingGoal.size() > 0) {
                    auto tintTo = TintTo::create(0.25f, Color3B::GREEN);
                    this->_dragToScore->runAction(tintTo);
                }
            }
        } else {
            if (this->_dragToScoreHovered) {
                this->_dragToScoreHovered = false;
                auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
                this->_dragToScore->runAction(tintTo);
            }
        }
    };

    listener->onTouchEnded = [this](Touch* touch, Event*) {
        this->_dragToScoreHovered = false;
        auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
        this->_dragToScore->runAction(tintTo);

        // If we were dragging a card, obtain its set and score.
        auto position = this->_dragToScore->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_dragToScore->getBoundingBox();
        if (box.containsPoint(position) && this->_isCardDragged && this->_cardsMatchingGoal.size() > 0) {
            this->_draggedCardDropping = true;
            this->scoreCardSet(this->_activeGoal, this->_cardsMatchingGoal);
        }
    };

    this->getEventDispatcher()->addEventListenerWithFixedPriority(listener, -1);
    this->_dragToScoreListener = listener;
}

void CollectionScreen::connectToServer() {
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

void CollectionScreen::disconnectFromServer() {
    this->_websocket->close();
}

void CollectionScreen::handleServerMessage(const std::string& event,
                                      const rapidjson::Value::ConstMemberIterator& dataIterator,
                                      bool hasData) {
    CCLOG("Handling event from server: \"%s\"", event.c_str());
    if (event == "server:playsCreated") {
        this->handlePlaysCreated(dataIterator, hasData);
    } else {
        CCLOGWARN("Unknown event \"%s\" received from server!", event.c_str());
    }
}

void CollectionScreen::handlePlaysCreated(const rapidjson::Value::ConstMemberIterator& dataIterator,
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
        if (PlaybookEvent::getTeam(event) != PlaybookEvent::Team::NONE) {
            this->_incomingCardQueue.push(event);
        }
    }
}

void CollectionScreen::reportScore(int score) {
    CCLOG("CollectionScreen->reportScore: %d", score);

    using namespace rapidjson;
    auto playerID = JniHelper::callStaticStringMethod("edu/cmu/etc/fanfare/playbook/Cocos2dxBridge", "getPlayerID");

    Document document;
    document.SetObject();
    Document::AllocatorType& allocator = document.GetAllocator();

    // Serialize the score report.
    document.AddMember(
        rapidjson::Value("cat", allocator).Move(),
        rapidjson::Value("collect", allocator).Move(),
        allocator
    );

    document.AddMember(
        rapidjson::Value("collectScore", allocator).Move(),
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

void CollectionScreen::receiveCard(PlaybookEvent::EventType event)
{
    CCLOG("CollectionScreen->receiveCard: %s", PlaybookEvent::eventToString(event).c_str());

    this->_activeCard = createCard(event);
    this->_isCardActive = true;
    auto card = this->_activeCard.sprite;

    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto cardScale = visibleSize.width / card->getContentSize().width * 0.8f;

    auto fadeIn = FadeIn::create(0.50f);
    auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, cardScale));
    auto spawn = Spawn::createWithTwoActions(fadeIn, scaleTo);
    card->runAction(spawn);

    auto listener = EventListenerTouchOneByOne::create();
    listener->onTouchBegan = [this](Touch* touch, Event*) {
        auto position = this->_activeCard.sprite->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_activeCard.sprite->getBoundingBox();
        if (box.containsPoint(position)) {
            this->startDraggingActiveCard(touch);
            this->_cardSlotDrawNode->setVisible(true);
            return true;
        } else {
            return false;
        }
    };

    listener->onTouchMoved = [this](Touch *touch, Event*) {
        auto position = this->_activeCard.sprite->getParent()->convertTouchToNodeSpace(touch);
        auto slot = this->getNearestAvailableCardSlot(this->_activeCard.sprite, position);
        this->_activeCard.sprite->setPosition(position);

        // Draw bounding box to show that card can be dropped.
        auto slotPosition = this->getCardPositionForSlot(this->_activeCard.sprite, slot);
        auto contentScaleFactor = Director::getInstance()->getContentScaleFactor();
        this->_cardSlotDrawNode->clear();
        if (slot >= 0 && slotPosition.distance(position) < 400.0f * contentScaleFactor) {
            this->drawBoundingBoxForSlot(this->_activeCard.sprite, slot);
        }

        return true;
    };

    listener->onTouchEnded = [this](Touch* touch, Event*) {
        this->stopDraggingActiveCard(touch);
        this->_cardSlotDrawNode->setVisible(false);
        return true;
    };

    card->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, card);
    this->_activeEventListener = listener;
}

CollectionScreen::Card CollectionScreen::createCard(PlaybookEvent::EventType event) {
    CCLOG("CollectionScreen->createCard: %s", PlaybookEvent::eventToString(event).c_str());
    auto team = PlaybookEvent::getTeam(event);
    if (team == PlaybookEvent::Team::NONE) {
        return Card();
    }

    std::stringstream cardFileNameSs;
    cardFileNameSs << "cards/Card-";
    switch (team) {
        case PlaybookEvent::Team::FIELDING:
            cardFileNameSs << "F-";
            break;
        case PlaybookEvent::Team::BATTING:
            cardFileNameSs << "B-";
            break;
        default:
            break;
    }

    cardFileNameSs << PlaybookEvent::eventToString(event) << ".jpg";

    auto card = Sprite::create(cardFileNameSs.str());
    auto visibleSize = Director::getInstance()->getVisibleSize();
    card->setPosition(visibleSize.width / 2.0f, visibleSize.height / 2.0f);
    card->setScale(0.0f);
    card->setRotation(RandomHelper::random_real(-5.0f, 5.0f));
    this->_visibleNode->addChild(card, 1);
    Card state {
        .team = team,
        .event = event,
        .sprite = card
    };

    return state;
}

void CollectionScreen::startDraggingActiveCard(Touch* touch) {
    CCLOG("CollectionScreen->startDraggingActiveCard");

    auto cardNode = this->_activeCard.sprite;
    if (cardNode->getNumberOfRunningActions() == 0) {
        this->_activeCardOrigPosition = cardNode->getPosition();
        this->_activeCardOrigRotation = cardNode->getRotation();
        this->_activeCardOrigScale = cardNode->getScale();
    }

    auto scale = this->getCardScaleInSlot(cardNode);
    auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);

    auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, scale));
    auto rotateTo = RotateTo::create(0.50f, 0.0f);
    auto moveTo = MoveTo::create(0.50f, position);
    auto callFunc = CallFunc::create([this]() {
        this->_isCardDragged = true;
        this->_draggedCard = this->_activeCard;
    });

    auto spawn = Spawn::create(scaleTo, rotateTo, moveTo, nullptr);
    auto sequence = Sequence::create(spawn, callFunc, nullptr);
    cardNode->runAction(sequence);
}

void CollectionScreen::stopDraggingActiveCard(cocos2d::Touch* touch) {
    CCLOG("CollectionScreen->dropDraggingActiveCard");

    // Check if touch position is within the slot.
    auto touchVisibleSpace = this->_cardsHolder->getParent()->convertTouchToNodeSpace(touch);
    auto cardsHolderBox = this->_cardsHolder->getBoundingBox();
    int slot = this->getNearestAvailableCardSlot(this->_activeCard.sprite, touchVisibleSpace);
    if (cardsHolderBox.containsPoint(touchVisibleSpace) && slot >= 0) {
        this->assignActiveCardToSlot(slot);
        this->_activeCard.sprite->getEventDispatcher()->removeEventListener(this->_activeEventListener);
    } else {
        auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, this->_activeCardOrigScale));
        auto rotateTo = RotateTo::create(0.50f, this->_activeCardOrigRotation);
        auto moveTo = MoveTo::create(0.50f, this->_activeCardOrigPosition);
        auto spawn = Spawn::create(scaleTo, rotateTo, moveTo, nullptr);
        this->_activeCard.sprite->runAction(spawn);
    }

    this->_isCardDragged = false;
}

void CollectionScreen::discardCard(const Card& card) {
    CCLOG("CollectionScreen->discardCard");

    if (this->_isCardActive) {
        if (this->_activeCard != card) {
            CCLOGWARN("A card is active, but card to be discarded is not the active card!");
            return;
        }

        this->_activeCard.sprite->removeFromParentAndCleanup(true);
        this->_isCardActive = false;

    } else {
        auto slotIterator = std::find_if(this->_cardSlots.begin(), this->_cardSlots.end(), [card](const CardSlot& slot) {
            return slot.card == card;
        });

        if (slotIterator == this->_cardSlots.end()) {
            CCLOGWARN("Attempting to discard card that doesn't exist in slot!");
            return;
        }

        slotIterator->present = false;
        slotIterator->card.sprite->removeFromParentAndCleanup(true);
    }
}

void CollectionScreen::scoreCardSet(GoalType goal, const std::vector<Card> &cardSet) {
    CCLOG("CollectionScreen->scoreCardSet");

    // Remove matching cards.
    std::for_each(this->_cardSlots.begin(), this->_cardSlots.end(), [this, cardSet](CardSlot& slot) {
        auto cardIterator = std::find_if(cardSet.begin(), cardSet.end(), [this, &slot](const Card& card) {
            return slot.present && card == slot.card;
        });

        if (cardIterator != cardSet.end()) {
            auto card = *cardIterator;
            auto dragToScorePosition = this->_dragToScore->getParent()->convertToWorldSpace(this->_dragToScore->getPosition());
            auto dragToScoreSize = this->_dragToScore->getBoundingBox().size;
            dragToScorePosition += Vec2(dragToScoreSize.width / 2.0f, dragToScoreSize.height / 2.0f);

            auto position = card.sprite->getParent()->convertToNodeSpace(dragToScorePosition);
            auto moveTo = MoveTo::create(0.25f, position);
            auto fadeOut = FadeOut::create(0.25f);
            auto spawn = Spawn::create(moveTo, fadeOut, nullptr);
            auto callFunc = CallFunc::create([card]() {
                card.sprite->removeFromParentAndCleanup(true);
            });
            auto sequence = Sequence::create(spawn, callFunc, nullptr);
            card.sprite->runAction(sequence);
            slot.present = false;
        }
    });

    // Send score to server.
    this->reportScore(GOAL_TYPE_SCORE_MAP.at(goal));

    // Display score on UI.
    this->displayScore(GOAL_TYPE_SCORE_MAP.at(goal));

    // We need to delay the execution of this so that animation completes.
    auto delayTime = DelayTime::create(0.25f);
    auto callFunc = CallFunc::create([this, goal]() {
        // Create a new goal.
        this->createGoal();
    });

    auto sequence = Sequence::create(delayTime, callFunc, nullptr);
    this->runAction(sequence);
}

void CollectionScreen::displayScore(int score) {
    CCLOG("CollectionScreen->displayScore: %d", score);

    this->_scoreLabel->setString(std::to_string(score));
    this->_scoreLabel->setOpacity(255);
    this->_scoreLabel->setVisible(true);

    auto delayTime = DelayTime::create(1.0f);
    auto fadeOut = FadeOut::create(0.5f);
    auto moveBy = MoveBy::create(0.5f, Vec2(0.0f, 60.0f));
    auto spawn = Spawn::create(fadeOut, moveBy, nullptr);

    auto callFunc = CallFunc::create([this]() {
        this->_scoreLabel->setVisible(false);
        this->_scoreLabel->setPositionY(this->_scoreLabel->getPositionY() - 60.0f);
    });
    auto sequence = Sequence::create(delayTime, spawn, callFunc, nullptr);

    this->_scoreLabel->runAction(sequence);
}

float CollectionScreen::getCardScaleInSlot(Node* card) {
    // Account for the left and right green sides (54px each).
    auto cardsHolderWidth = (this->_cardsHolder->getContentSize().width - 54.0f) * this->_cardsHolder->getScale();
    return cardsHolderWidth / card->getContentSize().width / NUM_SLOTS;
}

Vec2 CollectionScreen::getCardPositionForSlot(Node* cardNode, int slot) {
    // All target dimensions are in the visible's node space.
    auto cardScale = this->getCardScaleInSlot(cardNode);

    // Compute positions.
    auto scaledCardContentSize = cardNode->getContentSize() * cardScale;
    Vec2 position (
        this->_cardsHolder->getPosition().x + (slot + 0.5f) * scaledCardContentSize.width + (27.0f * this->_cardsHolder->getScale()),
        this->_cardsHolder->getPosition().y + scaledCardContentSize.height * 0.5f + (27.0f * this->_cardsHolder->getScale())
    );

    return position;
}

Rect CollectionScreen::getCardBoundingBoxForSlot(Node* cardNode, int slot) {
    Rect box;
    auto position = this->getCardPositionForSlot(cardNode, slot);
    box.origin = position - Vec2(
        cardNode->getContentSize().width * cardNode->getScale() / 2.0f,
        cardNode->getContentSize().height * cardNode->getScale() / 2.0f
    );
    box.size = cardNode->getContentSize() * cardNode->getScale();
    return box;
}

void CollectionScreen::drawBoundingBoxForSlot(cocos2d::Node *cardNode, int slot) {
    auto slotRect = this->getCardBoundingBoxForSlot(cardNode, slot);
    auto contentScaleFactor = Director::getInstance()->getContentScaleFactor();
    this->_cardSlotDrawNode->setLineWidth(6.0f * contentScaleFactor);
    this->_cardSlotDrawNode->drawRect(
        Vec2(slotRect.getMinX(), slotRect.getMinY()),
        Vec2(slotRect.getMaxX(), slotRect.getMaxY()),
        Color4F::ORANGE
    );
}

int CollectionScreen::getNearestAvailableCardSlot(Node *card, const Vec2 &position) {
    int slot = -1;
    float smallestDistance = FLT_MAX;

    for (int i = 0; i < NUM_SLOTS; ++i) {
        if (!this->_cardSlots[i].present) {
            auto slotPosition = this->getCardPositionForSlot(card, i);
            auto distance = slotPosition.distance(position);
            if (distance < smallestDistance) {
                slot = i;
                smallestDistance = distance;
            }
        }
    }

    return slot;
}

void CollectionScreen::assignActiveCardToSlot(int slot) {
    CCLOG("CollectionScreen->assignActiveCardToSlot");

    auto position = this->getCardPositionForSlot(this->_activeCard.sprite, slot);
    auto moveTo = MoveTo::create(0.50f, position);
    auto callFunc = CallFunc::create([this, position]() {
        // In case the card got touched again.
        auto card = this->_activeCard;
        auto cardNode = this->_activeCard.sprite;
        cardNode->setPosition(position);
        this->_isCardActive = false;

        // Create a new listener for it.
        auto listener = EventListenerTouchOneByOne::create();
        listener->setSwallowTouches(false);

        listener->onTouchBegan = [this, card, cardNode](Touch* touch, Event*) {
            // Prevent cards from being stuck if we're still moving it.
            if (card.sprite->getNumberOfRunningActions() > 0) {
                return false;
            }

            auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
            auto box = cardNode->getBoundingBox();
            if (box.containsPoint(position)) {
                this->_isCardDragged = true;
                this->_draggedCard = card;
                this->_draggedCardOrigPosition = cardNode->getPosition();
                return true;
            } else {
                return false;
            }
        };

        listener->onTouchMoved = [this, cardNode](Touch* touch, Event*) {
            auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
            cardNode->setPosition(position);
            return true;
        };

        listener->onTouchEnded = [this, cardNode](Touch* touch, Event*) {
            this->_isCardDragged = false;
            if (!this->_draggedCardDropping) {
                auto moveTo = MoveTo::create(0.25f, this->_draggedCardOrigPosition);
                cardNode->runAction(moveTo);
            } else {
                this->_draggedCardDropping = false;
            }
            return true;
        };

        cardNode->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, cardNode);
    });
    auto sequence = Sequence::create(moveTo, callFunc, nullptr);
    this->_activeCard.sprite->runAction(sequence);

    // Copy card into slot list.
    this->_cardSlots[slot].card = this->_activeCard;
    this->_cardSlots[slot].present = true;

    // Check if the set of cards meet the goal.
    this->checkIfGoalMet();
}

void CollectionScreen::checkIfGoalMet() {
    CCLOG("CollectionScreen->checkIfGoalMet");

    // Filter out slots with no cards.
    std::vector<CardSlot> slotsWithCard;
    std::copy_if(
        this->_cardSlots.begin(), this->_cardSlots.end(), std::back_inserter(slotsWithCard),
        [](const CardSlot& slot) {
            return slot.present;
        }
    );

    // Get the cards in those slots with a card.
    std::vector<Card> cardSet;
    std::transform(slotsWithCard.begin(), slotsWithCard.end(), std::back_inserter(cardSet),
        [](const CardSlot& slot) {
            return slot.card;
        }
    );

    std::vector<Card> outSet;
    if (this->cardSetMeetsGoal(cardSet, this->_activeGoal, outSet)) {
        std::for_each(outSet.begin(), outSet.end(), [](const Card& card) {
            auto tintTo = TintTo::create(0.25f, Color3B::GREEN);
            card.sprite->runAction(tintTo);
        });
    } else {
        CCLOG("Goal not met yet.");
    }
    this->_cardsMatchingGoal = outSet;
}

void CollectionScreen::createGoal() {
    CCLOG("CollectionScreen->createGoal");
    auto visibleSize = Director::getInstance()->getVisibleSize();

    // If we're replacing an old goal, remove it first.
    if (this->_goalSprite != nullptr) {
        this->_goalSprite->removeFromParentAndCleanup(true);
    }

    // Create a random goal.
    this->_activeGoal = static_cast<GoalType>(RandomHelper::random_int(0, RAND_MAX) % GoalType::UNKNOWN);
    auto goal = Sprite::create(CollectionScreen::GOAL_TYPE_FILE_MAP.at(this->_activeGoal));
    auto goalScale = visibleSize.width /goal->getContentSize().width;
    goal->setPosition(visibleSize.width/1.75f, visibleSize.height/1.4f);
    goal->setAnchorPoint(Vec2(0.0f, 0.0f));
    goal->setScaleX(goalScale/3);
    goal->setScaleY(goalScale/3);
    auto goalHeight = goalScale * goal->getContentSize().height;
    this->_visibleNode->addChild(goal, 0);
    this->_goalSprite = goal;

    // Invalidate.
    this->checkIfGoalMet();
}

bool CollectionScreen::cardSetMeetsGoal(const std::vector<Card>& cardSet, GoalType goal, std::vector<Card>& outSet) {
    CCLOG("CollectionScreen->cardSetMeetsGoal");

    auto isBase = [](const Card& card) {
        return card.event == PlaybookEvent::EventType::SINGLE ||
               card.event == PlaybookEvent::EventType::DOUBLE ||
               card.event == PlaybookEvent::EventType::TRIPLE;
    };

    auto isBlue = [](const Card& card) {
        return card.team == PlaybookEvent::Team::FIELDING;
    };

    auto isRed = [](const Card& card) {
        return card.team == PlaybookEvent::Team::BATTING;
    };

    auto numBlues = std::count_if(cardSet.begin(), cardSet.end(), isBlue);
    auto numReds = std::count_if(cardSet.begin(), cardSet.end(), isRed);

    std::unordered_map<PlaybookEvent::EventType, int, PlaybookEvent::EventTypeHash> cardCounts;
    std::for_each(cardSet.begin(), cardSet.end(), [&cardCounts](Card card) {
        if (cardCounts.find(card.event) == cardCounts.end()) {
            cardCounts[card.event] = 0;
        }
        cardCounts[card.event]++;
    });

    switch (goal) {
        case GoalType::BASE_STEAL_RBI: {
            auto isSteal = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::STEAL;
            };

            auto isRBI = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::RUN_SCORED;
            };

            auto hasBase = std::any_of(cardSet.begin(), cardSet.end(), isBase);
            auto hasSteal = std::any_of(cardSet.begin(), cardSet.end(), isSteal);
            auto hasRBI = std::any_of(cardSet.begin(), cardSet.end(), isRBI);

            // Collect matching cards.
            if (hasBase) {
                auto baseIterator = std::find_if(cardSet.begin(), cardSet.end(), isBase);
                outSet.push_back(*baseIterator);
            }

            if (hasSteal) {
                auto stealIterator = std::find_if(cardSet.begin(), cardSet.end(), isSteal);
                outSet.push_back(*stealIterator);
            }

            if (hasRBI) {
                auto RBIIterator = std::find_if(cardSet.begin(), cardSet.end(), isRBI);
                outSet.push_back(*RBIIterator);
            }

            return hasBase && hasSteal && hasRBI;
        }

        case GoalType::BASES_3: {
            auto satisfied = std::count_if(cardSet.begin(), cardSet.end(), isBase) >= 3;
            if (satisfied) {
                std::vector<Card> temp;
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isBase);
                std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::EACH_COLOR_1: {
            auto satisfied = numBlues >= 1 && numReds >= 1;
            if (satisfied) {
                auto blueIterator = std::find_if(cardSet.begin(), cardSet.end(), isBlue);
                auto redIterator = std::find_if(cardSet.begin(), cardSet.end(), isRed);
                outSet.push_back(*blueIterator);
                outSet.push_back(*redIterator);
            }
            return satisfied;
        }

        case GoalType::EACH_COLOR_2: {
            auto satisfied = numBlues >= 2 && numReds >= 2;
            if (satisfied) {
                std::vector<Card> blueTemp;
                std::vector<Card> redTemp;
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(blueTemp), isBlue);
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(redTemp), isRed);
                std::copy_n(blueTemp.begin(), 2, std::back_inserter(outSet));
                std::copy_n(redTemp.begin(), 2, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::IDENTICAL_CARDS_3: {
            for (const auto& pair : cardCounts) {
                if (pair.second >= 3) {
                    std::vector<Card> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](const Card& card) {
                       return card.event == pair.first;
                    });
                    std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
                    return true;
                }
            }
        }

        case GoalType::IDENTICAL_CARDS_4: {
            for (const auto& pair : cardCounts) {
                if (pair.second >= 4) {
                    std::vector<Card> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](const Card& card) {
                        return card.event == pair.first;
                    });
                    std::copy_n(temp.begin(), 4, std::back_inserter(outSet));
                    return true;
                }
            }
        }

        case GoalType::IDENTICAL_CARDS_5: {
            for (const auto& pair : cardCounts) {
                if (pair.second >= 5) {
                    std::vector<Card> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](const Card& card) {
                        return card.event == pair.first;
                    });
                    std::copy_n(temp.begin(), 5, std::back_inserter(outSet));
                    return true;
                }
            }
        }

        case GoalType::OUT_3: {
            auto isOut = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::STRIKEOUT ||
                    card.event == PlaybookEvent::EventType::GROUND_OUT ||
                    card.event == PlaybookEvent::EventType::FLY_OUT;
            };

            auto isDoublePlay = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::DOUBLE_PLAY;
            };

            auto isTriplePlay = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::TRIPLE_PLAY;
            };

            auto numOuts = std::count_if(cardSet.begin(), cardSet.end(), isOut);
            auto numDoublePlays = std::count_if(cardSet.begin(), cardSet.end(), isDoublePlay);
            auto numTriplePlays = std::count_if(cardSet.begin(), cardSet.end(), isTriplePlay);

            auto totalOuts = numOuts + (numDoublePlays * 2) + (numTriplePlays * 3);
            auto satisfied = totalOuts >= 3;
            if (satisfied) {
                // TODO: If players could select their own sets, that can work fine.
                if (numTriplePlays > 0) {
                    // If there's a triple play, the condition is already satisfied.
                    auto cardIterator = std::find_if(cardSet.begin(), cardSet.end(), isTriplePlay);
                    outSet.push_back(*cardIterator);
                } else if (numDoublePlays > 0) {
                    // Two cases: We can satisfy this with two double plays or a double play
                    // and a single out.
                    if (numOuts > 0 && numDoublePlays > 0) {
                        auto cardIterator = std::find_if(cardSet.begin(), cardSet.end(), isDoublePlay);
                        auto outCardIterator = std::find_if(cardSet.begin(), cardSet.end(), isOut);
                        outSet.push_back(*cardIterator);
                        outSet.push_back(*outCardIterator);
                    } else {
                        std::vector<Card> temp;
                        std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isDoublePlay);
                        std::copy_n(temp.begin(), 2, std::back_inserter(outSet));
                    }
                } else {
                    std::vector<Card> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isOut);
                    std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
                }
            }
            return satisfied;
        }

        case GoalType::SAME_COLOR_3: {
            auto satisfied = numBlues >= 3 || numReds >= 3;
            if (satisfied) {
                std::vector<Card> temp;
                if (numBlues >= 3) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isBlue);
                } else if (numReds >= 3) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isRed);
                }
                std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::SAME_COLOR_4: {
            auto satisfied = numBlues >= 4 || numReds >= 4;
            if (satisfied) {
                std::vector<Card> temp;
                if (numBlues >= 4) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isBlue);
                } else if (numReds >= 4) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isRed);
                }
                std::copy_n(temp.begin(), 4, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::SAME_COLOR_5: {
            auto satisfied = numBlues >= 5 || numReds >= 5;
            if (satisfied) {
                std::vector<Card> temp;
                if (numBlues >= 5) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isBlue);
                } else if (numReds >= 5) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isRed);
                }
                std::copy_n(temp.begin(), 5, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::WALK_OR_HIT_3: {
            auto isWalkOrHit = [](const Card& card) {
                return card.event == PlaybookEvent::EventType::WALK ||
                       card.event == PlaybookEvent::EventType::HIT;
            };

            auto satisfied = std::count_if(cardSet.begin(), cardSet.end(), isWalkOrHit) >= 3;
            if (satisfied) {
                std::vector<Card> temp;
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isWalkOrHit);
                std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
            }
            return satisfied;
        }

        // TODO: Not implemented yet
        //case GoalType::UNIQUE_OUT_CARDS_3:
        //case GoalType::UNIQUE_OUT_CARDS_4:
        default: {
            return false;
        }
    }
}

std::string CollectionScreen::serialize() {
    using namespace rapidjson;

    Document document;
    document.SetObject();
    Document::AllocatorType& allocator = document.GetAllocator();

    // Serialize the active card, if any.
    document.AddMember(
        rapidjson::Value("isCardActive", allocator).Move(),
        rapidjson::Value(this->_isCardActive).Move(),
        allocator
    );

    rapidjson::Value activeCard (kObjectType);

    activeCard.AddMember(
        rapidjson::Value("event", allocator).Move(),
        rapidjson::Value(PlaybookEvent::eventToString(this->_activeCard.event).c_str(), allocator).Move(),
        allocator
    );

    activeCard.AddMember(
        rapidjson::Value("team", allocator).Move(),
        rapidjson::Value(this->_activeCard.team).Move(),
        allocator
    );

    document.AddMember(rapidjson::Value("activeCard", allocator).Move(), activeCard, allocator);

    // Serialize the card slots.
    rapidjson::Value cardSlots (kArrayType);
    for (const auto& item : this->_cardSlots) {
        auto event = PlaybookEvent::eventToString(item.card.event);

        rapidjson::Value cardSlot (kObjectType);
        rapidjson::Value card (kObjectType);

        card.AddMember(
            rapidjson::Value("event", allocator).Move(),
            rapidjson::Value(event.c_str(), allocator).Move(),
            allocator
        );

        card.AddMember(
            rapidjson::Value("team", allocator).Move(),
            rapidjson::Value(item.card.team).Move(),
            allocator
        );

        cardSlot.AddMember(
            rapidjson::Value("card", allocator).Move(),
            card, allocator
        );

        cardSlot.AddMember(
            rapidjson::Value("present", allocator).Move(),
            rapidjson::Value(item.present).Move(),
            allocator
        );

        cardSlots.PushBack(cardSlot, allocator);
    }
    document.AddMember(rapidjson::Value("cardSlots", allocator).Move(), cardSlots, allocator);

    // Create the state.
    StringBuffer buffer;
    Writer<StringBuffer> writer(buffer);
    document.Accept(writer);

    return std::string(buffer.GetString());
}

void CollectionScreen::unserialize(const std::string& data) {
    using namespace rapidjson;

    Document document;
    document.Parse(data.c_str());
}
