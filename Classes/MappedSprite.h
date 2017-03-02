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

    std::function<void(std::string)> onTouchBegan;
    std::function<void(std::string)> onTouchEnded;

private:
    std::map<std::string, Polygon> _polygons;
    std::vector<std::string> _polygonNames;

    void initPolygons();
    void addEvents();
    std::vector<p2t::Triangle*> triangulate(const Polygon& points);
};


#endif //PLAYBOOK_MAPPEDSPRITE_H
