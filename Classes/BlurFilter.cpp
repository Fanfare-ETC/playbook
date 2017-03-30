#include <regex>
#include "BlurFilter.h"

using namespace cocos2d;

const std::map<int, std::vector<float>> BlurFilter::GAUSSIAN_VALUES = {
    {5, {0.153388, 0.221461, 0.250301}},
    {7, {0.071303, 0.131514, 0.189879, 0.214607}},
    {9, {0.028532, 0.067234, 0.124009, 0.179044, 0.20236}},
    {11, {0.0093, 0.028002, 0.065984, 0.121703, 0.175713, 0.198596}},
    {13, {0.002406, 0.009255, 0.027867, 0.065666, 0.121117, 0.174868, 0.197641}},
    {15, {0.000489, 0.002403, 0.009246, 0.02784, 0.065602, 0.120999, 0.174697, 0.197448}}
};

const int BlurFilter::SPRITE_PADDING = 12;

Sprite* BlurFilter::apply(cocos2d::Sprite *sprite, int inputStrength) {
    auto clone = Sprite::createWithTexture(sprite->getTexture());
    clone->setPosition(SPRITE_PADDING, SPRITE_PADDING);
    clone->setScale(1.0f);
    clone->setAnchorPoint(Vec2(0.0f, 0.0f));

    auto texture = clone->getTexture();
    auto renderTexture = RenderTexture::create(
        texture->getPixelsWide() + (2 * SPRITE_PADDING),
        texture->getPixelsHigh() + (2 * SPRITE_PADDING),
        texture->getPixelFormat()
    );

    renderTexture->begin();
    clone->visit();
    renderTexture->end();

    renderTexture = this->applyX(renderTexture, inputStrength);
    renderTexture = this->applyY(renderTexture, inputStrength);

    return renderTexture->getSprite();
}

RenderTexture* BlurFilter::apply(cocos2d::RenderTexture *input, int inputStrength, Direction direction) {
    auto kernelSize = this->getMaxKernelSize();
    auto isX = direction == Direction::X;
    auto shaderProgram = GLProgram::createWithByteArrays(
        this->generateVertBlurSource(kernelSize, isX).c_str(),
        this->generateFragBlurSource(kernelSize).c_str()
    );

    shaderProgram->link();
    shaderProgram->updateUniforms();
    shaderProgram->bindAttribLocation(GLProgram::ATTRIBUTE_NAME_POSITION, GLProgram::VERTEX_ATTRIB_POSITION);
    shaderProgram->bindAttribLocation(GLProgram::ATTRIBUTE_NAME_COLOR, GLProgram::VERTEX_ATTRIB_COLOR);
    shaderProgram->bindAttribLocation(GLProgram::ATTRIBUTE_NAME_TEX_COORD, GLProgram::VERTEX_ATTRIB_TEX_COORD);

    auto texture = input->getSprite()->getTexture();
    auto sprite = Sprite::createWithTexture(texture);
    sprite->setAnchorPoint(Vec2(0.0f, 0.0f));
    sprite->setFlippedY(true);
    sprite->setGLProgram(shaderProgram);

    auto strength = 1.0f / (sprite->getContentSize().width * sprite->getScaleX()) * 0.5f;
    strength *= inputStrength;

    auto programState = sprite->getGLProgramState();
    programState->setUniformFloat("strength", strength);

    auto renderTexture = RenderTexture::create(
        texture->getPixelsWide(),
        texture->getPixelsHigh(),
        texture->getPixelFormat()
    );

    renderTexture->begin();
    sprite->visit();
    renderTexture->end();

    return renderTexture;
}

RenderTexture* BlurFilter::applyX(cocos2d::RenderTexture *input, int inputStrength) {
    return this->apply(input, inputStrength, Direction::X);
}

RenderTexture* BlurFilter::applyY(cocos2d::RenderTexture *input, int inputStrength) {
    return this->apply(input, inputStrength, Direction::Y);
}

int BlurFilter::getMaxKernelSize() {
    GLint maxVaryings;
    glGetIntegerv(GL_MAX_VARYING_VECTORS, &maxVaryings);
    auto kernelSize = 15;

    while (kernelSize > maxVaryings) {
        kernelSize -= 2;
    }

    return kernelSize;
}

std::string BlurFilter::generateVertBlurSource(int kernelSize, bool x) {
    const auto halfLength = ceil(kernelSize / 2.0);
    auto vertSource = FileUtils::getInstance()->getStringFromFile("shaders/blur.vert");

    std::stringstream blurLoop;
    std::string tpl;

    if (x) {
        tpl = "vBlurTexCoords[%index%] = a_texCoord + vec2(%sampleIndex% * strength, 0.0);";
    } else {
        tpl = "vBlurTexCoords[%index%] = a_texCoord + vec2(0.0, %sampleIndex% * strength);";
    }

    for (int i = 0; i < kernelSize; i++) {
        std::string blur (std::regex_replace(tpl, std::regex("%index%"), std::to_string(i)));
        blur = std::regex_replace(blur, std::regex("%sampleIndex%"), std::to_string(i - (halfLength - 1)));
        blurLoop << blur << std::endl;
    }

    vertSource = std::regex_replace(vertSource, std::regex("%blur%"), blurLoop.str());
    vertSource = std::regex_replace(vertSource, std::regex("%size%"), std::to_string(kernelSize));
    return vertSource;
}

std::string BlurFilter::generateFragBlurSource(int kernelSize) {
    const auto kernel = GAUSSIAN_VALUES.at(kernelSize);
    const auto halfLength = kernel.size();

    auto fragSource = FileUtils::getInstance()->getStringFromFile("shaders/blur.frag");
    std::stringstream blurLoop;
    std::string tpl ("gl_FragColor += texture2D(CC_Texture0, vBlurTexCoords[%index%]) * %value%;");
    int value;

    for (int i = 0; i < kernelSize; i++) {
        std::string blur (std::regex_replace(tpl, std::regex("%index%"), std::to_string(i)));
        value = i;

        if (i >= halfLength) {
            value = kernelSize - i - 1;
        }

        blur = std::regex_replace(blur, std::regex("%value%"), std::to_string(kernel[value]));
        blurLoop << blur << std::endl;
    }

    fragSource = std::regex_replace(fragSource, std::regex("%blur%"), blurLoop.str());
    fragSource = std::regex_replace(fragSource, std::regex("%size%"), std::to_string(kernelSize));
    return fragSource;
}
