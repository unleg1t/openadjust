package com.adjustclient.managers.poll;

import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.utilities.Timer;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class PollService {
    public String question;
    public List<PollOptionButton> options;
    public List<String> questionLines;
    public PollOptionButton selected;
    public Timer timer;
    public long pollId;
    public float questionWidth;
    public int totalVotes;
    public boolean field40;
    public static final Comparator<PollOptionButton> VOTES_DESCENDING = (first, second) -> second.votes - first.votes;

    public PollService(String question, long pollId, List<PollOptionButton> choices) {
        this.question = question;
        this.pollId = pollId;
        this.timer = new Timer();
        this.options = java.util.Collections.unmodifiableList(choices);
        for (PollOptionButton option : choices) option.poll = this;
    }

    public static PollService readPoll(DataInputStream input) throws IOException {
        if (input.readBoolean()) return null;
        String question = input.readUTF();
        long id = input.readLong();
        int count = ByteUtil.readVarInt(input);
        List<PollOptionButton> choices = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            int votes = ByteUtil.readVarInt(input);
            String text = input.readUTF();
            choices.add(new PollOptionButton(text, votes));
        }
        return new PollService(question, id, choices);
    }

    public void select(PollOptionButton option) {
        if (selected != null) {
            --selected.votes;
            totalVotes = options.stream().mapToInt(value -> value.votes).sum();
            updatePercentages();
        }
        selected = option;
        if (option != null) {
            ++option.votes;
            option.poll.recount();
        }
    }

    public void prepareQuestion() {
        if (questionLines != null) return;
        NvgFontRenderer font = com.adjustclient.core.rendering.font.Fonts.TAHOMA_12;
        List<IChatComponent> lines = GuiUtilRenderComponents.splitText(new ChatComponentText(question), 220, font, false, false);
        questionLines = new ArrayList<>(lines.size());
        for (IChatComponent line : lines) {
            String text = line.getFormattedText();
            questionLines.add(text);
            questionWidth = Math.max(font.getStringWidth(text), questionWidth);
        }
    }

    public void updatePercentages() {
        if (totalVotes == 0) {
            options.forEach(option -> option.percentage = 0);
            return;
        }
        List<PollOptionButton> sorted = new ArrayList<>(options);
        sorted.sort(VOTES_DESCENDING);
        int remainder = 100;
        for (PollOptionButton option : sorted) {
            option.percentage = 100 * option.votes / totalVotes;
            remainder -= option.percentage;
        }
        for (int i = 0; i < remainder; ++i) sorted.get(i).percentage++;
    }

    public void recount() {
        totalVotes = options.stream().mapToInt(option -> option.votes).sum();
        updatePercentages();
    }
}
