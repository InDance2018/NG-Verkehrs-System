#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/cycle-tests
javac -d build/cycle-tests src/main/java/de/nevergate/verkehr/TrafficCycle.java src/main/java/de/nevergate/verkehr/ConflictRules.java src/main/java/de/nevergate/verkehr/CrossingCycle.java src/main/java/de/nevergate/verkehr/WayfindingData.java src/main/java/de/nevergate/verkehr/RoadSystemData.java src/main/java/de/nevergate/verkehr/RoadVehicleRules.java src/main/java/de/nevergate/verkehr/ConstructionRules.java src/main/java/de/nevergate/verkehr/PriorityCycle.java tests/*.java
for test in TrafficCycleTest CrossingCycleTest CrossingGroupsTest ProtectedLeftTest AllSignalsTest ProgramsTest WayfindingDataTest RoadSystemDataTest ConstructionRulesTest PriorityCycleTest; do
    java -cp build/cycle-tests "$test"
done
