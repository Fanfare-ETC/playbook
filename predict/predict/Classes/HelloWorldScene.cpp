#include "HelloWorldScene.h"
#include "SimpleAudioEngine.h"

USING_NS_CC;
using namespace std;
//struct type_rect { float  array[4][2]; };
//typedef struct type_rect type_rect;

Scene* HelloWorld::createScene()
{
    // 'scene' is an autorelease object
    auto scene = Scene::create();
    
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
	auto scale=  Director::getInstance()->getContentScaleFactor();
		

    // add grass to screen
    auto grass = Sprite::create("Prediction-BG-Grass.png");

    // position the sprite on the center of the screen
    grass->setPosition(Vec2(visibleSize.width + origin.x, visibleSize.height + origin.y));


    // add the sprite as a child to this layer
    this->addChild(grass, 0);
	
	//add overlay to screen
    auto overlay = Sprite::create("Prediction-Overlay-Field.png");

    // position the sprite on the center of the screen
	overlay->setAnchorPoint(Vec2(0, 0));
	overlay->setPosition(Vec2(0.5,0.5));
	overlay->setScaleX(visibleSize.width / overlay->getContentSize().width);
	overlay->setScaleY((visibleSize.height-50) / overlay->getContentSize().height);
	
    // add the sprite as a child to this layer
    this->addChild(overlay, 0);
	
	addtouchevent();
	
	//add banner on top to screen
	auto banner = Sprite::create("Prediction-Banner.png");
	
	// position the sprite on the center of the screen
	//banner->setAnchorPoint(Vec2(0,visibleSize.height));
	banner->setPosition(Vec2(visibleSize.width/2,visibleSize.height-20));
	banner->setScaleX(visibleSize.width / overlay->getContentSize().width);
	banner->setScaleY(visibleSize.height / overlay->getContentSize().height);
	 this->addChild(banner, 0);
	
    return true;
}
void HelloWorld::processpoint(Point p)
{

     //check for error catergory rectangle
	auto rect= this->getBoundingBox();
	//printf("rect %f %f w=%f h=%f\n",rect.origin.x,rect.origin.y,rect.size.width,rect.size.height);
	auto size_x=rect.size.width;
	auto size_y=rect.size.height;
	
	Point r[4];
	
	//error 
	r[0]=Point((16.0/1440)*size_x,(1896.0/1920)*size_y);
	r[1]=Point((16.0/1440)*size_x,(1628.0/1920)*size_y);	
	r[2]=Point((456.0/1440)*size_x,(1628.0/1920)*size_y);
	r[3]=Point((456.0/1440)*size_x,(1896.0/1920)*size_y);
	
	//printf("r %f %f %f %f %f %f %f %f\n",r[0].x,r[0].y,r[1].x,r[1].y,r[2].x,r[2].y,r[3].x,r[3].y);
	//printf("p %f %f \n",p.x,p.y);
	
	//Rect error_rect = Rect(r[3].x,r[3].y,r[3].x-r[0].x,r[3].y-r[1].y) ;
	//DrawNode *d= DrawNode::create();
	//d->drawPolygon(r,4.0,Color4F(1,1,1,1),4.0,Color4F(1,1,1,1));
	//this->addChild(d);
	if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked Error\n");
			prediction[PredictionEvents::error]=1;
        }
	
	//grandslam 
	r[0]=Point((432.0/1440)*size_x,(1910.0/1920)*size_y);
	r[1]=Point((432.0/1440)*size_x,(1700.0/1920)*size_y);	
	r[2]=Point((974.0/1440)*size_x,(1700.0/1920)*size_y);
	r[3]=Point((974.0/1440)*size_x,(1910.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked grandslam\n");
			prediction[PredictionEvents::grandslam]=1;
        }
	
	//shutout_inning 
	r[0]=Point((1042.0/1440)*size_x,(1906.0/1920)*size_y);
	r[1]=Point((1042.0/1440)*size_x,(1600.0/1920)*size_y);	
	r[2]=Point((1416.0/1440)*size_x,(1600.0/1920)*size_y);
	r[3]=Point((1416.0/1440)*size_x,(1906.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked shutout_inning\n");
			prediction[PredictionEvents::shutout_inning]=1;
        }
	//longout 
	r[0]=Point((30.0/1440)*size_x,(1616.0/1920)*size_y);
	r[1]=Point((30.0/1440)*size_x,(1138.0/1920)*size_y);	
	r[2]=Point((292.0/1440)*size_x,(1138.0/1920)*size_y);
	r[3]=Point((292.0/1440)*size_x,(1616.0/1920)*size_y);
	
	if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked Longout\n");
			prediction[PredictionEvents::longout]=1;
        }
	//runs_batted 
	r[0]=Point((280.0/1440)*size_x,(1684.0/1920)*size_y);
	r[1]=Point((280.0/1440)*size_x,(1354.0/1920)*size_y);	
	r[2]=Point((1062.0/1440)*size_x,(1354.0/1920)*size_y);
	r[3]=Point((1062.0/1440)*size_x,(1684.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked runs_batted\n");
			prediction[PredictionEvents::runs_batted]=1;
        }
	//pop_fly 
	r[0]=Point((1072.0/1440)*size_x,(1662.0/1920)*size_y);
	r[1]=Point((1072.0/1440)*size_x,(1096.0/1920)*size_y);	
	r[2]=Point((1412.0/1440)*size_x,(1096.0/1920)*size_y);
	r[3]=Point((1412.0/1440)*size_x,(1662.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked pop_fly\n");
			prediction[PredictionEvents::pop_fly]=1;
        }
	
	//triple_play 
	r[0]=Point((22.0/1440)*size_x,(1216.0/1920)*size_y);
	r[1]=Point((22.0/1440)*size_x,(876.0/1920)*size_y);	
	r[2]=Point((400.0/1440)*size_x,(876.0/1920)*size_y);
	r[3]=Point((400.0/1440)*size_x,(1216.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked triple_play\n");
			prediction[PredictionEvents::triple_play]=1;
        }
	
	//double_play
	r[0]=Point((316.0/1440)*size_x,(1404.0/1920)*size_y);
	r[1]=Point((316.0/1440)*size_x,(1100.0/1920)*size_y);	
	r[2]=Point((1000.0/1440)*size_x,(1100.0/1920)*size_y);
	r[3]=Point((1000.0/1440)*size_x,(1404.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked double_play\n");
			prediction[PredictionEvents::double_play]=1;
        }
	
	//grounder 
	r[0]=Point((1044.0/1440)*size_x,(1206.0/1920)*size_y);
	r[1]=Point((1044.0/1440)*size_x,(878.0/1920)*size_y);	
	r[2]=Point((1390.0/1440)*size_x,(878.0/1920)*size_y);
	r[3]=Point((1390.0/1440)*size_x,(1206.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked grounder\n");
			prediction[PredictionEvents::grounder]=1;
        }
	
	//steal
	r[0]=Point((468.0/1440)*size_x,(1176.0/1920)*size_y);
	r[1]=Point((468.0/1440)*size_x,(846.0/1920)*size_y);	
	r[2]=Point((970.0/1440)*size_x,(846.0/1920)*size_y);
	r[3]=Point((970.0/1440)*size_x,(1176.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked steal\n");
			prediction[PredictionEvents::steal]=1;
        }
	
	//pick_off  
	r[0]=Point((290.0/1440)*size_x,(1014.0/1920)*size_y);
	r[1]=Point((290.0/1440)*size_x,(514.0/1920)*size_y);	
	r[2]=Point((630.0/1440)*size_x,(514.0/1920)*size_y);
	r[3]=Point((630.0/1440)*size_x,(1014.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked pick_offl\n");
			prediction[PredictionEvents::pick_off]=1;
        }
	//walk 
	r[0]=Point((816.0/1440)*size_x,(1010.0/1920)*size_y);
	r[1]=Point((816.0/1440)*size_x,(512.0/1920)*size_y);	
	r[2]=Point((1102.0/1440)*size_x,(512.0/1920)*size_y);
	r[3]=Point((1102.0/1440)*size_x,(1010.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked walk\n");
			prediction[PredictionEvents::walk]=1;
        }
	//blocked_run
	r[0]=Point((486.0/1440)*size_x,(670.0/1920)*size_y);
	r[1]=Point((486.0/1440)*size_x,(374.0/1920)*size_y);	
	r[2]=Point((936.0/1440)*size_x,(374.0/1920)*size_y);
	r[3]=Point((936.0/1440)*size_x,(670.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked blocked_run\n");
			prediction[PredictionEvents::blocked_run]=1;
        }
	//strike_out
	r[0]=Point((570.0/1440)*size_x,(902.0/1920)*size_y);
	r[1]=Point((570.0/1440)*size_x,(634.0/1920)*size_y);	
	r[2]=Point((864.0/1440)*size_x,(634.0/1920)*size_y);
	r[3]=Point((864.0/1440)*size_x,(902.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked strike_out\n");
			prediction[PredictionEvents::strike_out]=1;
        }
	//hit
	r[0]=Point((26.0/1440)*size_x,(616.0/1920)*size_y);
	r[1]=Point((26.0/1440)*size_x,(190.0/1920)*size_y);	
	r[2]=Point((588.0/1440)*size_x,(190.0/1920)*size_y);
	r[3]=Point((588.0/1440)*size_x,(616.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked hit\n");
			prediction[PredictionEvents::hit]=1;
        }
	//homerun
	r[0]=Point((546.0/1440)*size_x,(338.0/1920)*size_y);
	r[1]=Point((546.0/1440)*size_x,(60.0/1920)*size_y);	
	r[2]=Point((858.0/1440)*size_x,(60.0/1920)*size_y);
	r[3]=Point((858.0/1440)*size_x,(338.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked homerun\n");
			prediction[PredictionEvents::homerun]=1;
        }
	//pitchcount_16
	r[0]=Point((958.0/1440)*size_x,(474.0/1920)*size_y);
	r[1]=Point((958.0/1440)*size_x,(114.0/1920)*size_y);	
	r[2]=Point((1298.0/1440)*size_x,(114.0/1920)*size_y);
	r[3]=Point((1298.0/1440)*size_x,(474.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked pitchcount_16\n");
			prediction[PredictionEvents::pitchcount_16]=1;
        }
	
	//walk_off 
	r[0]=Point((18.0/1440)*size_x,(336.0/1920)*size_y);
	r[1]=Point((18.0/1440)*size_x,(22.0/1920)*size_y);	
	r[2]=Point((414.0/1440)*size_x,(22.0/1920)*size_y);
	r[3]=Point((414.0/1440)*size_x,(336.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked walk_off\n");
			prediction[PredictionEvents::walk_off]=1;
        }
	
	//pitchcount_17
	r[0]=Point((1114.0/1440)*size_x,(382.0/1920)*size_y);
	r[1]=Point((1114.0/1440)*size_x,(12.0/1920)*size_y);	
	r[2]=Point((1392.0/1440)*size_x,(12.0/1920)*size_y);
	r[3]=Point((1392.0/1440)*size_x,(382.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked pitchcount_17\n");
			prediction[PredictionEvents::pitchcount_17]=1;
        }
	
	//oneb
	r[0]=Point((1124.0/1440)*size_x,(914.0/1920)*size_y);
	r[1]=Point((1124.0/1440)*size_x,(636.0/1920)*size_y);	
	r[2]=Point((1358.0/1440)*size_x,(636.0/1920)*size_y);
	r[3]=Point((1358.0/1440)*size_x,(914.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked oneb\n");
			prediction[PredictionEvents::oneb]=1;
        }
	
	//twob 
	r[0]=Point((586.0/1440)*size_x,(1420.0/1920)*size_y);
	r[1]=Point((586.0/1440)*size_x,(1172.0/1920)*size_y);	
	r[2]=Point((862.0/1440)*size_x,(1172.0/1920)*size_y);
	r[3]=Point((862.0/1440)*size_x,(1420.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked twob\n");
			prediction[PredictionEvents::twob]=1;
        }
	
	//threeb 
	r[0]=Point((86.0/1440)*size_x,(918.0/1920)*size_y);
	r[1]=Point((86.0/1440)*size_x,(636.0/1920)*size_y);	
	r[2]=Point((316.0/1440)*size_x,(636.0/1920)*size_y);
	r[3]=Point((316.0/1440)*size_x,(918.0/1920)*size_y);
		if(p.x>=r[0].x && p.x<=r[2].x && p.y<=r[0].y && p.y>=r[1].y)
        {
            printf("Clicked threeb\n");
			prediction[PredictionEvents::threeb]=1;
        }
}
void HelloWorld::addtouchevent()
{
//  Create a "one by one" touch event listener
auto listener1 = EventListenerTouchOneByOne::create();

// trigger when you push down
listener1->onTouchBegan = [this](Touch* touch, Event* event){
    // your code
	   Vec2 p = touch->getLocation();
	   processpoint(p);
       return true; // if you are consuming it
};

// trigger when moving touch
listener1->onTouchMoved = [](Touch* touch, Event* event){
    // your code
};

// trigger when you let up
listener1->onTouchEnded = [=](Touch* touch, Event* event){
    // your code
};

// Add listener
_eventDispatcher->addEventListenerWithSceneGraphPriority(listener1, this);
	
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


