#include "SectionSelectionScene.h"
#include "SimpleAudioEngine.h"
#include "extensions/cocos-ext.h"
#include <string.h>
#include "cocos/network/HttpClient.h"
#include <iostream>

USING_NS_CC;
using namespace cocos2d;

//SectionSprite::SectionSprite() {}

//SectionSprite::~SectionSprite() {}


SectionSprite* SectionSprite::create(std::string s)
{
	SectionSprite* aSprite = new SectionSprite();

	if (aSprite->initWithFile(s))
	{ 
		aSprite->autorelease();
		//aSprite->initOptions();
		aSprite->addEvents();

		return aSprite;
	}

	CC_SAFE_DELETE(aSprite);
	return NULL;
	
	
}

void SectionSprite::addEvents()
{
	auto listener = EventListenerTouchOneByOne::create();
	listener->setSwallowTouches(true);

	listener->onTouchBegan = [&](Touch* touch, Event* event) 
	{
		Vec2 p = touch->getLocation();
		Vec2  size = this->getBoundingBox().size;
		Vec2 worldPostion = this->getParent()->convertToWorldSpace(this->getPosition());
		Rect rect = Rect(worldPostion.x - size.x / 2, worldPostion.y - size.y / 2, worldPostion.x + size.x / 2, worldPostion.y + size.y / 2) ;

		if (rect.containsPoint(p))
		{
			log("true");
			return true;
		}

		log("false");

		return false;
		
	};

	listener->onTouchEnded = [=](Touch* touch, Event* event)
	{
		SectionSprite::touchEvent(touch, event);
	};

	Director::getInstance()->getEventDispatcher()->addEventListenerWithFixedPriority(listener, 30);
}

void SectionSprite::touchEvent(Touch* touch, Event* event)
{
	std::string touchName = this->getName();
	int touchId = this->getTag();
	log("Touched %s", touchName.c_str());
	log("Tag: %d", touchId);
	
	Vec2 currentSize = this->getContentSize();
	Vec2 currentPosition = this->getPosition();
	this->setScaleY(1.5);
	Vec2 afterSize = this->getContentSize();
	this->setPosition(currentPosition.x, currentPosition.y + (afterSize.y - currentSize.y) / 2);

	
	network::HttpRequest* request = new network::HttpRequest();

	request->setUrl("http://10.0.2.2:8080/events");
	request->setRequestType(network::HttpRequest::Type::POST);

	request->setResponseCallback(CC_CALLBACK_2(SectionSprite::onHttpRequestCompleted, this));

	std::stringstream postTestSS;
	postTestSS << "id=" << touchId;
	std::string postTest = postTestSS.str();

	log("%s", postTest.c_str());
	request->setRequestData(postTest.c_str(), strlen(postTest.c_str()));
	request->setTag("test 1");
	network::HttpClient::getInstance()->send(request);


	request->release();
	
}

Scene* SectionSelection::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
    // 'layer' is an autorelease object
    auto layer = SectionSelection::create();

	//auto layer1 = SectionSelection::create();

    // add layer as a child to scene
    scene->addChild(layer);

	//scene->addChild(layer1);

    // return the scene
    return scene;
}

void SectionSprite::onHttpRequestCompleted(network::HttpClient *sender, network::HttpResponse *response) {
	if (!response)
	{
		log("response failed");
		log("error buffer: %s", response->getErrorBuffer());
		return;
	}

	//std::vector<char>* buffer = response->getResponseData();
	log("Response code: %li", response->getResponseCode());
	log("Response Buffer? %s", response->getResponseDataString());
	
}

// on "init" you need to initialize your instance
bool SectionSelection::init()
{
    //////////////////////////////
    // 1. super init first



    if ( !Layer::init() )
    {
        return false;
    }
    
    auto visibleSize = Director::getInstance()->getVisibleSize();
    Vec2 origin = Director::getInstance()->getVisibleOrigin();

    /////////////////////////////
    // 2. add a menu item with "X" image, which is clicked to quit the program
    //    you may modify it.

    // add a "close" icon to exit the progress. it's an autorelease object
    auto closeItem = MenuItemImage::create(
                                           "CloseNormal.png",
                                           "CloseSelected.png",
                                           CC_CALLBACK_1(SectionSelection::menuCloseCallback, this));

    closeItem->setPosition(Vec2(origin.x + visibleSize.width - closeItem->getContentSize().width/2 ,
                                origin.y + closeItem->getContentSize().height/2));

    // create menu, it's an autorelease object
    auto menu = Menu::create(closeItem, NULL);
    menu->setPosition(Vec2::ZERO);
    this->addChild(menu, 1);

    /////////////////////////////
    // 3. add your codes below...

    // add a label shows "Hello World"
    // create and initialize a label

	auto stadiumMap = Sprite::create("Stadium Map.png");
	SectionSprite* centerSection = SectionSprite::create("Center Section.png");
	SectionSprite* leftSection1 = SectionSprite::create("Left Section.png");
	SectionSprite* rightSection1 = SectionSprite::create("Right Section.png");

	centerSection->setName("Center Section");
	centerSection->setTag(1);
	leftSection1->setName("Left Section 1");
	leftSection1->setTag(2);
	rightSection1->setName("Right Section 1");
	rightSection1->setTag(3);

	stadiumMap->setAnchorPoint(Vec2(0.5, 0.5));
	centerSection->setAnchorPoint(Vec2(0.5, 0.5));
	leftSection1->setAnchorPoint(Vec2(0.5, 0.5));
	rightSection1->setAnchorPoint(Vec2(0.5, 0.5));
	
	stadiumMap->addChild(leftSection1);
	stadiumMap->addChild(rightSection1);
	stadiumMap->addChild(centerSection);
	
	stadiumMap->setPosition(Vec2(visibleSize.width / 2 + origin.x, (visibleSize.height) * 2 / 3 + origin.y));
	stadiumMap->setScale(1.2);

	leftSection1->setPosition(stadiumMap->getContentSize().width * 220 / 565, stadiumMap->getContentSize().height * 80/ 475);

	rightSection1->setPosition(stadiumMap->getContentSize().width * 380 / 565, stadiumMap->getContentSize().height * 83 / 475);

	centerSection->setPosition(stadiumMap->getContentSize().width *280 / 565, stadiumMap->getContentSize().height * 60/ 475);
	centerSection->setScale(0.95);

	this->addChild(stadiumMap);

    auto label = Label::createWithTTF("Select Your Section", "fonts/Marker Felt.ttf", 18);
    
    // position the label on the center of the screen
    label->setPosition(Vec2(origin.x + visibleSize.width/2,
                            origin.y + visibleSize.height - (label->getContentSize().height)));

    // add the label as a child to this layer
    this->addChild(label, 1);


	auto label1 = Label::createWithTTF("Seat #", "fonts/Marker Felt.ttf", 18);

	label1->setPosition(Vec2(origin.x + visibleSize.width / 3,
		origin.y + visibleSize.height / 3));

	// add the label as a child to this layer
	this->addChild(label1, 1);

//td::string pNormalSprite = "extensions/green_edit.png";
	auto seatNoBox = ui::EditBox::create(Size(30, 10), "SeatNoBar.png");
//editName = ui::EditBox::create(editBoxSize, ui::Scale9Sprite::create(pNormalSprite));
	seatNoBox->setAnchorPoint(Vec2(0.5, 0.5));
	seatNoBox->setPosition(Vec2(origin.x + visibleSize.width *2 / 3,
		origin.y + visibleSize.height / 3));
	seatNoBox->setFontName("fonts/Marker Felt.ttf");
	seatNoBox->setFontSize(14);
	seatNoBox->setFontColor(Color3B::RED);
//eatNoBox->setPlaceHolder("Seat #");
	seatNoBox->setPlaceholderFontColor(Color3B::GRAY);
	seatNoBox->setMaxLength(8);
	seatNoBox->setReturnType(ui::EditBox::KeyboardReturnType::DONE);
	seatNoBox->setScale(1.2);
//eatNoBox->setDelegate(this);
	this->addChild(seatNoBox);




    // add "HelloWorld" splash screen"
    //auto sprite = Sprite::create("HelloWorld.png");

    // position the sprite on the center of the screen
    //sprite->setPosition(Vec2(visibleSize.width/2 + origin.x, visibleSize.height/2 + origin.y));

    // add the sprite as a child to this layer
    //this->addChild(sprite, 0);

    // listen to back button
    auto listener = EventListenerKeyboard::create();
    listener->onKeyReleased = [](EventKeyboard::KeyCode keyCode, Event* event) {
        Director::getInstance()->end();
    };

    _eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

    return true;
}


void SectionSelection::menuCloseCallback(Ref* pSender)
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


