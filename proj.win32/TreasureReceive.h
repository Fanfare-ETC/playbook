#pragma once
#ifndef __TREASURE_RECEIVE_SCENE_H__
#define __TREASURE_RECEIVE_SCENE_H__

#include "cocos2d.h"
#include "cocos/network/HttpClient.h"

class TreasureReceive : public cocos2d::Layer
{
public:
	static cocos2d::Scene* createScene();

	virtual bool init();

	int selectedId = 3;
	//int highId = -1;
	
	//void initOptions();

	void onHttpRequestCompleted(cocos2d::network::HttpClient *sender, cocos2d::network::HttpResponse *response);
	//void onHttpRequestCompleted1(cocos2d::network::HttpClient *sender, cocos2d::network::HttpResponse *response);

	// a selector callback
	void menuCloseCallback(cocos2d::Ref* pSender);


	// implement the "static create()" method manually
	CREATE_FUNC(TreasureReceive);
};
/*
class SectionScoreSprite : public cocos2d::Sprite
{
public:
	//SectionSprite();
	//~SectionSprite();
	int selectedId = 4;
	int highId = -1;
	static SectionScoreSprite* create(std::string s);
	//void initOptions();
	void addEvents();
	void touchEvent(cocos2d::Touch* touch, cocos2d::Event* event);

	void onHttpRequestCompleted(cocos2d::network::HttpClient *sender, cocos2d::network::HttpResponse *response);
	void onHttpRequestCompleted1(cocos2d::network::HttpClient *sender, cocos2d::network::HttpResponse *response);

private:

};
*/
#endif // __SECTIONSELECTION_SCENE_H__