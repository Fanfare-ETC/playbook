#ifndef PLAYBOOK_BLUR_FILTER_H
#define PLAYBOOK_BLUR_FILTER_H

#include "cocos2d.h"

class BlurFilter {
public:
    cocos2d::Sprite* apply(cocos2d::Sprite* sprite, int inputStrength = 8);

private:
    static const std::map<int, std::vector<float>> GAUSSIAN_VALUES;

    int getMaxKernelSize();
    cocos2d::RenderTexture* applyX(cocos2d::RenderTexture* input, int inputStrength);
    std::string generateVertBlurSource(int kernelSize, bool x);
    std::string generateFragBlurSource(int kernelSize);
};

#endif //PLAYBOOK_BLUR_FILTER_H
