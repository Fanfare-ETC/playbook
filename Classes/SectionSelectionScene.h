#ifndef __SECTIONSELECTION_SCENE_H__
#define __SECTIONSELECTION_SCENE_H__

#include "cocos2d.h"
#include "cocos/network/HttpClient.h"

class SectionSelection : public cocos2d::Layer
{
public:
    static cocos2d::Scene* createScene();

    virtual bool init();

	static int selectedId;
	int getSelected();

    // a selector callback
    void menuCloseCallback(cocos2d::Ref* pSender);
	
	void onHttpRequestCompleted(cocos2d::network::HttpClient *sender, cocos2d::network::HttpResponse *response);
    // implement the "static create()" method manually
    CREATE_FUNC(SectionSelection);
};

class SectionSprite : public cocos2d::Sprite
{
	public:
		//SectionSprite();
		//~SectionSprite();
		bool selected = false;

		static SectionSprite* create(std::string s);
		//void initOptions();

		void setSelected(bool selected);
		bool isSelected();

		void addEvents();
		void touchEvent(cocos2d::Touch* touch, cocos2d::Event* event);

		


	private:

};
#endif // __SECTIONSELECTION_SCENE_H__
