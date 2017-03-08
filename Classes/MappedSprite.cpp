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

void MappedSprite::highlight(const std::string& name, const Color4F& fillColor,
                             float borderWidth, const Color4F& borderColor) {
    auto polygon = this->_polygons[name];

    // Create the DrawNode if it doesn't exist.
    if (this->_highlightNodes.find(name) == this->_highlightNodes.end()) {
        this->_highlightNodes[name] = DrawNode::create();
        this->addChild(this->_highlightNodes[name]);

        auto triangles = this->triangulate(polygon);
        for (const auto& triangle : triangles) {
            this->_highlightNodes[name]->drawPolygon(triangle.data(), triangle.size(), fillColor, borderWidth, borderColor);
        }
    }
}

void MappedSprite::clearHighlight(const std::string& name) {
    if (this->_highlightNodes.find(name) == this->_highlightNodes.end()) {
        CCLOG("MappedSprite->clearHighlight: Attempting to clear non-existent highlight %s", name.c_str());
        return;
    }

    this->_highlightNodes[name]->clear();
    this->removeChild(this->_highlightNodes[name], true);
    this->_highlightNodes.erase(name);
}

void MappedSprite::initPolygons() {
    auto physicsBody = PhysicsBody::create();
    physicsBody->setGravityEnable(false);

    // Pre-process the polygons to account for content scale factor.
    for (auto& polygon : this->_polygons) {
        auto& points = polygon.second;
        std::transform(points.begin(), points.end(), points.begin(), [this](Vec2 point) {
            auto contentScaleFactor = Director::getInstance()->getContentScaleFactor();
            return point / contentScaleFactor;
        });
    }

    for (const auto& polygon : this->_polygons) {
        auto name = polygon.first;
        auto points = polygon.second;

        // Create child nodes for these polygons.
        // Compute the center.
        Vec2 center = std::accumulate(points.begin(), points.end(), Vec2()) / points.size();
        auto centerNode = Node::create();
        centerNode->setPosition(center);
        this->addChild(centerNode);
        this->_polygonNode[name] = centerNode;

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
    auto listener = EventListenerTouchAllAtOnce::create();

    listener->onTouchesBegan = [this](const std::vector<Touch*>& touches, Event*) {
        if (!this->onTouchPolygonBegan) { return; }

        auto scene = Director::getInstance()->getRunningScene();
        for (const auto& touch : touches) {
            auto shape = scene->getPhysicsWorld()->getShape(touch->getLocation());

            // There are two cases for the initial touch:
            // (1) The touch started from outside a polygon
            //     In this case, we do not need to fire any polygon events.
            // (2) The touch started from inside a polygon
            //     In this case, we enter a polygon, therefore we need to fire
            //     a began event.
            if (shape != nullptr) {
                auto curr = this->_polygonNames[shape->getTag()];
                this->_touchPolygonNames[touch->getID()] = curr;
                this->onTouchPolygonBegan(curr, this->_polygons[curr], touch);
            } else {
                this->_touchPolygonNames[touch->getID()] = "";
            }
        }
    };

    listener->onTouchesMoved = [this](const std::vector<Touch*>& touches, Event*) {
        auto scene = Director::getInstance()->getRunningScene();
        for (const auto& touch : touches) {
            auto shape = scene->getPhysicsWorld()->getShape(touch->getLocation());
            auto prev = this->_touchPolygonNames[touch->getID()];

            // For movements, there are two cases:
            // (1) The new location is within a polygon
            //     If the previous polygon is empty (not inside a polygon),
            //     we fire a began event.
            //     If the current polygon differs from the previous, we just
            //     moved between polygons. Therefore, we fire a ended event,
            //     then a began event.
            //     If the current polygon is the same as the previous, then we
            //     just moved within that polygon, so we fire a moved event.
            // (2) The new location is outside a polygon
            //     If the previous location is inside a polygon, we fire an
            //     ended event, because we just exited the polygon.
            if (shape != nullptr) {
                auto curr = this->_polygonNames[shape->getTag()];
                if (prev == "") {
                    if (this->onTouchPolygonBegan) {
                        this->onTouchPolygonBegan(curr, this->_polygons[curr], touch);
                    }
                } else if (prev != curr) {
                    if (this->onTouchPolygonEnded) {
                        this->onTouchPolygonEnded(prev, this->_polygons[prev], touch);
                    }
                    if (this->onTouchPolygonBegan) {
                        this->onTouchPolygonBegan(curr, this->_polygons[curr], touch);
                    }
                } else {
                    if (this->onTouchPolygonMoved) {
                        this->onTouchPolygonMoved(curr, this->_polygons[curr], touch);
                    }
                }
                this->_touchPolygonNames[touch->getID()] = curr;
            } else {
                if (prev != "") {
                    if (this->onTouchPolygonEnded) {
                        this->onTouchPolygonEnded(prev, this->_polygons[prev], touch);
                    }
                }
                this->_touchPolygonNames[touch->getID()] = "";
            }
        }
    };

    listener->onTouchesEnded = [this](const std::vector<Touch*>& touches, Event*) {
        if (!this->onTouchPolygonEnded) { return; }

        for (const auto& touch : touches) {
            auto prev = this->_touchPolygonNames[touch->getID()];

            // If there was a previous polygon, we just ended touching it.
            // In both cases, we remove the touch tracking data.
            if (prev != "") {
                this->onTouchPolygonEnded(prev, this->_polygons[prev], touch);
            }
            this->_touchPolygonNames.erase(touch->getID());
        }
    };

    this->getEventDispatcher()->addEventListenerWithFixedPriority(listener, 1);
}

void MappedSprite::addChildToPolygon(const std::string& name, Node* node) {
    this->_polygonNode[name]->addChild(node);
}

void MappedSprite::getPolygonChildren(const std::string& name) {
    this->_polygonNode[name]->getChildren();
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
