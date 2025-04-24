package core;

import java.util.HashSet;
import java.util.Set;

public class RunningTaskInfo {
    public Thread thread;
    public long startTimeMillis;
    public int originalDuration;
    public Set<String> participants = new HashSet<>();

    public RunningTaskInfo(Thread thread, int originalDuration, String firstAgent) {
        this.thread = thread;
        this.originalDuration = originalDuration;
        this.startTimeMillis = System.currentTimeMillis();
        this.participants.add(firstAgent);
    }

    public int getElapsedSeconds() {
        return (int)((System.currentTimeMillis() - startTimeMillis) / 1000);
    }

    public void addParticipant(String agentName) {
        participants.add(agentName);
    }

    public int getParticipantCount() {
        return participants.size();
    }
}
