#include <unordered_map>
#include <vector>
#include "PlaybookEvent.h"

PlaybookEvent::EventType PlaybookEvent::stringToEvent(const std::string &event) {
    std::unordered_map<std::string, EventType> map = {
        {"Error", EventType::error},
        {"GrandSlam", EventType::grandslam},
        {"ShutoutInning", EventType::shutout_inning},
        {"LongOut", EventType::longout},
        {"RunsBattedIn", EventType::runs_batted},
        {"PopFly", EventType::pop_fly},
        {"TriplePlay", EventType::triple_play},
        {"Grounder", EventType::grounder},
        {"DoublePlay", EventType::double_play},
        {"SecondBase", EventType::twob},
        {"Steal", EventType::steal},
        {"PickOff", EventType::pick_off},
        {"Strikeout", EventType::strike_out},
        {"Walk", EventType::walk},
        {"ThirdBase", EventType::threeb},
        {"FirstBase", EventType::oneb},
        {"Hit", EventType::hit},
        {"HomeRun", EventType::homerun},
        {"PitchCount16", EventType::pitchcount_16},
        {"BlockedRun", EventType::blocked_run},
        {"WalkOff", EventType::walk_off},
        {"PitchCount17", EventType::pitchcount_17}
    };

    return map[event];
}

std::string PlaybookEvent::eventToString(EventType event) {
    std::unordered_map<EventType, std::string, EventTypeHash> map = {
        {EventType::error, "Error"},
        {EventType::grandslam, "GrandSlam"},
        {EventType::shutout_inning, "ShutoutInning"},
        {EventType::longout, "LongOut"},
        {EventType::runs_batted, "RunBattedIn"},
        {EventType::pop_fly, "PopFly"},
        {EventType::triple_play, "TriplePlay"},
        {EventType::grounder, "Grounder"},
        {EventType::double_play, "DoublePlay"},
        {EventType::twob, "SecondBase"},
        {EventType::steal, "Steal"},
        {EventType::pick_off, "PickOff"},
        {EventType::strike_out, "Strikeout"},
        {EventType::walk, "Walk"},
        {EventType::threeb, "ThirdBase"},
        {EventType::oneb, "FirstBase"},
        {EventType::hit, "Hit"},
        {EventType::homerun, "HomeRun"},
        {EventType::pitchcount_16, "PitchCount16"},
        {EventType::blocked_run, "BlockedRun"},
        {EventType::walk_off, "WalkOff"},
        {EventType::pitchcount_17, "PitchCount17"}
    };

    return map[event];
}

PlaybookEvent::EventType PlaybookEvent::intToEvent(int event) {
    std::vector<EventType> map = {
        EventType::error,
        EventType::grandslam,
        EventType::shutout_inning,
        EventType::longout,
        EventType::runs_batted,
        EventType::pop_fly,
        EventType::triple_play,
        EventType::double_play,
        EventType::grounder,
        EventType::steal,
        EventType::pick_off,
        EventType::walk,
        EventType::blocked_run,
        EventType::strike_out,
        EventType::hit,
        EventType::homerun,
        EventType::pitchcount_16,
        EventType::walk_off,
        EventType::pitchcount_17,
        EventType::oneb,
        EventType::twob,
        EventType::threeb
    };

    return map[event];
}

PlaybookEvent::Team PlaybookEvent::getTeam(EventType event) {
    std::unordered_map<EventType, Team, EventTypeHash> map = {
        {EventType::error, Team::NONE},
        {EventType::grandslam, Team::BATTING},
        {EventType::shutout_inning, Team::NONE},
        {EventType::longout, Team::FIELDING},
        {EventType::runs_batted, Team::BATTING},
        {EventType::pop_fly, Team::NONE},
        {EventType::triple_play, Team::FIELDING},
        {EventType::grounder, Team::NONE},
        {EventType::double_play, Team::FIELDING},
        {EventType::twob, Team::BATTING},
        {EventType::steal, Team::BATTING},
        {EventType::pick_off, Team::FIELDING},
        {EventType::strike_out, Team::FIELDING},
        {EventType::walk, Team::BATTING},
        {EventType::threeb, Team::BATTING},
        {EventType::oneb, Team::BATTING},
        {EventType::hit, Team::BATTING},
        {EventType::homerun, Team::BATTING},
        {EventType::pitchcount_16, Team::NONE},
        {EventType::blocked_run, Team::FIELDING},
        {EventType::walk_off, Team::BATTING},
        {EventType::pitchcount_17, Team::NONE}
    };

    return map[event];
}
