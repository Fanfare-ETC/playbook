#include <unordered_map>
#include <vector>
#include "PlaybookEvent.h"

PlaybookEvent::EventType PlaybookEvent::stringToEvent(const std::string &event) {
    std::unordered_map<std::string, EventType> map = {
        {"Error", EventType::ERROR},
        {"GrandSlam", EventType::GRAND_SLAM},
        {"ShutoutInning", EventType::SHUTOUT_INNING},
        {"LongOut", EventType::LONG_OUT},
        {"RunScored", EventType::RUN_SCORED},
        {"FlyOut", EventType::FLY_OUT},
        {"TriplePlay", EventType::TRIPLE_PLAY},
        {"GroundOut", EventType::GROUND_OUT},
        {"DoublePlay", EventType::DOUBLE_PLAY},
        {"SecondBase", EventType::DOUBLE},
        {"Steal", EventType::STEAL},
        {"PickOff", EventType::PICK_OFF},
        {"Strikeout", EventType::STRIKEOUT},
        {"Walk", EventType::WALK},
        {"ThirdBase", EventType::TRIPLE},
        {"FirstBase", EventType::SINGLE},
        {"Hit", EventType::HIT},
        {"HomeRun", EventType::HOME_RUN},
        {"PitchCount16", EventType::PITCH_COUNT_16},
        {"BlockedRun", EventType::BLOCKED_RUN},
        {"PitchCount17", EventType::PITCH_COUNT_17},
        {"BatterCount4", EventType::BATTER_COUNT_4},
        {"BatterCount5", EventType ::BATTER_COUNT_5},
        {"MostInLeftOutfield", EventType::MOST_IN_LEFT_OUTFIELD},
        {"MostInRightOutfield", EventType::MOST_IN_RIGHT_OUTFIELD},
        {"MostInInfield", EventType::MOST_IN_INFIELD}
    };

    return map[event];
}

std::string PlaybookEvent::eventToString(EventType event) {
    std::unordered_map<EventType, std::string, EventTypeHash> map = {
        {EventType::ERROR, "Error"},
        {EventType::GRAND_SLAM, "GrandSlam"},
        {EventType::SHUTOUT_INNING, "ShutoutInning"},
        {EventType::LONG_OUT, "LongOut"},
        {EventType::RUN_SCORED, "RunScored"},
        {EventType::FLY_OUT, "FlyOut"},
        {EventType::TRIPLE_PLAY, "TriplePlay"},
        {EventType::GROUND_OUT, "GroundOut"},
        {EventType::DOUBLE_PLAY, "DoublePlay"},
        {EventType::DOUBLE, "SecondBase"},
        {EventType::STEAL, "Steal"},
        {EventType::PICK_OFF, "PickOff"},
        {EventType::STRIKEOUT, "Strikeout"},
        {EventType::WALK, "Walk"},
        {EventType::TRIPLE, "ThirdBase"},
        {EventType::SINGLE, "FirstBase"},
        {EventType::HIT, "Hit"},
        {EventType::HOME_RUN, "HomeRun"},
        {EventType::PITCH_COUNT_16, "PitchCount16"},
        {EventType::BLOCKED_RUN, "BlockedRun"},
        {EventType::PITCH_COUNT_17, "PitchCount17"},
        {EventType::BATTER_COUNT_4, "BatterCount4"},
        {EventType::BATTER_COUNT_5, "BatterCount5"},
        {EventType::MOST_IN_LEFT_OUTFIELD, "MostInLeftOutfield"},
        {EventType::MOST_IN_RIGHT_OUTFIELD, "MostInRightOutfield"},
        {EventType::MOST_IN_INFIELD, "MostInInfield"}
    };

    return map[event];
}

PlaybookEvent::EventType PlaybookEvent::intToEvent(int event) {
    std::vector<EventType> map = {
        EventType::ERROR,
        EventType::GRAND_SLAM,
        EventType::SHUTOUT_INNING,
        EventType::LONG_OUT,
        EventType::RUN_SCORED,
        EventType::FLY_OUT,
        EventType::TRIPLE_PLAY,
        EventType::DOUBLE_PLAY,
        EventType::GROUND_OUT,
        EventType::STEAL,
        EventType::PICK_OFF,
        EventType::WALK,
        EventType::BLOCKED_RUN,
        EventType::STRIKEOUT,
        EventType::HIT,
        EventType::HOME_RUN,
        EventType::PITCH_COUNT_16,
        EventType::PITCH_COUNT_17,
        EventType::SINGLE,
        EventType::DOUBLE,
        EventType::TRIPLE,
        EventType::BATTER_COUNT_4,
        EventType::BATTER_COUNT_5,
        EventType::MOST_IN_LEFT_OUTFIELD,
        EventType::MOST_IN_RIGHT_OUTFIELD,
        EventType::MOST_IN_INFIELD
    };

    return map[event];
}

PlaybookEvent::Team PlaybookEvent::getTeam(EventType event) {
    std::unordered_map<EventType, Team, EventTypeHash> map = {
        {EventType::ERROR, Team::NONE},
        {EventType::GRAND_SLAM, Team::BATTING},
        {EventType::SHUTOUT_INNING, Team::NONE},
        {EventType::LONG_OUT, Team::FIELDING},
        {EventType::RUN_SCORED, Team::BATTING},
        {EventType::FLY_OUT, Team::NONE},
        {EventType::TRIPLE_PLAY, Team::FIELDING},
        {EventType::GROUND_OUT, Team::NONE},
        {EventType::DOUBLE_PLAY, Team::FIELDING},
        {EventType::DOUBLE, Team::BATTING},
        {EventType::STEAL, Team::BATTING},
        {EventType::PICK_OFF, Team::FIELDING},
        {EventType::STRIKEOUT, Team::FIELDING},
        {EventType::WALK, Team::BATTING},
        {EventType::TRIPLE, Team::BATTING},
        {EventType::SINGLE, Team::BATTING},
        {EventType::HIT, Team::BATTING},
        {EventType::HOME_RUN, Team::BATTING},
        {EventType::PITCH_COUNT_16, Team::NONE},
        {EventType::BLOCKED_RUN, Team::FIELDING},
        {EventType::PITCH_COUNT_17, Team::NONE},
        {EventType::BATTER_COUNT_4, Team::NONE},
        {EventType::BATTER_COUNT_5, Team::NONE},
        {EventType::MOST_IN_LEFT_OUTFIELD, Team::NONE},
        {EventType::MOST_IN_RIGHT_OUTFIELD, Team::NONE},
        {EventType::MOST_IN_INFIELD, Team::NONE}
    };

    return map[event];
}
