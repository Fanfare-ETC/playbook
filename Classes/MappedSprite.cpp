#include "MappedSprite.h"
#include "spine/Json.h"

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

        auto physicsShape = PhysicsShapePolygon::create(points.data(), points.size());
        physicsShape->setTag(this->_polygonNames.size());
        this->_polygonNames.push_back(name);

        physicsBody->addShape(physicsShape);
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
