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
    void clearHighlight();

    std::function<bool(std::string, Polygon polygon)> onTouchBegan;
    std::function<bool(std::string, Polygon polygon)> onTouchMoved;
    std::function<bool(std::string, Polygon polygon)> onTouchEnded;

private:
    std::map<std::string, Polygon> _polygons;
    std::vector<std::string> _polygonNames;
    cocos2d::DrawNode* _highlightNode;

    void initPolygons();
    void addEvents();
    std::vector<Polygon> triangulate(const Polygon& points);
};


#endif //PLAYBOOK_MAPPEDSPRITE_H
