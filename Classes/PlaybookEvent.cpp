#include <unordered_map>
#include <vector>
#include "PlaybookEvent.h"

PlaybookEvent::EventType PlaybookEvent::stringToEvent(const std::string &event) {
    std::unordered_map<std::string, EventType> map = {
        {"Error", EventType::ERROR},
        {"GrandSlam", EventType::GRAND_SLAM},
        {"ShutoutInning", EventType::SHUTOUT_INNING},
        {"LongOut", EventType::LONG_OUT},
        {"RunsBattedIn", EventType::RUN_BATTED_IN},
        {"PopFly", EventType::POP_FLY},
        {"TriplePlay", EventType::TRIPLE_PLAY},
        {"Grounder", EventType::GROUNDER},
        {"DoublePlay", EventType::DOUBLE_PLAY},
        {"SecondBase", EventType::DOUBLE},
        {"Steal", EventType::STEAL},
        {"PickOff", EventType::PICK_OFF},
        {"Strikeout", EventType::STRIKE_OUT},
        {"Walk", EventType::WALK},
        {"ThirdBase", EventType::TRIPLE},
        {"FirstBase", EventType::SINGLE},
        {"Hit", EventType::HIT},
        {"HomeRun", EventType::HOME_RUN},
        {"PitchCount16", EventType::PITCH_COUNT_16},
        {"BlockedRun", EventType::BLOCKED_RUN},
        {"WalkOff", EventType::WALK_OFF},
        {"PitchCount17", EventType::PITCH_COUNT_17}
    };

    return map[event];
}

std::string PlaybookEvent::eventToString(EventType event) {
    std::unordered_map<EventType, std::string, EventTypeHash> map = {
        {EventType::ERROR, "Error"},
        {EventType::GRAND_SLAM, "GrandSlam"},
        {EventType::SHUTOUT_INNING, "ShutoutInning"},
        {EventType::LONG_OUT, "LongOut"},
        {EventType::RUN_BATTED_IN, "RunBattedIn"},
        {EventType::POP_FLY, "PopFly"},
        {EventType::TRIPLE_PLAY, "TriplePlay"},
        {EventType::GROUNDER, "Grounder"},
        {EventType::DOUBLE_PLAY, "DoublePlay"},
        {EventType::DOUBLE, "SecondBase"},
        {EventType::STEAL, "Steal"},
        {EventType::PICK_OFF, "PickOff"},
        {EventType::STRIKE_OUT, "Strikeout"},
        {EventType::WALK, "Walk"},
        {EventType::TRIPLE, "ThirdBase"},
        {EventType::SINGLE, "FirstBase"},
        {EventType::HIT, "Hit"},
        {EventType::HOME_RUN, "HomeRun"},
        {EventType::PITCH_COUNT_16, "PitchCount16"},
        {EventType::BLOCKED_RUN, "BlockedRun"},
        {EventType::WALK_OFF, "WalkOff"},
        {EventType::PITCH_COUNT_17, "PitchCount17"}
    };

    return map[event];
}

PlaybookEvent::EventType PlaybookEvent::intToEvent(int event) {
    std::vector<EventType> map = {
        EventType::ERROR,
        EventType::GRAND_SLAM,
        EventType::SHUTOUT_INNING,
        EventType::LONG_OUT,
        EventType::RUN_BATTED_IN,
        EventType::POP_FLY,
        EventType::TRIPLE_PLAY,
        EventType::DOUBLE_PLAY,
        EventType::GROUNDER,
        EventType::STEAL,
        EventType::PICK_OFF,
        EventType::WALK,
        EventType::BLOCKED_RUN,
        EventType::STRIKE_OUT,
        EventType::HIT,
        EventType::HOME_RUN,
        EventType::PITCH_COUNT_16,
        EventType::WALK_OFF,
        EventType::PITCH_COUNT_17,
        EventType::SINGLE,
        EventType::DOUBLE,
        EventType::TRIPLE
    };

    return map[event];
}

PlaybookEvent::Team PlaybookEvent::getTeam(EventType event) {
    std::unordered_map<EventType, Team, EventTypeHash> map = {
        {EventType::ERROR, Team::NONE},
        {EventType::GRAND_SLAM, Team::BATTING},
        {EventType::SHUTOUT_INNING, Team::NONE},
        {EventType::LONG_OUT, Team::FIELDING},
        {EventType::RUN_BATTED_IN, Team::BATTING},
        {EventType::POP_FLY, Team::NONE},
        {EventType::TRIPLE_PLAY, Team::FIELDING},
        {EventType::GROUNDER, Team::NONE},
        {EventType::DOUBLE_PLAY, Team::FIELDING},
        {EventType::DOUBLE, Team::BATTING},
        {EventType::STEAL, Team::BATTING},
        {EventType::PICK_OFF, Team::FIELDING},
        {EventType::STRIKE_OUT, Team::FIELDING},
        {EventType::WALK, Team::BATTING},
        {EventType::TRIPLE, Team::BATTING},
        {EventType::SINGLE, Team::BATTING},
        {EventType::HIT, Team::BATTING},
        {EventType::HOME_RUN, Team::BATTING},
        {EventType::PITCH_COUNT_16, Team::NONE},
        {EventType::BLOCKED_RUN, Team::FIELDING},
        {EventType::WALK_OFF, Team::BATTING},
        {EventType::PITCH_COUNT_17, Team::NONE}
    };

    return map[event];
}
