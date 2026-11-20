package com.adjustclient.core.gui.account.repository;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.ApiResultCallback;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.utilities.server.ServerRegion;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.AccountManagerScreen;
import com.adjustclient.core.gui.account.login.CookieFileSelection;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.TimeUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.ChatColor;
import org.lwjgl.opengl.GL11;

public final class AccountRepository implements ApiResultCallback, GuiListExtended.IGuiListEntry {
    public String name;
    public final ArrayList<CookieFileSelection> members;
    public final ArrayList<AccountEntry> accounts;
    public String[] memberNames;
    public final Animation highlight;
    public long lastAccessed;
    public float maxTextWidth;
    public int id;
    public int accountCount;
    public boolean loading;
    public boolean loaded;
    public boolean fadingSelection;

    public AccountRepository(String name, int id) {
        this.name = name;
        this.id = id;
        members = new ArrayList<>();
        accounts = new ArrayList<>();
        highlight = new Animation(Easing.EASE_OUT_SINE, 300L);
        maxTextWidth = -1.0F;
        loading = true;
    }

    public AccountEntry getAccount(int accountId) {
        for (AccountEntry account : accounts) {
            if (account.id == accountId) return account;
        }
        return null;
    }

    public void addMember(ClientUser user, ServerRegion role) {
        CookieFileSelection existing = null;
        synchronized (members) {
            for (CookieFileSelection member : members) {
                if (member.user == user) {
                    existing = member;
                    break;
                }
            }
        }
        if (existing != null) {
            existing.role = role;
            return;
        }
        synchronized (members) {
            members.add(new CookieFileSelection(this, user, role));
        }
    }

    public void setMembers(String[] names, ServerRegion[] roles) {
        synchronized (members) {
            members.clear();
            for (int i = 0; i < names.length; i++) {
                addMember(((com.adjustclient.core.settings.property.UserManager) Adjust.INSTANCE.field40).getOrCreate(names[i]), roles[i]);
            }
        }
        refreshMemberNames();
    }

    public List<AccountEntry> findAccountsByUuid(String uuid) {
        return accounts.stream().filter(account -> account.uuid.equalsIgnoreCase(uuid))
                .toList();
    }

    @Override
    public void setSelected(int slotIndex, int x, int y) {
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        AccountManagerScreen manager = Adjust.INSTANCE.accountManager;
        if (manager.currentRepository != this) {
            manager.currentRepository = this;
            manager.selection.clear();
            com.adjustclient.core.gui.account.LocalAccountStore.INSTANCE.selected(this);
        }
        return false;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean selected) {
        var font = Minecraft.getMinecraft().fontRendererObj;
        String title = name + ChatColor.RESET + " (" + ChatColor.GRAY
                + (loaded && !loading ? accounts.size() : accountCount) + ChatColor.RESET + ")";
        float left = x - 1;
        float top = y + 1.0F;
        boolean hovered = mouseX >= left && mouseX <= left + listWidth
                && mouseY >= top && mouseY <= top + slotHeight;
        boolean current = Adjust.INSTANCE.accountManager.currentRepository == this;
        if (current) {
            highlight.update(1.0F);
            title = ChatColor.RESET + title;
            fadingSelection = true;
        } else {
            highlight.update(!fadingSelection && hovered ? 0.5F : 0.0F);
            if (fadingSelection && highlight.value <= 0.1F) fadingSelection = false;
        }
        float center = left + listWidth / 2.0F;
        float width = font.getStringWidth(title, 1.0F);
        int titleColor = ColorUtil.blend(Color.WHITE, new Color(160, 160, 160, 200), highlight.value).getRGB();
        font.drawString(title, titleColor, true, center - width / 2.0F, top + 2.0F, 1.0F);
        maxTextWidth = Math.max(maxTextWidth, width);
        if (current || fadingSelection) top += highlight.value;
        int detailsColor = ColorUtil.blend(new Color(235, 235, 235, 255), new Color(200, 200, 200, 120), highlight.value).getRGB();
        String accessed = lastAccessed < 1970L ? "Never accessed"
                : "Last accessed " + ChatColor.GRAY + TimeUtil.timeAgo(lastAccessed, " ago");
        GL11.glScalef(0.50505048F, 0.50505048F, 0.50505048F);
        float accessedWidth = font.getStringWidth(accessed, 1.0F) / 1.98F;
        font.drawString(accessed, detailsColor, true, (center - accessedWidth / 2.0F) * 1.98F,
                (top + 13.0F) * 1.98F, 1.0F);
        GL11.glScalef(1.98F, 1.98F, 1.98F);
        maxTextWidth = Math.max(maxTextWidth, accessedWidth);
        String memberText = formatMembers();
        GL11.glScalef(0.50505048F, 0.50505048F, 0.50505048F);
        float memberWidth = font.getStringWidth(memberText, 1.0F) / 1.98F;
        font.drawString(memberText, detailsColor, true, (center - memberWidth / 2.0F) * 1.98F,
                (top + 19.0F) * 1.98F, 1.0F);
        GL11.glScalef(1.98F, 1.98F, 1.98F);
        maxTextWidth = Math.max(maxTextWidth, memberWidth);
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    public boolean hasMember(String name) {
        return Arrays.stream(memberNames).anyMatch(member -> member.equalsIgnoreCase(name));
    }

    public void refreshMemberNames() {
        String[] names;
        synchronized (members) {
            names = new String[members.size()];
            for (int i = 0; i < names.length; i++) names[i] = members.get(i).user.name;
        }
        memberNames = names;
    }

    public String formatMembers() {
        if (members.isEmpty()) return "Local owner";
        synchronized (members) {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (CookieFileSelection member : members) {
                if (!first) result.append(", ");
                if (member.role == ServerRegion.OWNER) result.append(ChatColor.DARK_AQUA);
                else if (member.role == ServerRegion.ADMIN) result.append(ChatColor.RED);
                result.append(member.user.name).append(ChatColor.RESET);
                first = false;
            }
            return result.toString();
        }
    }

    @Override
    public String toString() {
        return "AccountRepository{id=" + id + ", repositoryName='" + name + "'}";
    }
}
