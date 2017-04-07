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

const std::unordered_map<
    CollectionScreen::GoalType,
    CollectionScreen::GoalMetadata,
    CollectionScreen::GoalTypeHash> CollectionScreen::GOAL_TYPE_METADATA_MAP = {
    {GoalType::IDENTICAL_CARDS_3, {
        "IDENTICAL_CARDS_3",
        "3 IDENTICAL CARDS",
        "goal/goal1.png",
        8, true
    }},
    {GoalType::IDENTICAL_CARDS_4, {
        "IDENTICAL_CARDS_4",
        "4 IDENTICAL CARDS",
        "goal/goal2.png",
        12, false
    }},
    {GoalType::UNIQUE_OUT_CARDS_4, {
        "UNIQUE_OUT_CARDS_4",
        "4 DIFFERENT OUT CARDS",
        "goal/goal3.png",
        12, false
    }},
    {GoalType::IDENTICAL_CARDS_5, {
        "IDENTICAL_CARDS_5",
        "5 IDENTICAL CARDS",
        "goal/goal4.png",
        20, false
    }},
    {GoalType::WALK_OR_HIT_BY_PITCH_3, {
        "WALK_OR_HIT_BY_PITCH_3",
        "3 OF WALK OR HIT BY PITCH",
        "goal/goal5.png",
        8, true
    }},
    {GoalType::OUT_3, {
        "OUT_3",
        "SET SHOWS 3 OUTS",
        "goal/goal6.png",
        6, false
    }},
    {GoalType::BASES_RBI_3, {
        "BASES_RBI_3",
        "SET SHOWS 3 BASES",
        "goal/goal7.png",
        12, false
    }},
    {GoalType::EACH_COLOR_2, {
        "EACH_COLOR_2",
        "2 CARDS OF EACH COLOR",
        "goal/goal8.png",
        12, false
    }},
    {GoalType::SAME_COLOR_3, {
        "SAME_COLOR_3",
        "3 CARDS OF SAME COLOR",
        "goal/goal9.png",
        8, true
    }},
    {GoalType::EACH_COLOR_1, {
        "EACH_COLOR_1",
        "1 CARD OF EACH COLOR",
        "goal/goal11.png",
        4, true
    }},
    {GoalType::UNIQUE_OUT_CARDS_3, {
        "UNIQUE_OUT_CARDS_3",
        "3 DIFFERENT OUT CARDS",
        "goal/goal12.png",
        8, false
    }},
    {GoalType::SAME_COLOR_4, {
        "SAME_COLOR_4",
        "4 CARDS OF SAME COLOR",
        "goal/goal13.png",
        12, false
    }},
    {GoalType::SAME_COLOR_5, {
        "SAME_COLOR_5",
        "5 CARDS OF SAME COLOR",
        "goal/goal14.png",
        20, false
    }},
    {GoalType::BASE_STEAL_RBI, {
        "BASE_STEAL_RBI",
        "BASE, STEAL & RBI",
        "goal/goal15.png",
        8, false
    }},
    {GoalType::ON_BASE_STEAL_PICK_OFF, {
        "ON_BASE_STEAL_PICK_OFF",
        "BASE, STEAL & PICK OFF",
        "",
        8, true
    }},
    {GoalType::FULL_HOUSE, {
        "FULL_HOUSE",
        "FULL HOUSE",
        "",
        16, true
    }}
};

CollectionScreen::Card::Card(PlaybookEvent::Team team, PlaybookEvent::EventType event,
                             cocos2d::Sprite *sprite) :
    team(team),
    event(event),
    sprite(sprite) {
}

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
    Vec2 origin = Director::getInstance()->getVisibleOrigin();

    // Create Node that represents the visible portion of the screen.
    auto node = Node::create();
    this->_visibleNode = node;
    node->setContentSize(visibleSize);
    node->setPosition(origin);
    this->addChild(node);

    // add background to screen
    auto background = Sprite::create("Collection-BG-Wood.jpg");
    background->setPosition(0.0f, 0.0f);
    background->setAnchorPoint(Vec2(0.0f, 0.0f));
    background->setScaleX(visibleSize.width / background->getContentSize().width);
    background->setScaleY(visibleSize.height / background->getContentSize().height);
    node->addChild(background, 0);

    // Add card tray at the bottom.
    auto holder = Sprite::create("Collection-Tray-9x16.png");
    auto holderScale = visibleSize.width / holder->getContentSize().width;
    holder->setName(NODE_NAME_HOLDER);
    holder->setPosition(0.0f, 0.0f);
    holder->setAnchorPoint(Vec2(0.0f, 0.0f));
    holder->setScaleX(holderScale);
    holder->setScaleY(holderScale);
    this->_cardsHolder = holder;
    node->addChild(holder, 3);

    // Add score section.
    auto scoreBar = Sprite::create("Collection-Bar-Gold-9x16.png");
    auto scoreBarHeight = 96.0f;
    scoreBar->setName(NODE_NAME_SCORE_BAR);
    scoreBar->setContentSize(Size(visibleSize.width / 2.0f, scoreBarHeight));
    scoreBar->setAnchorPoint(Vec2(0.0f, 0.0f));
    scoreBar->setPosition(0.0f, holder->getContentSize().height * holderScale);

    auto scoreBarShadow = Sprite::create("Collection-Shadow-9x16.png");
    scoreBarShadow->setContentSize(Size(visibleSize.width / 2.0f, scoreBarHeight));
    scoreBarShadow->setAnchorPoint(Vec2(0.0f, 0.0f));
    scoreBar->addChild(scoreBarShadow, 0);

    auto scoreBarLabel = Label::createWithTTF("SCORE:", "fonts/nova2.ttf", 80.0f);
    scoreBarLabel->setColor(Color3B::WHITE);
    scoreBarLabel->setAnchorPoint(Vec2(0.0f, 0.5f));
    scoreBarLabel->setPosition(64.0f, scoreBarHeight / 2.0f);
    scoreBar->addChild(scoreBarLabel, 1);

    auto scoreBarScoreCard = Label::createWithTTF("000", "fonts/SCOREBOARD.ttf", 80.0f);
    scoreBarScoreCard->setName(NODE_NAME_SCORE_BAR_SCORE_CARD);
    scoreBarScoreCard->setColor(Color3B::WHITE);
    scoreBarScoreCard->setAnchorPoint(Vec2(0.0f, 0.5f));
    scoreBarScoreCard->setPosition(
        // Left margin + the "SCORE:" label + left margin.
        64.0f + scoreBarLabel->getContentSize().width + 32.0f,
        // XXX: For some reason, the scoreboard font doesn't align itself in the middle.
        (scoreBarHeight / 2.0f) - 4.0f
    );
    scoreBar->addChild(scoreBarScoreCard, 1);

    node->addChild(scoreBar, 2);

    // Add goal section.
    auto goalBar = Sprite::create("Collection-Bar-Yellow-9x16.png");
    auto goalBarHeight = scoreBarHeight;
    goalBar->setName(NODE_NAME_GOAL_BAR);
    goalBar->setContentSize(Size(visibleSize.width / 2.0f, goalBarHeight));
    goalBar->setAnchorPoint(Vec2(0.0f, 0.0f));
    goalBar->setPosition(visibleSize.width / 2.0f, holder->getContentSize().height * holderScale);

    auto goalBarShadow = Sprite::create("Collection-Shadow-9x16.png");
    goalBarShadow->setContentSize(Size(visibleSize.width / 2.0f, scoreBarHeight));
    goalBarShadow->setAnchorPoint(Vec2(0.0f, 0.0f));
    goalBar->addChild(goalBarShadow, 0);

    auto goalBarLabel = Label::createWithTTF("GOAL:", "fonts/nova2.ttf", 80.0f);
    goalBarLabel->setName(NODE_NAME_GOAL_BAR_LABEL);
    goalBarLabel->setColor(Color3B(0x80, 0x62, 0x00));
    goalBarLabel->setAnchorPoint(Vec2(0.0f, 0.5f));
    goalBarLabel->setPosition(64.0f, goalBarHeight / 2.0f);
    goalBar->addChild(goalBarLabel, 1);

    node->addChild(goalBar, 2);

    // Add hidden container for goals.
    auto goalsContainer = Node::create();
    goalsContainer->setName(NODE_NAME_GOALS_CONTAINER);
    goalsContainer->setAnchorPoint(Vec2(0.0f, 0.0f));
    goalsContainer->setPosition(
        0.0f,
        holder->getContentSize().height * holderScale +
        goalBar->getContentSize().height * goalBar->getScaleY()
    );
    node->addChild(goalsContainer, 1);

    // Small little white strip at the top of the view.
    auto whiteBanner = DrawNode::create();
    auto whiteBannerHeight = 24.0f;
    whiteBanner->setName(NODE_NAME_WHITE_BANNER);
    whiteBanner->setContentSize(Size(visibleSize.width, whiteBannerHeight));
    whiteBanner->setPosition(0.0f, visibleSize.height - whiteBannerHeight);
    whiteBanner->drawSolidRect(
        Vec2(0.0f, whiteBannerHeight),
        Vec2(visibleSize.width, 0.0f),
        Color4F::WHITE
    );
    node->addChild(whiteBanner, 0);

    // Drag plays up to discard.
    auto dragToDiscard = Sprite::create("Collection-Banner-9x16.png");
    auto dragToDiscardHeight = 96.0f;
    dragToDiscard->setName(NODE_NAME_DRAG_TO_DISCARD);
    dragToDiscard->setContentSize(Size(visibleSize.width, dragToDiscardHeight));
    dragToDiscard->setAnchorPoint(Vec2(0.0f, 0.0f));
    dragToDiscard->setPosition(0.0f, visibleSize.height - dragToDiscardHeight - whiteBannerHeight);

    auto dragToDiscardLabel = Label::createWithTTF(
        "DRAG PLAYS UP TO DISCARD",
        "fonts/nova2.ttf", 80.0f
    );
    dragToDiscardLabel->setColor(Color3B::WHITE);
    dragToDiscardLabel->setAlignment(TextHAlignment::CENTER);
    dragToDiscardLabel->setPosition(visibleSize.width / 2.0f, dragToDiscardHeight / 2.0f);
    dragToDiscard->addChild(dragToDiscardLabel, 0);

    node->addChild(dragToDiscard, 1);
    this->_dragToDiscard = dragToDiscard;

    // Draw shadows for the drag to score section.
    // For the bottom shadow, layout options are computed later.
    auto dragToScoreShadowTop = Sprite::create("Collection-Shadow-9x16.png");
    dragToScoreShadowTop->setContentSize(Size(visibleSize.width, 64.0f));
    dragToScoreShadowTop->setAnchorPoint(Vec2(0.0f, 0.0f));
    dragToScoreShadowTop->setScaleY(-1.0f);
    dragToScoreShadowTop->setPosition(dragToDiscard->getPosition());
    node->addChild(dragToScoreShadowTop, 0);

    auto dragToScoreShadowBottom = Sprite::create("Collection-Shadow-9x16.png");
    dragToScoreShadowBottom->setName(NODE_NAME_DRAG_TO_SCORE_SHADOW_BOTTOM);
    dragToScoreShadowBottom->setContentSize(Size(visibleSize.width, 64.0f));
    dragToScoreShadowBottom->setAnchorPoint(Vec2(0.0f, 0.0f));
    node->addChild(dragToScoreShadowBottom, 0);

    // Add Drag to Score button.
    // Layout options are computed later.
    auto dragToScore = Sprite::create("Collection-Star-9x16.png");
    dragToScore->setName(NODE_NAME_DRAG_TO_SCORE);
    this->_dragToScore = dragToScore;
    node->addChild(dragToScore, 1);

    // Generate a random goal.
    // createGoal calls invalidateDragToScore in the end.
    this->createGoal();

    // Create DrawNode to highlight card slot.
    this->_cardSlotDrawNode = DrawNode::create();
    this->_cardSlotDrawNode->setVisible(true);
    this->_visibleNode->addChild(this->_cardSlotDrawNode, 1);

    // Create the card slots.
    for (int i = 0; i < NUM_SLOTS; i++) {
        CardSlot slot {
            .card = std::shared_ptr<Card>(),
            .present = false
        };
        this->_cardSlots.push_back(slot);
    }

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
    this->initEventsDragToDiscard();
    this->initEventsDragToScore();
}

void CollectionScreen::onExit() {
    this->getEventDispatcher()->removeEventListener(this->_dragToDiscardListener);
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

void CollectionScreen::initEventsDragToDiscard() {
    CCLOG("CollectionScreen->initEventsDragToDiscard");
    auto listener = EventListenerTouchOneByOne::create();

    listener->onTouchBegan = [](Touch*, Event*) {
        return true;
    };

    listener->onTouchMoved = [this](Touch* touch, Event*) {
        // Look for the card that's dragged.
        auto card = this->getDraggedCard(touch);
        if (card.expired()) {
            return;
        }

        // Change "Drag to Discard" color if we're hovering over it.
        auto position = this->_dragToDiscard->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_dragToDiscard->getBoundingBox();
        if (box.containsPoint(position)) {
            if (!this->_dragToDiscardHovered) {
                this->_dragToDiscardHovered = true;
                auto tintTo = TintTo::create(0.25f, Color3B::RED);
                this->_dragToDiscard->runAction(tintTo);
            }
        } else {
            if (this->_dragToDiscardHovered) {
                this->_dragToDiscardHovered = false;
                auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
                this->_dragToDiscard->runAction(tintTo);
            }
        }
    };

    listener->onTouchEnded = [this](Touch* touch, Event*) {
        auto card = this->getDraggedCard(touch);
        if (!card.expired()) {
            this->_dragToDiscardHovered = false;
            auto tintTo = TintTo::create(0.25f, Color3B::WHITE);
            this->_dragToDiscard->runAction(tintTo);

            // If we were dragging a card, discard it.
            auto position = this->_dragToDiscard->getParent()->convertTouchToNodeSpace(touch);
            auto box = this->_dragToDiscard->getBoundingBox();
            if (box.containsPoint(position)) {
                this->discardCard(card);
            }
        }
    };

    this->getEventDispatcher()->addEventListenerWithFixedPriority(listener, -1);
    this->_dragToDiscardListener = listener;
}

void CollectionScreen::initEventsDragToScore() {
    CCLOG("CollectionScreen->initEventsDragToScore");
    auto listener = EventListenerTouchOneByOne::create();

    listener->onTouchBegan = [](Touch*, Event*) {
        return true;
    };

    listener->onTouchMoved = [this](Touch* touch, Event*) {
        // Look for the card that's dragged.
        auto card = this->getDraggedCard(touch);
        if (card.expired()) {
            return;
        }

        // Enlarge "Drag Set to Score" if we're hovering over it.
        auto position = this->_dragToScore->getParent()->convertTouchToNodeSpace(touch);
        auto box = this->_dragToScore->getBoundingBox();
        if (box.containsPoint(position)) {
            if (!this->_dragToScoreHovered) {
                this->_dragToScoreHovered = true;

                // Check if dragged card is in matching goal set.
                auto iterator = std::find_if(
                    this->_cardsMatchingGoal.begin(),
                    this->_cardsMatchingGoal.end(),
                    [this, card](std::weak_ptr<Card> cardInGoal) {
                        return cardInGoal.lock() == card.lock();
                    }
                );

                if (this->_cardsMatchingGoal.size() > 0 && iterator != this->_cardsMatchingGoal.end()) {
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

        // Check if dragged card is in matching goal set.
        auto card = this->getDraggedCard(touch);
        if (!card.expired()) {
            auto iterator = std::find_if(
                this->_cardsMatchingGoal.begin(),
                this->_cardsMatchingGoal.end(),
                [this, card](std::weak_ptr<Card> cardInGoal) {
                    return cardInGoal.lock() == card.lock();
                }
            );

            if (box.containsPoint(position) && this->_cardsMatchingGoal.size() > 0 &&
                iterator != this->_cardsMatchingGoal.end()) {
                card.lock()->draggedDropping = true;
                this->scoreCardSet(this->_activeGoal, this->_cardsMatchingGoal);
            }
        }
    };

    this->getEventDispatcher()->addEventListenerWithFixedPriority(listener, -1);
    this->_dragToScoreListener = listener;
}

void CollectionScreen::invalidateDragToScore() {
    auto goalsContainer = this->_visibleNode->getChildByName(NODE_NAME_GOALS_CONTAINER);

    // Redraw the shadow.
    auto dragToScoreShadowBottom = this->_visibleNode->getChildByName(NODE_NAME_DRAG_TO_SCORE_SHADOW_BOTTOM);
    dragToScoreShadowBottom->setPosition(
        0.0f,
        goalsContainer->getPosition().y + goalsContainer->getContentSize().height * goalsContainer->getScaleY()
    );

    // These nodes are needed to compute the height.
    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto holder = this->_visibleNode->getChildByName(NODE_NAME_HOLDER);
    auto scoreBar = this->_visibleNode->getChildByName(NODE_NAME_SCORE_BAR);
    auto whiteBanner = this->_visibleNode->getChildByName(NODE_NAME_WHITE_BANNER);
    auto dragToDiscard = this->_visibleNode->getChildByName(NODE_NAME_DRAG_TO_DISCARD);

    // Redraw the drag to score area.
    auto dragToScore = this->_visibleNode->getChildByName(NODE_NAME_DRAG_TO_SCORE);
    auto dragToScoreHeight = visibleSize.height -
                             // Margins
                             128.0f * 2 -
                             // Bottom part of screen
                             holder->getContentSize().height * holder->getScaleY() -
                             scoreBar->getContentSize().height * scoreBar->getScaleY() -
                             goalsContainer->getContentSize().height * goalsContainer->getScaleY() -
                             // Top part of screen
                             whiteBanner->getContentSize().height * whiteBanner->getScaleY() -
                             dragToDiscard->getContentSize().height * dragToDiscard->getScaleY();
    auto dragToScoreWidth = visibleSize.width - 128.0f * 2;
    auto dragToScoreScaleX = dragToScoreWidth / dragToScore->getContentSize().width;
    auto dragToScoreScaleY = dragToScoreHeight / dragToScore->getContentSize().height;
    auto dragToScoreScale = std::min(dragToScoreScaleX, dragToScoreScaleY);
    dragToScore->setPosition(
        visibleSize.width / 2.0f,
        (dragToDiscard->getPosition().y +
         goalsContainer->getPosition().y + (goalsContainer->getContentSize().height * goalsContainer->getScaleY())) / 2.0f
    );
    dragToScore->setScale(dragToScoreScale);
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
        CCLOG("CollectionScreen->handlePlaysCreated: %s",
              PlaybookEvent::eventToString(event).c_str());
        if (PlaybookEvent::getTeam(event) != PlaybookEvent::Team::NONE) {
            this->_incomingCardQueue.push(event);
        }
    }
}

void CollectionScreen::reportScore(int score) {
    CCLOG("CollectionScreen->reportScore: %d", score);

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
    CCLOG("CollectionScreen->reportScore: Sending score to: %s",
          PLAYBOOK_SECTION_API_URL "/updateScore");
    network::HttpRequest* request = new network::HttpRequest();
    request->setUrl(PLAYBOOK_SECTION_API_URL "/updateScore");
    request->setRequestType(network::HttpRequest::Type::POST);
    request->setRequestData(buffer.GetString(), buffer.GetSize());
    request->setHeaders({ "Content-Type: application/json" });
    request->setResponseCallback([](network::HttpClient*, network::HttpResponse* response) {
       if (response != nullptr) {
           // Dump the data
           CCLOG("CollectionScreen->reportScore: Response: %s", response->getResponseDataString());
       }
    });
    network::HttpClient::getInstance()->send(request);
    request->release();
}

std::weak_ptr<CollectionScreen::Card> CollectionScreen::getDraggedCard(cocos2d::Touch *touch) {
    // Look for the card that's dragged.
    auto slotIterator = std::find_if(
        this->_cardSlots.begin(), this->_cardSlots.end(),
        [touch](const CardSlot& slot) {
            if (slot.present) {
                return slot.card->isDragged &&
                    slot.card->draggedTouchID == touch->getID();
            } else {
                return false;
            }
        }
    );

    std::weak_ptr<Card> card;
    if (slotIterator != this->_cardSlots.end()) {
        card = slotIterator->card;
    } else if (this->_activeCard && touch->getID() == this->_activeCard->draggedTouchID) {
        card = this->_activeCard;
    }

    return card;
}

void CollectionScreen::receiveCard(PlaybookEvent::EventType event)
{
    CCLOG("CollectionScreen->receiveCard: %s", PlaybookEvent::eventToString(event).c_str());

    auto card = createCard(event);
    this->_activeCard = card;
    this->_isCardActive = true;
    auto cardNode = card->sprite;

    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto cardScale = visibleSize.width / cardNode->getContentSize().width * 0.8f;

    // Animate the appearance of the card.
    auto fadeIn = FadeIn::create(0.50f);
    auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, cardScale));
    auto spawn = Spawn::createWithTwoActions(fadeIn, scaleTo);
    cardNode->runAction(spawn);

    // Save these so that the animations can use them later.
    this->_activeCardOrigPosition = cardNode->getPosition();
    this->_activeCardOrigRotation = cardNode->getRotation();
    this->_activeCardOrigScale = cardScale;

    auto listener = EventListenerTouchOneByOne::create();
    listener->onTouchBegan = [this, cardNode](Touch* touch, Event*) {
        auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
        auto box = cardNode->getBoundingBox();
        if (box.containsPoint(position)) {
            this->startDraggingActiveCard(touch);
            this->_cardSlotDrawNode->setVisible(true);
            return true;
        } else {
            return false;
        }
    };

    listener->onTouchMoved = [this, cardNode](Touch *touch, Event*) {
        auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
        auto slot = this->getNearestAvailableCardSlot(cardNode, position);
        cardNode->setPosition(position);

        // Draw bounding box to show that card can be dropped.
        auto slotPosition = this->getCardPositionForSlot(cardNode, slot);
        auto contentScaleFactor = Director::getInstance()->getContentScaleFactor();
        this->_cardSlotDrawNode->clear();
        if (slot >= 0 && slotPosition.distance(position) < 400.0f * contentScaleFactor) {
            this->drawBoundingBoxForSlot(cardNode, slot);
        }

        return true;
    };

    listener->onTouchEnded = [this](Touch* touch, Event*) {
        this->stopDraggingActiveCard(touch);
        this->_cardSlotDrawNode->setVisible(false);
        return true;
    };

    cardNode->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, cardNode);
    this->_activeEventListener = listener;
}

std::shared_ptr<CollectionScreen::Card> CollectionScreen::createCard(PlaybookEvent::EventType event) {
    CCLOG("CollectionScreen->createCard: %s", PlaybookEvent::eventToString(event).c_str());
    auto team = PlaybookEvent::getTeam(event);
    if (team == PlaybookEvent::Team::NONE) {
        return nullptr;
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
    this->_visibleNode->addChild(card, 3);
    return std::make_shared<Card>(team, event, card);
}

void CollectionScreen::startDraggingActiveCard(Touch* touch) {
    CCLOG("CollectionScreen->startDraggingActiveCard");

    auto card = this->_activeCard;
    card->isDragged = true;
    card->draggedTouchID = touch->getID();

    auto cardNode = this->_activeCard->sprite;
    auto scale = this->getCardScaleInSlot(cardNode);
    auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
    auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, scale));
    auto rotateTo = RotateTo::create(0.50f, 0.0f);
    auto moveTo = MoveTo::create(0.50f, position);
    auto spawn = Spawn::create(scaleTo, rotateTo, moveTo, nullptr);
    cardNode->runAction(spawn);

    this->_activeCardAction = spawn;
}

void CollectionScreen::stopDraggingActiveCard(cocos2d::Touch* touch) {
    CCLOG("CollectionScreen->stopDraggingActiveCard");

    this->_activeCard->isDragged = false;
    auto cardNode = this->_activeCard->sprite;
    cardNode->stopAction(this->_activeCardAction);

    // Check if touch position is within the slot.
    auto touchVisibleSpace = this->_cardsHolder->getParent()->convertTouchToNodeSpace(touch);
    auto cardsHolderBox = this->_cardsHolder->getBoundingBox();
    int slot = this->getNearestAvailableCardSlot(cardNode, touchVisibleSpace);
    if (cardsHolderBox.containsPoint(touchVisibleSpace) && slot >= 0) {
        cardNode->getEventDispatcher()->removeEventListener(this->_activeEventListener);
        this->assignActiveCardToSlot(slot);
    } else {
        auto scaleTo = EaseBackOut::create(ScaleTo::create(0.50f, this->_activeCardOrigScale));
        auto rotateTo = RotateTo::create(0.50f, this->_activeCardOrigRotation);
        auto moveTo = MoveTo::create(0.50f, this->_activeCardOrigPosition);
        auto spawn = Spawn::create(scaleTo, rotateTo, moveTo, nullptr);
        cardNode->runAction(spawn);
    }
}

void CollectionScreen::discardCard(std::weak_ptr<Card> card) {
    CCLOG("CollectionScreen->discardCard");

    if (this->_isCardActive) {
        if (this->_activeCard != card.lock()) {
            CCLOGWARN("A card is active, but card to be discarded is not the active card!");
            return;
        }

        this->_activeCard->sprite->removeFromParentAndCleanup(true);
        this->_activeCard.reset();
        this->_isCardActive = false;

    } else {
        auto slotIterator = std::find_if(this->_cardSlots.begin(), this->_cardSlots.end(), [card](const CardSlot& slot) {
            return slot.card == card.lock();
        });

        if (slotIterator == this->_cardSlots.end()) {
            CCLOGWARN("Attempting to discard card that doesn't exist in slot!");
            return;
        }

        slotIterator->present = false;
        slotIterator->card->sprite->removeFromParentAndCleanup(true);
        slotIterator->card.reset();
    }

    this->checkIfGoalMet();
}

void CollectionScreen::scoreCardSet(GoalType goal, const std::vector<std::weak_ptr<Card>> &cardSet) {
    CCLOG("CollectionScreen->scoreCardSet");

    // Remove matching cards.
    std::for_each(this->_cardSlots.begin(), this->_cardSlots.end(), [this, cardSet](CardSlot& slot) {
        auto cardIterator = std::find_if(cardSet.begin(), cardSet.end(), [this, &slot](std::weak_ptr<Card> card) {
            return slot.present && card.lock() == slot.card;
        });

        if (cardIterator != cardSet.end()) {
            auto card = cardIterator->lock();
            auto dragToScorePosition = this->_dragToScore->getParent()->convertToWorldSpace(this->_dragToScore->getPosition());

            auto position = card->sprite->getParent()->convertToNodeSpace(dragToScorePosition);
            auto moveTo = MoveTo::create(0.25f, position);
            auto fadeOut = FadeOut::create(0.25f);
            auto spawn = Spawn::create(moveTo, fadeOut, nullptr);
            auto callFunc = CallFunc::create([card]() {
                card->sprite->removeFromParentAndCleanup(true);
            });
            auto sequence = Sequence::create(spawn, callFunc, nullptr);
            card->sprite->runAction(sequence);
            slot.present = false;
        }
    });

    // Send score to server.
    this->reportScore(GOAL_TYPE_METADATA_MAP.at(goal).score);

    // Update score on UI.
    this->_score += GOAL_TYPE_METADATA_MAP.at(goal).score;
    this->updateScore(this->_score);

    // We need to delay the execution of this so that animation completes.
    auto delayTime = DelayTime::create(0.25f);
    auto callFunc = CallFunc::create([this, goal]() {
        // Create a new goal.
        this->createGoal();
    });

    auto sequence = Sequence::create(delayTime, callFunc, nullptr);
    this->runAction(sequence);
}

void CollectionScreen::updateScore(int score) {
    CCLOG("CollectionScreen->updateScore: %d", score);

    auto scoreBar = this->_visibleNode->getChildByName(NODE_NAME_SCORE_BAR);
    auto scoreBarScoreCard = dynamic_cast<Label*>(scoreBar->getChildByName(NODE_NAME_SCORE_BAR_SCORE_CARD));
    CC_ASSERT(scoreBarScoreCard != nullptr);

    auto origScale = scoreBarScoreCard->getScale();
    scoreBarScoreCard->setOpacity(0);
    scoreBarScoreCard->setScale(origScale * 3.0f);

    auto scoreString = std::to_string(score);
    for (unsigned int i = 0; i < 3 - scoreString.length(); i++) {
        scoreString.insert(0, "0");
    }
    scoreBarScoreCard->setString(scoreString);

    auto scaleTo = ScaleTo::create(0.5f, origScale);
    auto fadeIn = FadeIn::create(0.5f);
    auto spawn = Spawn::create(scaleTo, fadeIn, nullptr);
    scoreBarScoreCard->runAction(spawn);
}

float CollectionScreen::getCardScaleInSlot(Node* card) {
    // Account for the left and right green sides (54px each).
    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto cardsHolderWidth = (visibleSize.width - (48.0f * 2) - ((NUM_SLOTS - 1) * 24.0f)) / 5.0f;
    return cardsHolderWidth / card->getContentSize().width;
}

Vec2 CollectionScreen::getCardPositionForSlot(Node* cardNode, int slot) {
    // All target dimensions are in the visible's node space.
    auto cardScale = this->getCardScaleInSlot(cardNode);

    // Compute positions.
    auto scaledCardContentSize = cardNode->getContentSize() * cardScale;
    Vec2 position (
        48.0f + (slot * 24.0f) + // Left margin and slot margins
            (slot + 0.5f) * scaledCardContentSize.width, // Space occupied by slot,
        48.0f + (scaledCardContentSize.height * 0.5f)
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

    // Copy card into slot list.
    this->_cardSlots[slot].card = this->_activeCard;
    this->_cardSlots[slot].present = true;
    this->_activeCard.reset();

    auto card = this->_cardSlots[slot].card;
    auto position = this->getCardPositionForSlot(card->sprite, slot);
    auto moveTo = MoveTo::create(0.50f, position);
    auto callFunc = CallFunc::create([this, card, position]() {
        // In case the card got touched again.
        auto cardNode = card->sprite;
        cardNode->setPosition(position);
        this->_isCardActive = false;

        // Create a new listener for it.
        auto listener = EventListenerTouchOneByOne::create();
        listener->setSwallowTouches(false);

        listener->onTouchBegan = [this, card, cardNode](Touch* touch, Event*) {
            // Prevent cards from being stuck if we're still moving it.
            if (cardNode->getNumberOfRunningActions() > 0) {
                return false;
            }

            auto position = cardNode->getParent()->convertTouchToNodeSpace(touch);
            auto box = cardNode->getBoundingBox();
            if (box.containsPoint(position)) {
                card->isDragged = true;
                card->draggedTouchID = touch->getID();
                card->draggedOrigPosition = cardNode->getPosition();
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

        listener->onTouchEnded = [this, card, cardNode](Touch* touch, Event*) {
            if (touch->getID() == card->draggedTouchID) {
                card->isDragged = false;
                if (!card->draggedDropping) {
                    auto moveTo = MoveTo::create(0.25f, card->draggedOrigPosition);
                    cardNode->runAction(moveTo);
                } else {
                    card->draggedDropping = false;
                }
            }
            return true;
        };

        cardNode->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, cardNode);
    });
    auto sequence = Sequence::create(moveTo, callFunc, nullptr);
    card->sprite->runAction(sequence);

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
    std::vector<std::weak_ptr<Card>> cardSet;
    std::transform(slotsWithCard.begin(), slotsWithCard.end(), std::back_inserter(cardSet),
        [](const CardSlot& slot) {
            return slot.card;
        }
    );

    std::for_each(cardSet.begin(), cardSet.end(), [](std::weak_ptr<Card> card) {
        auto tintTo = TintTo::create(0.0f, Color3B::WHITE);
        card.lock()->sprite->runAction(tintTo);
    });

    std::unordered_map<GoalType, std::vector<std::weak_ptr<Card>>, GoalTypeHash> outSets;
    for (const auto& pair : GOAL_TYPE_METADATA_MAP) {
        std::vector<std::weak_ptr<Card>> outSet;
        if (pair.second.isHidden && this->cardSetMeetsGoal(cardSet, pair.first, outSet)) {
            outSets[pair.first] = outSet;
        }
    }

    std::vector<std::weak_ptr<Card>> outSet;
    if (this->cardSetMeetsGoal(cardSet, this->_activeGoal, outSet)) {
        outSets[this->_activeGoal] = outSet;
    }

    CCLOG("CollectionScreen->checkIfGoalMet: Set meets %d goals", outSets.size());
    for (const auto& pair : outSets) {
        CCLOG("CollectionScreen->checkIfGoalMet: %s", GOAL_TYPE_METADATA_MAP.at(pair.first).name.c_str());
    }

    this->updateGoals(outSets);
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
    auto goalBar = this->_visibleNode->getChildByName(NODE_NAME_GOAL_BAR);
    auto goalBarLabel = goalBar->getChildByName(NODE_NAME_GOAL_BAR_LABEL);

    // Only use goal types that are visible.
    std::vector<GoalType> visibleGoalTypes;
    for (auto pair : GOAL_TYPE_METADATA_MAP) {
        if (!pair.second.isHidden) {
            visibleGoalTypes.push_back(pair.first);
        }
    }

    //auto randomChoice = RandomHelper::random_int(0, RAND_MAX) % visibleGoalTypes.size();
    //this->_activeGoal = visibleGoalTypes[randomChoice];
    this->_activeGoal = GoalType::EACH_COLOR_2;
    auto goal = Sprite::create(GOAL_TYPE_METADATA_MAP.at(this->_activeGoal).file);
    auto goalWidth = (visibleSize.width / 2.0f) -
        // Left and right margins and padding to the left of the goal.
        (64.0f + 48.0f + 32.0f) -
        // Space occupied by goal bar label
        goalBarLabel->getContentSize().width * goalBarLabel->getScaleX();
    auto goalScale = goalWidth / goal->getContentSize().width;
    goal->setPosition(
        64.0f + goalBarLabel->getContentSize().width + 32.0f,
        16.0f
    );
    goal->setAnchorPoint(Vec2(0.0f, 0.0f));
    goal->setScale(goalScale);
    goalBar->addChild(goal, 1);
    this->_goalSprite = goal;

    // Invalidate.
    this->checkIfGoalMet();
}

bool CollectionScreen::cardSetMeetsGoal(const std::vector<std::weak_ptr<Card>>& cardSet,
                                        GoalType goal,
                                        std::vector<std::weak_ptr<Card>>& outSet) {
    CCLOG("CollectionScreen->cardSetMeetsGoal");

    auto isOnBase = [](std::weak_ptr<Card> weakCard) {
        auto card = weakCard.lock();
        return PlaybookEvent::isOnBase(card->event);
    };

    auto isBlue = [](std::weak_ptr<Card> card) {
        return card.lock()->team == PlaybookEvent::Team::FIELDING;
    };

    auto isRed = [](std::weak_ptr<Card> card) {
        return card.lock()->team == PlaybookEvent::Team::BATTING;
    };

    auto isSteal = [](std::weak_ptr<Card> card) {
        return card.lock()->event == PlaybookEvent::EventType::STEAL;
    };

    auto numBlues = std::count_if(cardSet.begin(), cardSet.end(), isBlue);
    auto numReds = std::count_if(cardSet.begin(), cardSet.end(), isRed);

    std::unordered_map<PlaybookEvent::EventType, int, PlaybookEvent::EventTypeHash> cardCounts;
    std::for_each(cardSet.begin(), cardSet.end(), [&cardCounts](std::weak_ptr<Card> weakCard) {
        auto card = weakCard.lock();
        if (cardCounts.find(card->event) == cardCounts.end()) {
            cardCounts[card->event] = 0;
        }
        cardCounts[card->event]++;
    });

    switch (goal) {
        case GoalType::BASE_STEAL_RBI: {
            auto isRBI = [](std::weak_ptr<Card> card) {
                return card.lock()->event == PlaybookEvent::EventType::RUN_SCORED;
            };

            auto hasBase = std::any_of(cardSet.begin(), cardSet.end(), isOnBase);
            auto hasSteal = std::any_of(cardSet.begin(), cardSet.end(), isSteal);
            auto hasRBI = std::any_of(cardSet.begin(), cardSet.end(), isRBI);

            // Collect matching cards.
            if (hasBase) {
                auto baseIterator = std::find_if(cardSet.begin(), cardSet.end(), isOnBase);
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

        case GoalType::BASES_RBI_3: {
            auto satisfied = std::count_if(cardSet.begin(), cardSet.end(), isOnBase) >= 3;
            if (satisfied) {
                std::vector<std::weak_ptr<Card>> temp;
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isOnBase);
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
                std::vector<std::weak_ptr<Card>> blueTemp;
                std::vector<std::weak_ptr<Card>> redTemp;
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
                    std::vector<std::weak_ptr<Card>> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](std::weak_ptr<Card> card) {
                       return card.lock()->event == pair.first;
                    });
                    std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
                    return true;
                }
            }
            return false;
        }

        case GoalType::IDENTICAL_CARDS_4: {
            for (const auto& pair : cardCounts) {
                if (pair.second >= 4) {
                    std::vector<std::weak_ptr<Card>> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](std::weak_ptr<Card> card) {
                        return card.lock()->event == pair.first;
                    });
                    std::copy_n(temp.begin(), 4, std::back_inserter(outSet));
                    return true;
                }
            }
            return false;
        }

        case GoalType::IDENTICAL_CARDS_5: {
            for (const auto& pair : cardCounts) {
                if (pair.second >= 5) {
                    std::vector<std::weak_ptr<Card>> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), [pair](std::weak_ptr<Card> card) {
                        return card.lock()->event == pair.first;
                    });
                    std::copy_n(temp.begin(), 5, std::back_inserter(outSet));
                    return true;
                }
            }
            return false;

        }

        case GoalType::OUT_3: {
            auto isOut = [](std::weak_ptr<Card> weakCard) {
                auto card = weakCard.lock();
                return card->event == PlaybookEvent::EventType::STRIKEOUT ||
                    card->event == PlaybookEvent::EventType::GROUND_OUT ||
                    card->event == PlaybookEvent::EventType::FLY_OUT;
            };

            auto isDoublePlay = [](std::weak_ptr<Card> card) {
                return card.lock()->event == PlaybookEvent::EventType::DOUBLE_PLAY;
            };

            auto isTriplePlay = [](std::weak_ptr<Card> card) {
                return card.lock()->event == PlaybookEvent::EventType::TRIPLE_PLAY;
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
                        std::vector<std::weak_ptr<Card>> temp;
                        std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isDoublePlay);
                        std::copy_n(temp.begin(), 2, std::back_inserter(outSet));
                    }
                } else {
                    std::vector<std::weak_ptr<Card>> temp;
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isOut);
                    std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
                }
            }
            return satisfied;
        }

        case GoalType::SAME_COLOR_3: {
            auto satisfied = numBlues >= 3 || numReds >= 3;
            if (satisfied) {
                std::vector<std::weak_ptr<Card>> temp;
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
                std::vector<std::weak_ptr<Card>> temp;
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
                std::vector<std::weak_ptr<Card>> temp;
                if (numBlues >= 5) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isBlue);
                } else if (numReds >= 5) {
                    std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isRed);
                }
                std::copy_n(temp.begin(), 5, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::WALK_OR_HIT_BY_PITCH_3: {
            auto isWalkOrHit = [](std::weak_ptr<Card> weakCard) {
                auto card = weakCard.lock();
                return card->event == PlaybookEvent::EventType::WALK ||
                       card->event == PlaybookEvent::EventType::HIT_BY_PITCH;
            };

            auto satisfied = std::count_if(cardSet.begin(), cardSet.end(), isWalkOrHit) >= 3;
            if (satisfied) {
                std::vector<std::weak_ptr<Card>> temp;
                std::copy_if(cardSet.begin(), cardSet.end(), std::back_inserter(temp), isWalkOrHit);
                std::copy_n(temp.begin(), 3, std::back_inserter(outSet));
            }
            return satisfied;
        }

        case GoalType::UNIQUE_OUT_CARDS_3: {
            std::vector<std::weak_ptr<Card>> filteredCards;
            std::copy_if(
                cardSet.begin(), cardSet.end(), std::back_inserter(filteredCards),
                [](std::weak_ptr<Card> card) {
                    return PlaybookEvent::isOut(card.lock()->event);
                }
            );

            // Remove non-unique items.
            auto end = std::unique(
                filteredCards.begin(), filteredCards.end(),
                [](std::weak_ptr<Card> card1, std::weak_ptr<Card> card2) {
                    return card1.lock()->event == card2.lock()->event;
                }
            );

            // Remove items past the new end range.
            std::copy(filteredCards.begin(), end, std::back_inserter(outSet));
            if (outSet.size() == 3) {
                return true;
            } else {
                outSet.clear();
                return false;
            }
        }

        case GoalType::UNIQUE_OUT_CARDS_4: {
            std::vector<std::weak_ptr<Card>> filteredCards;
            std::copy_if(
                cardSet.begin(), cardSet.end(), std::back_inserter(filteredCards),
                [](std::weak_ptr<Card> card) {
                    return PlaybookEvent::isOut(card.lock()->event);
                }
            );

            // Remove non-unique items.
            auto end = std::unique(
                filteredCards.begin(), filteredCards.end(),
                [](std::weak_ptr<Card> card1, std::weak_ptr<Card> card2) {
                    return card1.lock()->event == card2.lock()->event;
                }
            );

            // Remove items past the new end range.
            std::copy(filteredCards.begin(), end, std::back_inserter(outSet));
            if (outSet.size() == 4) {
                return true;
            } else {
                outSet.clear();
                return false;
            }
        }

        case GoalType::ON_BASE_STEAL_PICK_OFF: {
            auto isPickOff = [](std::weak_ptr<Card> card) {
                return card.lock()->event == PlaybookEvent::EventType::PICK_OFF;
            };

            auto hasBase = std::any_of(cardSet.begin(), cardSet.end(), isOnBase);
            auto hasSteal = std::any_of(cardSet.begin(), cardSet.end(), isSteal);
            auto hasPickOff = std::any_of(cardSet.begin(), cardSet.end(), isPickOff);

            // Collect matching cards.
            if (hasBase) {
                auto baseIterator = std::find_if(cardSet.begin(), cardSet.end(), isOnBase);
                outSet.push_back(*baseIterator);
            }

            if (hasSteal) {
                auto stealIterator = std::find_if(cardSet.begin(), cardSet.end(), isSteal);
                outSet.push_back(*stealIterator);
            }

            if (hasPickOff) {
                auto pickOffIterator = std::find_if(cardSet.begin(), cardSet.end(), isPickOff);
                outSet.push_back(*pickOffIterator);
            }

            return hasBase && hasSteal && hasPickOff;
        }

        case GoalType::FULL_HOUSE: {
            // XXX: Assumption here is that we will only have 5 cards in hand.
            auto hasPair = false;
            auto hasTriple = false;

            for (const auto& pair : cardCounts) {
                if (pair.second == 2) {
                    hasPair = true;
                } else if (pair.second == 3) {
                    hasTriple = true;
                }
            }

            if (hasPair && hasTriple) {
                std::copy(cardSet.begin(), cardSet.end(), std::back_inserter(outSet));
                return true;
            } else {
                return false;
            }
        }

        default: {
            return false;
        }
    }
}

void CollectionScreen::updateGoals(
    std::unordered_map<GoalType,std::vector<std::weak_ptr<Card>>, GoalTypeHash> goalSets) {
    auto visibleSize = Director::getInstance()->getVisibleSize();
    auto goalsContainer = this->_visibleNode->getChildByName(NODE_NAME_GOALS_CONTAINER);
    auto goalsContainerHeight = 96.0f * goalSets.size();
    goalsContainer->removeAllChildrenWithCleanup(true);
    goalsContainer->setContentSize(Size(visibleSize.width, goalsContainerHeight));

    auto index = 0;
    for (const auto& pair : goalSets) {
        auto goal = pair.first;
        auto description = GOAL_TYPE_METADATA_MAP.at(goal).description;
        auto isHidden = GOAL_TYPE_METADATA_MAP.at(goal).isHidden;
        auto barSprite = isHidden ? "Collection-Bar-Green-9x16.png" : "Collection-Bar-Yellow-9x16.png";
        auto barLabelColor = isHidden ? Color3B::WHITE : Color3B(0x80, 0x62, 0x00);

        auto goalBar = Sprite::create(barSprite);
        auto goalBarHeight = 96.0f;
        goalBar->setContentSize(Size(visibleSize.width, goalBarHeight));
        goalBar->setAnchorPoint(Vec2(0.0f, 0.0f));
        goalBar->setPosition(0.0f, goalBarHeight * index);

        auto goalBarShadow = Sprite::create("Collection-Shadow-9x16.png");
        goalBarShadow->setContentSize(Size(visibleSize.width, goalBarHeight));
        goalBarShadow->setAnchorPoint(Vec2(0.0f, 0.0f));
        goalBar->addChild(goalBarShadow, 0);

        auto goalBarLabel = Label::createWithTTF(description, "fonts/nova2.ttf", 80.0f);
        goalBarLabel->setColor(barLabelColor);
        goalBarLabel->setAnchorPoint(Vec2(0.0f, 0.5f));
        goalBarLabel->setPosition(64.0f, goalBarHeight / 2.0f);
        goalBar->addChild(goalBarLabel, 1);

        auto goalBarHighlight = DrawNode::create();
        goalBarHighlight->setContentSize(Size(visibleSize.width, goalBarHeight));
        goalBarHighlight->drawSolidRect(
            Vec2(0.0f, goalBarHeight),
            Vec2(visibleSize.width, 0.0f),
            Color4F(0.0f, 0.0f, 0.0f, 0.5f)
        );
        goalBarHighlight->setVisible(false);
        goalBar->addChild(goalBarHighlight, 2);

        auto listener = EventListenerTouchOneByOne::create();

        listener->onTouchBegan = [goalBar, goalBarHighlight](Touch* touch, Event*) {
            auto position = goalBar->getParent()->convertTouchToNodeSpace(touch);
            auto box = goalBar->getBoundingBox();
            if (box.containsPoint(position)) {
                goalBarHighlight->setVisible(true);
                return true;
            }
        };

        listener->onTouchEnded = [this, goal, goalBar, goalSets, goalBarHighlight](Touch* touch, Event*) {
            auto position = goalBar->getParent()->convertTouchToNodeSpace(touch);
            auto box = goalBar->getBoundingBox();
            if (box.containsPoint(position)) {
                this->_cardsMatchingGoal = goalSets.at(goal);
                this->highlightCardsMatchingGoal();
            }
            goalBarHighlight->setVisible(false);
            return true;
        };

        goalBar->getEventDispatcher()->addEventListenerWithSceneGraphPriority(listener, goalBar);

        goalsContainer->addChild(goalBar);
        index++;
    }

    this->invalidateDragToScore();
}

void CollectionScreen::highlightCardsMatchingGoal() {
    for (const auto& slot : this->_cardSlots) {
        if (slot.present) {
            auto tintTo = TintTo::create(0.0f, Color3B::WHITE);
            slot.card->sprite->runAction(tintTo);
        }
    }

    for (const auto& card : this->_cardsMatchingGoal) {
        auto tintTo = TintTo::create(0.25f, Color3B::GREEN);
        card.lock()->sprite->runAction(tintTo);
    }
}

std::string CollectionScreen::serialize() {
    using namespace rapidjson;

    Document document;
    document.SetObject();
    Document::AllocatorType& allocator = document.GetAllocator();

    // Incoming card queue.
    rapidjson::Value incomingCardQueue (kArrayType);

    auto incomingCardQueueCopy = this->_incomingCardQueue;
    std::vector<PlaybookEvent::EventType> incomingCardVector;
    while (!incomingCardQueueCopy.empty()) {
        incomingCardVector.push_back(incomingCardQueueCopy.front());
        incomingCardQueueCopy.pop();
    }

    for (const auto& item : incomingCardVector) {
        incomingCardQueue.PushBack(
            rapidjson::Value(PlaybookEvent::eventToString(item).c_str(), allocator).Move(),
            allocator
        );
    }

    document.AddMember(
        rapidjson::Value("incomingCardQueue", allocator).Move(),
        incomingCardQueue, allocator
    );

    // Card slots (_cardSlots).
    rapidjson::Value cardSlots (kArrayType);
    for (const auto& item : this->_cardSlots) {
        rapidjson::Value cardSlot (kObjectType);
        rapidjson::Value card (kObjectType);

        if (item.card) {
            auto event = PlaybookEvent::eventToString(item.card->event);
            card.AddMember(
                rapidjson::Value("event", allocator).Move(),
                rapidjson::Value(event.c_str(), allocator).Move(),
                allocator
            );

            card.AddMember(
                rapidjson::Value("team", allocator).Move(),
                rapidjson::Value(item.card->team).Move(),
                allocator
            );
        } else {
            card.SetNull();
        }

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

    // The active goal (_activeGoal).
    document.AddMember(
        rapidjson::Value("activeGoal", allocator).Move(),
        rapidjson::Value(this->_activeGoal).Move(),
        allocator
    );

    // The active state (_isCardActive).
    document.AddMember(
        rapidjson::Value("isCardActive", allocator).Move(),
        rapidjson::Value(this->_isCardActive).Move(),
        allocator
    );

    // The active card (_activeCard).
    rapidjson::Value activeCard (kObjectType);
    if (this->_isCardActive) {
        activeCard.AddMember(
            rapidjson::Value("event", allocator).Move(),
            rapidjson::Value(PlaybookEvent::eventToString(this->_activeCard->event).c_str(), allocator).Move(),
            allocator
        );

        activeCard.AddMember(
            rapidjson::Value("team", allocator).Move(),
            rapidjson::Value(this->_activeCard->team).Move(),
            allocator
        );
    } else {
        activeCard.SetNull();
    }
    document.AddMember(rapidjson::Value("activeCard", allocator).Move(), activeCard, allocator);

    // The active card scale (_activeCardOrigScale).
    document.AddMember(
        rapidjson::Value("activeCardOrigScale", allocator).Move(),
        rapidjson::Value(this->_activeCardOrigScale).Move(),
        allocator
    );

    // The active card position (_activeCardOrigPosition).
    rapidjson::Value activeCardOrigPosition (kObjectType);

    activeCardOrigPosition.AddMember(
        rapidjson::Value("x", allocator).Move(),
        rapidjson::Value(this->_activeCardOrigPosition.x).Move(),
        allocator
    );

    activeCardOrigPosition.AddMember(
        rapidjson::Value("y", allocator).Move(),
        rapidjson::Value(this->_activeCardOrigPosition.y).Move(),
        allocator
    );

    document.AddMember(
        rapidjson::Value("activeCardOrigPosition", allocator).Move(),
        activeCardOrigPosition, allocator
    );

    // The active card rotation (_activeCardOrigRotation).
    document.AddMember(
        rapidjson::Value("activeCardOrigRotation", allocator).Move(),
        rapidjson::Value(this->_activeCardOrigRotation).Move(),
        allocator
    );

    // The score.
    document.AddMember(
        rapidjson::Value("score", allocator).Move(),
        rapidjson::Value(this->_score).Move(),
        allocator
    );

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
