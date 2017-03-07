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

int SectionSelection::selectedId = -1;


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
		/*Vec2 p = touch->getLocation();

		log("touch point x: %f", p.x);
		log("touch point y: %f", p.y);

		Vec2  size = this->getBoundingBox().size;
		
		Vec2 worldPostion = this->getParent()->convertToWorldSpace(this->getPosition());
		if (this->getTag() == 1) {
			size.x /= 2;
			size.y /= 2;
		}

		log("World position x: %f", worldPostion.x);
		log("World position y: %f", worldPostion.y);

		log("Size x: %f", size.x);
		log("Size y: %f", size.y);
		Rect rect (worldPostion.x - size.x / 2, worldPostion.y - size.y / 2, worldPostion.x + size.x / 2, worldPostion.y + size.y / 2) ;
		*/
		Vec2 p = this->convertTouchToNodeSpace(touch);

		log("touch point x: %f", p.x);
		log("touch point y: %f", p.y);

		Vec2  size = this->getContentSize();

		//Vec2 worldPostion = this->getParent()->convertToWorldSpace(this->getPosition());

		//log("World position x: %f", worldPostion.x);
		//log("World position y: %f", worldPostion.y);

		//log("Size x: %f", size.x);
		//log("Size y: %f", size.y);

		//Rect rect(worldPostion.x - size.x / 2, worldPostion.y - size.y / 2, worldPostion.x + size.x / 2, worldPostion.y + size.y / 2);
		Rect rect(0, 0, size.x, size.y);

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
	
	if (this->getScaleY() == 1.5) {
		this->setSelected(false);
		this->setColor(Color3B(255, 255, 255));
		this->setScaleY(1);
		//this->removeChildByTag(1);
	}
	else {
		this->setSelected(true);
		this->setColor(Color3B(250, 198, 26));

		Vec2 currentSize = this->getContentSize();
		Vec2 currentPosition = this->getPosition();
		this->setScaleY(1.5);
		Vec2 afterSize = this->getContentSize();
		this->setPosition(currentPosition.x, currentPosition.y + (afterSize.y - currentSize.y) / 2);


	}
	
}

void SectionSprite::setSelected(bool selected) {
	this->selected = selected;
}

bool SectionSprite::isSelected() {
	return this->selected;
}

int SectionSelection::getSelected() {
	return this->selectedId;
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

void SectionSelection::onHttpRequestCompleted(network::HttpClient *sender, network::HttpResponse *response) {
	if (!response)
	{
		log("response failed");
		log("error buffer: %s", response->getErrorBuffer());
		return;
	}

	//std::vector<char>* buffer = response->getResponseData();
	log("Response code: %li", response->getResponseCode());
	std::stringstream responseTestSS;
	responseTestSS << "Response buffer: " << response->getResponseDataString();
	std::string responseTest = responseTestSS.str();

	log("%s", responseTest.c_str());
	
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
	SectionSprite* leftSection[7];
	SectionSprite* rightSection[6];
	for (int i = 0; i < 7; i++) {
		leftSection[i] = SectionSprite::create("Left Section.png");
		std::stringstream nameSS;
		nameSS << "Left Section " << (i + 2);
		leftSection[i]->setName(nameSS.str());
		leftSection[i]->setTag(i+2);
		leftSection[i]->setAnchorPoint(Vec2(0.5, 0.5));
		stadiumMap->addChild(leftSection[i], 7-i);
		leftSection[i]->setPosition(stadiumMap->getContentSize().width * (220 - (220-30)/7*i) / 565, stadiumMap->getContentSize().height * (80 + (293-80)/7*i) / 475);
	}

	for (int i = 0; i < 6; i++) {
		rightSection[i] = SectionSprite::create("Right Section.png");
		std::stringstream nameSS1;
		nameSS1 << "Right Section " << (i + 9);
		rightSection[i]->setName(nameSS1.str());
		rightSection[i]->setTag(i + 9);
		rightSection[i]->setAnchorPoint(Vec2(0.5, 0.5));
		stadiumMap->addChild(rightSection[i], 6-i);
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
	stadiumMap->addChild(centerSection,7);
	

	stadiumMap->setPosition(Vec2(visibleSize.width / 2 + origin.x, (visibleSize.height) * 4.2 / 7 + origin.y));
	stadiumMap->setScale(1.2);

	//leftSection1->setPosition(stadiumMap->getContentSize().width * 220 / 565, stadiumMap->getContentSize().height * 80/ 475);

	//rightSection1->setPosition(stadiumMap->getContentSize().width * 380 / 565, stadiumMap->getContentSize().height * 83 / 475);

	centerSection->setPosition(stadiumMap->getContentSize().width *280 / 565, stadiumMap->getContentSize().height * 60/ 475);
	centerSection->setScale(0.95);

	this->addChild(stadiumMap);

    auto label = Label::createWithTTF("Select Your Section", "fonts/nova2.ttf", 30);
	label->setColor(Color3B::BLACK);
    // position the label on the center of the screen
    label->setPosition(Vec2(origin.x + visibleSize.width/2,
                            origin.y + visibleSize.height - (label->getContentSize().height)));

    // add the label as a child to this layer
    this->addChild(label, 1);


	auto label1 = Label::createWithTTF("Seat #", "fonts/nova1.ttf", 30);
	label1->setColor(Color3B::BLACK);
	label1->setPosition(Vec2(origin.x + visibleSize.width / 3,
		origin.y + visibleSize.height / 4));

	// add the label as a child to this layer
	this->addChild(label1, 1);

//td::string pNormalSprite = "extensions/green_edit.png";
	auto seatNoBox = ui::EditBox::create(Size(72, 24), "SeatNoBar.png");
//editName = ui::EditBox::create(editBoxSize, ui::Scale9Sprite::create(pNormalSprite));
	seatNoBox->setAnchorPoint(Vec2(0.5, 0.5));
	seatNoBox->setPosition(Vec2(origin.x + visibleSize.width *2 / 3,
		origin.y + visibleSize.height / 4));
	seatNoBox->setFontName("fonts/nova1.ttf");
	seatNoBox->setFontSize(14);
	seatNoBox->setFontColor(Color3B::BLACK);
//eatNoBox->setPlaceHolder("Seat #");
	seatNoBox->setPlaceholderFontColor(Color3B::GRAY);
	seatNoBox->setMaxLength(8);
	seatNoBox->setReturnType(ui::EditBox::KeyboardReturnType::DONE);
	seatNoBox->setScale(1.2);
//eatNoBox->setDelegate(this);
	this->addChild(seatNoBox);


	//selectedId = -1;

	if (centerSection->isSelected())
		selectedId = 1;
	else {
		for (int i = 0; i < 7; i++) {
			if (leftSection[i]->isSelected()) {
				selectedId = leftSection[i]->getTag();
				break;
			}
		}

		for (int i = 0; i < 6; i++) {
			if (rightSection[i]->isSelected()) {
				selectedId = rightSection[i]->getTag();
				break;
			}
		}
	}

	log("Selected section No: %d", selectedId);

	auto enterListener = EventListenerKeyboard::create();

	enterListener->onKeyPressed = [&](EventKeyboard::KeyCode KeyCode, Event* event) {
		log("Some key pressed");
		if (KeyCode == EventKeyboard::KeyCode::KEY_KP_ENTER) {

			log("Enter pressed");
			network::HttpRequest* request = new network::HttpRequest();
			std::vector<std::string> requestHeaders;
			requestHeaders.push_back("Content-Type: application/json");

			request->setUrl("http://10.0.2.2:8080/events");
			request->setRequestType(network::HttpRequest::Type::POST);
			request->setHeaders(requestHeaders);

			request->setResponseCallback(CC_CALLBACK_2(SectionSelection::onHttpRequestCompleted, this));

			std::stringstream postTestSS;
			postTestSS << "{"
				<< "\"id\": " << selectedId << ","
				<< "\"SeatNo\": " << seatNoBox->getText()
				<< "}";

			std::string postTest = postTestSS.str();

			log("%s", postTest.c_str());
			request->setRequestData(postTest.c_str(), strlen(postTest.c_str()));
			request->setTag("test 1");
			network::HttpClient::getInstance()->send(request);


			request->release();

		}
	};

	_eventDispatcher->addEventListenerWithSceneGraphPriority(enterListener, this);


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

    this->_eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);

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


