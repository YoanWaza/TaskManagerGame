package core;

import family.Agent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRegistry {
    private static final Map<String, Agent> agents = new ConcurrentHashMap<>();

    public static void register(String name, Agent agent) {
        agents.put(name, agent);
    }

    public static Agent get(String name) {
        return agents.get(name);
    }

    public static void clear() {
        agents.clear();
    }
}
