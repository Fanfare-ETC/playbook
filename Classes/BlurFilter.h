#ifndef PLAYBOOK_BLUR_FILTER_H
#define PLAYBOOK_BLUR_FILTER_H

#include "cocos2d.h"

class BlurFilter : public cocos2d::Component {
public:
    BlurFilter();

    void onEnter();
    void onExit();

    int getStrength();
    void setStrength(int strength);
    int getPasses();
    void setPasses(int passes);
    cocos2d::Texture2D* apply(cocos2d::Texture2D* texture);

    CREATE_FUNC(BlurFilter);

private:
    static const std::map<int, std::vector<float>> GAUSSIAN_VALUES;
    static const int SPRITE_PADDING;
    enum Direction { X, Y };

    cocos2d::Sprite* _sprite;
    cocos2d::Texture2D* _origTexture;
    int _strength;
    int _passes;
    cocos2d::EventListenerCustom* _resumeListener;

    int getMaxKernelSize();
    cocos2d::RenderTexture* apply(cocos2d::RenderTexture* input, int inputStrength, Direction direction);
    cocos2d::RenderTexture* applyX(cocos2d::RenderTexture* input, int inputStrength);
    cocos2d::RenderTexture* applyY(cocos2d::RenderTexture* input, int inputStrength);
    std::string generateVertBlurSource(int kernelSize, bool x);
    std::string generateFragBlurSource(int kernelSize);
};

#endif //PLAYBOOK_BLUR_FILTER_H
