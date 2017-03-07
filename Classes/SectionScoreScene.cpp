#include "SectionScoreScene.h"
#include "SectionSelectionScene.h"
//#include "SimpleAudioEngine.h"
#include "extensions/cocos-ext.h"
#include <string.h>
#include "cocos/network/HttpClient.h"
#include <iostream>
#include "spine/Json.h"

USING_NS_CC;
using namespace cocos2d;

//SectionSprite::SectionSprite() {}

//SectionSprite::~SectionSprite() {}


SectionScoreSprite* SectionScoreSprite::create(std::string s)
{
	SectionScoreSprite* aSprite = new SectionScoreSprite();

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

void SectionScoreSprite::addEvents()
{
	auto listener = EventListenerTouchOneByOne::create();
	listener->setSwallowTouches(true);

	listener->onTouchBegan = [&](Touch* touch, Event* event)
	{
		//Vec2 p = touch->getLocation();
		Vec2 pNode = this->convertTouchToNodeSpace(touch);

		log("touch point x: %f", pNode.x);
		log("touch point y: %f", pNode.y);

		Vec2  size = this->getContentSize();

		//Vec2 worldPostion = this->getParent()->convertToWorldSpace(this->getPosition());

		//log("World position x: %f", worldPostion.x);
		//log("World position y: %f", worldPostion.y);

		//log("Size x: %f", size.x);
		//log("Size y: %f", size.y);

		//Rect rect(worldPostion.x - size.x / 2, worldPostion.y - size.y / 2, worldPostion.x + size.x / 2, worldPostion.y + size.y / 2);
		Rect rectNode(0, 0, size.x, size.y);

		if (rectNode.containsPoint(pNode))
		{
			log("true");
			return true;
		}

		log("false");

		return false;

	};

	listener->onTouchEnded = [=](Touch* touch, Event* event)
	{
		SectionScoreSprite::touchEvent(touch, event);
	};

	Director::getInstance()->getEventDispatcher()->addEventListenerWithFixedPriority(listener, 30);
}

void SectionScoreSprite::touchEvent(Touch* touch, Event* event)
{
	std::string touchName = this->getName();
	int touchId = this->getTag();
	log("Touched %s", touchName.c_str());
	log("Tag: %d", touchId);

	Vec2 currentSize = this->getContentSize();
	Vec2 currentPosition = this->getPosition();

	if (this->getScaleY() == 1.5) {
		if(this->getTag() != SectionSelection::selectedId)
			this->setColor(Color3B(255, 255, 255));
		this->setScaleY(1);
		this->removeChildByTag(1);
	}
	else {
		this->setScaleY(1.5);

		network::HttpRequest* request1 = new network::HttpRequest();
		std::vector<std::string> requestHeaders1;
		requestHeaders1.push_back("Content-Type: application/json");

		request1->setUrl("http://10.0.2.2:8080/highest");
		request1->setRequestType(network::HttpRequest::Type::GET);
		request1->setHeaders(requestHeaders1);

		request1->setResponseCallback(CC_CALLBACK_2(SectionScoreSprite::onHttpRequestCompleted1, this));

		//request->setRequestData(postTest.c_str(), strlen(postTest.c_str()));
		request1->setTag("get");
		network::HttpClient::getInstance()->send(request1);


		request1->release();


		network::HttpRequest* request = new network::HttpRequest();
		std::vector<std::string> requestHeaders;
		requestHeaders.push_back("Content-Type: application/json");

		request->setUrl("http://10.0.2.2:8080/score");
		request->setRequestType(network::HttpRequest::Type::POST);
		request->setHeaders(requestHeaders);

		request->setResponseCallback(CC_CALLBACK_2(SectionScoreSprite::onHttpRequestCompleted, this));

		std::stringstream postTestSS;
		postTestSS << "{" << "\"id\": " << touchId << "}";
		std::string postTest = postTestSS.str();

		log("%s", postTest.c_str());
		request->setRequestData(postTest.c_str(), strlen(postTest.c_str()));
		request->setTag("test 1");
		network::HttpClient::getInstance()->send(request);


		request->release();
		
	
	}
	
	Vec2 afterSize = this->getContentSize();
	this->setPosition(currentPosition.x, currentPosition.y + (afterSize.y - currentSize.y) / 2);
	

}

Scene* SectionScore::createScene()
{
	// 'scene' is an autorelease object
	auto scene = Scene::create();

	// 'layer' is an autorelease object
	auto layer = SectionScore::create();

	//auto layer1 = SectionSelection::create();

	// add layer as a child to scene
	scene->addChild(layer);

	//scene->addChild(layer1);

	// return the scene
	return scene;
}

void SectionScoreSprite::onHttpRequestCompleted(network::HttpClient *sender, network::HttpResponse *response) {
	if (!response)
	{
		log("response failed");
		log("error buffer: %s", response->getErrorBuffer());
		return;
	}

	//std::vector<char>* buffer = response->getResponseData();
	log("Response code: %li", response->getResponseCode());
	std::stringstream responseTestSS;
	responseTestSS << "Response buffer: " << response->getResponseData();
	std::string responseTest = responseTestSS.str();

	Json* scoreJson = Json_create(response->getResponseData()->data());
	int score = Json_getInt(scoreJson, "SecScore", -1);
	log("%d", score);

	Json_dispose(scoreJson);
	
	std::stringstream labelSS;
	labelSS << "Section " << this->getTag() << "\n" << score;
	std::string labelstr = labelSS.str();

	auto label = Label::createWithTTF(labelstr, "fonts/Marker Felt.ttf", 8);

	Vec2 worldPostion = this->getParent()->convertToWorldSpace(this->getPosition());

	log("This position x: %f", this->getPosition().x);
	log("This position y: %f", this->getPosition().y);
	log("World position x: %f", worldPostion.x);
	log("World position y: %f", worldPostion.y);
	
	// position the label on the center of the screen
	label->setPosition(Vec2(this->getContentSize().width / 2, this->getContentSize().height * 9 / 8));
	label->setTag(1);
	// add the label as a child to this layer
	this->addChild(label, 1);

	log("High Id: %d", highId);
	if ((this->getTag() != SectionSelection::selectedId) && (this->getTag() != highId))
		this->setColor(Color3B(52, 147, 255));
	else if (this->getTag() == highId)
		this->setColor(Color3B(242, 73, 73));

}

void SectionScoreSprite::onHttpRequestCompleted1(network::HttpClient *sender, network::HttpResponse *response) {
	if (!response)
	{
		log("response failed");
		log("error buffer: %s", response->getErrorBuffer());
		return;
	}

	//std::vector<char>* buffer = response->getResponseData();
	log("Response code: %li", response->getResponseCode());
	std::stringstream responseTestSS;
	responseTestSS << "Response buffer: " << response->getResponseData();
	std::string responseTest = responseTestSS.str();

	Json* scoreJson = Json_create(response->getResponseData()->data());
	int highId = Json_getInt(scoreJson, "id", -1);
	this->highId = highId;
	log("%d", highId);
	
	Json_dispose(scoreJson);

	
}


// on "init" you need to initialize your instance
bool SectionScore::init()
{
	//////////////////////////////
	// 1. super init first



	if (!Layer::init())
	{
		return false;
	}

	auto bg = cocos2d::LayerColor::create(Color4B(255, 227, 136, 255));
	this->addChild(bg, -1);

	auto visibleSize = Director::getInstance()->getVisibleSize();
	Vec2 origin = Director::getInstance()->getVisibleOrigin();

	/////////////////////////////
	// 2. add a menu item with "X" image, which is clicked to quit the program
	//    you may modify it.

	// add a "close" icon to exit the progress. it's an autorelease object
	auto closeItem = MenuItemImage::create(
		"CloseNormal.png",
		"CloseSelected.png",
		CC_CALLBACK_1(SectionScore::menuCloseCallback, this));

	closeItem->setPosition(Vec2(origin.x + visibleSize.width - closeItem->getContentSize().width / 2,
		origin.y + closeItem->getContentSize().height / 2));

	// create menu, it's an autorelease object
	auto menu = Menu::create(closeItem, NULL);
	menu->setPosition(Vec2::ZERO);
	this->addChild(menu, 1);

	/////////////////////////////
	// 3. add your codes below...

	// add a label shows "Hello World"
	// create and initialize a label


	auto stadiumMap = Sprite::create("Stadium Map.png");
	SectionScoreSprite* centerSection = SectionScoreSprite::create("Center Section.png");
	SectionScoreSprite* leftSection[7];
	SectionScoreSprite* rightSection[6];
	for (int i = 0; i < 7; i++) {
		leftSection[i] = SectionScoreSprite::create("Left Section.png");
		std::stringstream nameSS;
		nameSS << "Left Section " << (i + 2);
		leftSection[i]->setName(nameSS.str());
		leftSection[i]->setTag(i + 2);
		if (i + 2 == SectionSelection::selectedId) {
			leftSection[i]->setColor(Color3B(250, 198, 26));
		}
		leftSection[i]->setAnchorPoint(Vec2(0.5, 0.5));
		stadiumMap->addChild(leftSection[i], 7 - i);
		leftSection[i]->setPosition(stadiumMap->getContentSize().width * (220 - (220 - 30) / 7 * i) / 565, stadiumMap->getContentSize().height * (80 + (293 - 80) / 7 * i) / 475);
	}

	for (int i = 0; i < 6; i++) {
		rightSection[i] = SectionScoreSprite::create("Right Section.png");
		std::stringstream nameSS1;
		nameSS1 << "Right Section " << (i + 9);
		rightSection[i]->setName(nameSS1.str());
		rightSection[i]->setTag(i + 9);
		if (i + 9 == SectionSelection::selectedId) {
			rightSection[i]->setColor(Color3B(250, 198, 26));
		}
		rightSection[i]->setAnchorPoint(Vec2(0.5, 0.5));
		stadiumMap->addChild(rightSection[i], 6 - i);
		rightSection[i]->setPosition(stadiumMap->getContentSize().width * (347 + (544 - 347) / 6 * i) / 565, stadiumMap->getContentSize().height * (87 + (276 - 87) / 6 * i) / 475);
	}



	centerSection->setName("Center Section");
	centerSection->setTag(1);
	/*leftSection1->setName("Left Section 1");
	leftSection1->setTag(2);
	rightSection1->setName("Right Section 1");
	rightSection1->setTag(3);
	*/
	stadiumMap->setAnchorPoint(Vec2(0.5, 0.5));
	centerSection->setAnchorPoint(Vec2(0.5, 0.5));
	//leftSection1->setAnchorPoint(Vec2(0.5, 0.5));
	//rightSection1->setAnchorPoint(Vec2(0.5, 0.5));

	//stadiumMap->addChild(leftSection1);
	//stadiumMap->addChild(rightSection1);
	stadiumMap->addChild(centerSection, 7);


	stadiumMap->setPosition(Vec2(visibleSize.width / 2 + origin.x, (visibleSize.height) * 4.2 / 7 + origin.y));
	stadiumMap->setScale(1.2);

	//leftSection1->setPosition(stadiumMap->getContentSize().width * 220 / 565, stadiumMap->getContentSize().height * 80/ 475);

	//rightSection1->setPosition(stadiumMap->getContentSize().width * 380 / 565, stadiumMap->getContentSize().height * 83 / 475);

	centerSection->setPosition(stadiumMap->getContentSize().width * 280 / 565, stadiumMap->getContentSize().height * 60 / 475);
	centerSection->setScale(0.95);

	this->addChild(stadiumMap);


	auto label = Label::createWithTTF("Leaderboard", "fonts/Marker Felt.ttf", 30);

	// position the label on the center of the screen
	label->setPosition(Vec2(origin.x + visibleSize.width / 2,
		origin.y + visibleSize.height - (label->getContentSize().height)));

	// add the label as a child to this layer
	this->addChild(label, 1);

	// listen to back button
	auto listener = EventListenerKeyboard::create();
	listener->onKeyReleased = [](EventKeyboard::KeyCode keyCode, Event* event) {
		Director::getInstance()->end();
	};

	_eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

	return true;
}


void SectionScore::menuCloseCallback(Ref* pSender)
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



