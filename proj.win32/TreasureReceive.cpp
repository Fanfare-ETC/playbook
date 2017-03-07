#include "TreasureReceive.h"
#include "extensions/cocos-ext.h"
#include <string.h>
#include "cocos/network/HttpClient.h"
#include <iostream>
#include "spine/Json.h"

USING_NS_CC;
using namespace cocos2d;

Scene* TreasureReceive::createScene()
{
	// 'scene' is an autorelease object
	auto scene = Scene::create();

	// 'layer' is an autorelease object
	auto layer = TreasureReceive::create();

	//auto layer1 = SectionSelection::create();

	// add layer as a child to scene
	scene->addChild(layer);

	//scene->addChild(layer1);

	// return the scene
	return scene;
}

void TreasureReceive::onHttpRequestCompleted(network::HttpClient *sender, network::HttpResponse *response) {
	if (!response)
	{
		log("response failed");
		log("error buffer: %s", response->getErrorBuffer());
		return;
	}

	int warmer = 0;
	int colder = 0;
	bool flag = false;

	//std::vector<char>* buffer = response->getResponseData();
	log("Response code: %li", response->getResponseCode());
	std::stringstream responseTestSS;
	responseTestSS << "Response buffer: " << response->getResponseData();
	std::string responseTest = responseTestSS.str();

	Json* scoreJson = Json_create(response->getResponseData()->data());
	int length = scoreJson->size;
	log("No Children in Json: %d", length);
	Json* childPointer = scoreJson->child;
	for (int i = 0; i < length; i++) {
		int move = Json_getInt(childPointer, "Move", -1);
		if (move == 0)
			colder++;
		if (move == 1)
			warmer++;
		if (move == 0)
			flag = true;
		else
			log("Wrong move instruction");
			
		childPointer = childPointer->next;
		log("%d", move);
	}
	
	log("warmer: %d; colder: %d; flag: %d", warmer, colder, flag);

	Json_dispose(scoreJson);

	if (flag) {
		auto bg = cocos2d::LayerColor::create(Color4B(52, 247, 65, 255));
		this->addChild(bg, -1);
	}

	if (colder <= warmer) {
		auto bg = cocos2d::LayerColor::create(Color4B(247, 76, 76, 255));
		this->addChild(bg, -1);
	}
		
	else {
		auto bg = cocos2d::LayerColor::create(Color4B(52, 124, 247, 255));
		this->addChild(bg, -1);
	}

}


// on "init" you need to initialize your instance
bool TreasureReceive::init()
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
		CC_CALLBACK_1(TreasureReceive::menuCloseCallback, this));

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
	
	network::HttpRequest* request = new network::HttpRequest();
	std::vector<std::string> requestHeaders;
	requestHeaders.push_back("Content-Type: application/json");

	request->setUrl("http://10.0.2.2:8080/move");
	request->setRequestType(network::HttpRequest::Type::POST);
	request->setHeaders(requestHeaders);

	request->setResponseCallback(CC_CALLBACK_2(TreasureReceive::onHttpRequestCompleted, this));

	std::stringstream postTestSS;
	postTestSS << "{" << "\"id\": " << selectedId << "}";
	std::string postTest = postTestSS.str();

	log("%s", postTest.c_str());
	request->setRequestData(postTest.c_str(), strlen(postTest.c_str()));
	request->setTag("test 1");
	network::HttpClient::getInstance()->send(request);


	request->release();


	// listen to back button
	auto listener = EventListenerKeyboard::create();
	listener->onKeyReleased = [](EventKeyboard::KeyCode keyCode, Event* event) {
		Director::getInstance()->end();
	};

	_eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

	return true;
}


void TreasureReceive::menuCloseCallback(Ref* pSender)
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
