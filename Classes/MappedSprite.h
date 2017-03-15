#ifndef PLAYBOOK_MAPPEDSPRITE_H
#define PLAYBOOK_MAPPEDSPRITE_H

#include "cocos2d.h"
#include "poly2tri/poly2tri.h"

class MappedSprite : public cocos2d::Sprite {
public:
    typedef std::vector<cocos2d::Vec2> Polygon;

    MappedSprite();
    ~MappedSprite();
    static MappedSprite* create(std::string name, std::map<std::string, Polygon> polygons);
    static MappedSprite* createFromFile(std::string name, std::string fileName);

    void highlight(const std::string& name, const cocos2d::Color4F& fillColor, float borderWidth,
                   const cocos2d::Color4F& borderColor);
    void clearHighlight(const std::string& name);

    cocos2d::Vec2 getPolygonCenter(const std::string&);
    void addChildToPolygon(const std::string&, cocos2d::Node*);
    void getPolygonChildren(const std::string&);

    void onExit();

    std::function<void(const std::string&, Polygon, const cocos2d::Touch*)> onTouchPolygonBegan;
    std::function<void(const std::string&, Polygon, const cocos2d::Touch*)> onTouchPolygonMoved;
    std::function<void(const std::string&, Polygon, const cocos2d::Touch*)> onTouchPolygonEnded;
    std::function<void(const std::string&, Polygon, const cocos2d::Touch*)> onTouchPolygon;

private:
    cocos2d::EventListenerTouchAllAtOnce* _listener;

    std::map<std::string, Polygon> _polygons;
    std::vector<std::string> _polygonNames;
    std::map<std::string, cocos2d::Node*> _polygonNode;
    std::unordered_map<std::string, cocos2d::DrawNode*> _highlightNodes;

    std::unordered_map<int, std::string> _touchPolygonNames;

    void initPolygons();
    void addEvents();
    std::vector<Polygon> triangulate(const Polygon& points);
};


#endif //PLAYBOOK_MAPPEDSPRITE_H
