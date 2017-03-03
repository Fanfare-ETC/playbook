#include "MappedSprite.h"

using namespace cocos2d;

MappedSprite::MappedSprite() {}

MappedSprite::~MappedSprite() {}

MappedSprite* MappedSprite::create(std::string name, std::map<std::string, Polygon> polygons) {
    MappedSprite* pSprite = new MappedSprite();
    pSprite->_polygons = polygons;

    if (pSprite->initWithFile(name)) {
        pSprite->autorelease();
        pSprite->initPolygons();
        pSprite->addEvents();
        return pSprite;
    }

    CC_SAFE_DELETE(pSprite);
    return NULL;
}

MappedSprite* MappedSprite::createFromFile(std::string name, std::string fileName) {
    // TODO: This is a stub implementation
    auto fileData = FileUtils::getInstance()->getStringFromFile(fileName);
    std::map<std::string, Polygon> polygons;
    return MappedSprite::create(name, polygons);
}

void MappedSprite::initPolygons() {
    auto physicsBody = PhysicsBody::create();
    physicsBody->setGravityEnable(false);

    for (auto polygon : this->_polygons) {
        auto name = polygon.first;
        auto points = polygon.second;

        // Modify the polygon data to account for the content offset.
        std::transform(points.begin(), points.end(), points.begin(), [this](Vec2 point) {
            point.x -= this->getContentSize().width / 2.0f;
            point.y -= this->getContentSize().height / 2.0f;
            return point;
        });

        // Convert polygon to triangles so they can be used in PhysicsShape.
        // Each PhysicsShape only supports up to 4 vertices, so we have to
        // break things apart.
        std::vector<Polygon> triangles = this->triangulate(points);
        for (const auto& triangle : triangles) {
            auto physicsShape = PhysicsShapePolygon::create(triangle.data(), triangle.size());
            physicsShape->setTag(this->_polygonNames.size());
            physicsBody->addShape(physicsShape);
        }

        this->_polygonNames.push_back(name);
    }

    this->addComponent(physicsBody);
}

void MappedSprite::addEvents() {
    auto listener = EventListenerTouchOneByOne::create();

    listener->onTouchBegan = [this](Touch* touch, Event* event) {
        auto scene = Director::getInstance()->getRunningScene();
        auto shape = scene->getPhysicsWorld()->getShape(touch->getLocation());
        if (shape != nullptr) {
            if (this->onTouchBegan) {
                this->onTouchBegan(this->_polygonNames[shape->getTag()]);
            }
            return true;
        } else {
            return false;
        }
    };

    listener->onTouchEnded = [this](Touch* touch, Event* event) {
        auto scene = Director::getInstance()->getRunningScene();
        auto shape = scene->getPhysicsWorld()->getShape(touch->getLocation());
        if (shape != nullptr) {
            if (this->onTouchEnded) {
                this->onTouchEnded(this->_polygonNames[shape->getTag()]);
            }
            return true;
        } else {
            return false;
        }
    };

    _eventDispatcher->addEventListenerWithSceneGraphPriority(listener, this);
}

std::vector<MappedSprite::Polygon> MappedSprite::triangulate(const Polygon &points) {
    if (points.size() < 3) {
        CCLOG("AUTOPOLYGON: cannot triangulate polygons with less than 3 points");
        return std::vector<Polygon>();
    }

    std::vector<p2t::Point*> p2points;
    for (const auto& pt : points) {
        p2t::Point* p = new (std::nothrow) p2t::Point(pt.x, pt.y);
        p2points.push_back(p);
    }

    p2t::CDT cdt (p2points);
    cdt.Triangulate();
    std::vector<p2t::Triangle*> tris = cdt.GetTriangles();

    std::vector<Polygon> triangles;
    for (const auto& p2tTriangle : tris) {
        Polygon triangle;
        for (int i = 0; i < 3; i++) {
            auto point = p2tTriangle->GetPoint(i);
            triangle.push_back(Vec2((float) point->x, (float) point->y));
        }
        triangles.push_back(triangle);
    }

    for (auto j : p2points) {
        delete j;
    }

    return triangles;
}
