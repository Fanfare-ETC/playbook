#include <unordered_map>
#include <vector>
#include "PlaybookEvent.h"

const std::unordered_map<std::string, PlaybookEvent::EventType> PlaybookEvent::STRING_EVENT_MAP = {
    {"ShutoutInning", EventType::SHUTOUT_INNING},
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
    {"HitByPitch", EventType::HIT_BY_PITCH},
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

const std::unordered_map<
    PlaybookEvent::EventType,
    std::string,
    PlaybookEvent::EventTypeHash> PlaybookEvent::EVENT_STRING_MAP = {
    {EventType::SHUTOUT_INNING, "ShutoutInning"},
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
    {EventType::HIT_BY_PITCH, "HitByPitch"},
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

const std::vector<PlaybookEvent::EventType> PlaybookEvent::INT_TO_EVENT_MAP = {
    EventType::SHUTOUT_INNING,
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
    EventType::HIT_BY_PITCH,
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

const std::unordered_map<
    PlaybookEvent::EventType,
    PlaybookEvent::Team,
    PlaybookEvent::EventTypeHash> PlaybookEvent::EVENT_TEAM_MAP = {
    {EventType::SHUTOUT_INNING, Team::NONE},
    {EventType::RUN_SCORED, Team::BATTING},
    {EventType::FLY_OUT, Team::FIELDING},
    {EventType::TRIPLE_PLAY, Team::FIELDING},
    {EventType::GROUND_OUT, Team::FIELDING},
    {EventType::DOUBLE_PLAY, Team::FIELDING},
    {EventType::DOUBLE, Team::BATTING},
    {EventType::STEAL, Team::BATTING},
    {EventType::PICK_OFF, Team::FIELDING},
    {EventType::STRIKEOUT, Team::FIELDING},
    {EventType::WALK, Team::BATTING},
    {EventType::TRIPLE, Team::BATTING},
    {EventType::SINGLE, Team::BATTING},
    {EventType::HIT_BY_PITCH, Team::BATTING},
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

const std::unordered_map<
    PlaybookEvent::EventType, bool,
    PlaybookEvent::EventTypeHash> PlaybookEvent::EVENT_IS_OUT_MAP = {
    {EventType::SHUTOUT_INNING, false},
    {EventType::RUN_SCORED, false},
    {EventType::FLY_OUT, true},
    {EventType::TRIPLE_PLAY, true},
    {EventType::GROUND_OUT, true},
    {EventType::DOUBLE_PLAY, true},
    {EventType::DOUBLE, false},
    {EventType::STEAL, false},
    {EventType::PICK_OFF, true},
    {EventType::STRIKEOUT, true},
    {EventType::WALK, false},
    {EventType::TRIPLE, false},
    {EventType::SINGLE, false},
    {EventType::HIT_BY_PITCH, false},
    {EventType::HOME_RUN, false},
    {EventType::PITCH_COUNT_16, false},
    {EventType::BLOCKED_RUN, true},
    {EventType::PITCH_COUNT_17, false},
    {EventType::BATTER_COUNT_4, false},
    {EventType::BATTER_COUNT_5, false},
    {EventType::MOST_IN_LEFT_OUTFIELD, false},
    {EventType::MOST_IN_RIGHT_OUTFIELD, false},
    {EventType::MOST_IN_INFIELD, false}
};

const std::unordered_map<
    PlaybookEvent::EventType, bool,
    PlaybookEvent::EventTypeHash> PlaybookEvent::EVENT_IS_ON_BASE_MAP = {
    {EventType::SHUTOUT_INNING, false},
    {EventType::RUN_SCORED, false},
    {EventType::FLY_OUT, false},
    {EventType::TRIPLE_PLAY, false},
    {EventType::GROUND_OUT, false},
    {EventType::DOUBLE_PLAY, false},
    {EventType::DOUBLE, true},
    {EventType::STEAL, false},
    {EventType::PICK_OFF, false},
    {EventType::STRIKEOUT, false},
    {EventType::WALK, true},
    {EventType::TRIPLE, true},
    {EventType::SINGLE, true},
    {EventType::HIT_BY_PITCH, true},
    {EventType::HOME_RUN, true},
    {EventType::PITCH_COUNT_16, false},
    {EventType::BLOCKED_RUN, false},
    {EventType::PITCH_COUNT_17, false},
    {EventType::BATTER_COUNT_4, false},
    {EventType::BATTER_COUNT_5, false},
    {EventType::MOST_IN_LEFT_OUTFIELD, false},
    {EventType::MOST_IN_RIGHT_OUTFIELD, false},
    {EventType::MOST_IN_INFIELD, false}
};

PlaybookEvent::EventType PlaybookEvent::stringToEvent(const std::string &event) {
    return STRING_EVENT_MAP.at(event);
}

std::string PlaybookEvent::eventToString(EventType event) {
    return EVENT_STRING_MAP.at(event);
}

PlaybookEvent::EventType PlaybookEvent::intToEvent(int event) {
    return INT_TO_EVENT_MAP[event];
}

PlaybookEvent::Team PlaybookEvent::getTeam(EventType event) {
    return EVENT_TEAM_MAP.at(event);
}

bool PlaybookEvent::isOut(EventType event) {
    return EVENT_IS_OUT_MAP.at(event);
}

bool PlaybookEvent::isOnBase(EventType event) {
    return EVENT_IS_ON_BASE_MAP.at(event);
}
