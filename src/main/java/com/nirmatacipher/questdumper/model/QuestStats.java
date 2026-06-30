package com.nirmatacipher.questdumper.model;

import com.nirmatacipher.questdumper.QuestDumper;

import java.util.ArrayList;
import java.util.List;

public final class QuestStats {
    public String questFileClass = "unknown";
    public int chapters;
    public int quests;
    public int tasks;
    public int rewards;
    public int rewardTables;
    public int chapterGroups;
    public int teamData;
    public int allObjects;
    public final List<String> chapterNames = new ArrayList<>();

    public int chapters() { return chapters; }
    public int quests() { return quests; }
    public int tasks() { return tasks; }
    public int rewards() { return rewards; }
    public int rewardTables() { return rewardTables; }
    public int chapterGroups() { return chapterGroups; }

    public String toJson(String stamp) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"tool\": \"Arcanum Quest Dumper\",\n");
        sb.append("  \"author\": \"NirmataCipher\",\n");
        sb.append("  \"version\": \"").append(escape(QuestDumper.VERSION)).append("\",\n");
        sb.append("  \"timestamp\": \"").append(escape(stamp)).append("\",\n");
        sb.append("  \"questFileClass\": \"").append(escape(questFileClass)).append("\",\n");
        sb.append("  \"chapters\": ").append(chapters).append(",\n");
        sb.append("  \"quests\": ").append(quests).append(",\n");
        sb.append("  \"tasks\": ").append(tasks).append(",\n");
        sb.append("  \"rewards\": ").append(rewards).append(",\n");
        sb.append("  \"rewardTables\": ").append(rewardTables).append(",\n");
        sb.append("  \"chapterGroups\": ").append(chapterGroups).append(",\n");
        sb.append("  \"teamDataEntries\": ").append(teamData).append(",\n");
        sb.append("  \"allObjects\": ").append(allObjects).append(",\n");
        sb.append("  \"chapterNames\": [\n");
        for (int i = 0; i < chapterNames.size(); i++) {
            sb.append("    \"").append(escape(chapterNames.get(i))).append("\"");
            if (i + 1 < chapterNames.size()) sb.append(',');
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
