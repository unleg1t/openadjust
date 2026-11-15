package net.minecraft.event;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;

public class ClickEvent
{
    private final ClickEvent.Action action;
    private final String value;
    public Runnable displayOnly;

    public ClickEvent(ClickEvent.Action theAction, String theValue)
    {
        this(theAction, theValue, (Runnable)null);
    }

    public ClickEvent(ClickEvent.Action theAction, String theValue, Runnable displayOnly)
    {
        this.action = theAction;
        this.value = theValue;
        this.displayOnly = displayOnly;
    }

    public ClickEvent.Action getAction()
    {
        return this.action;
    }

    public String getValue()
    {
        return this.value;
    }

    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass())
        {
            ClickEvent clickevent = (ClickEvent)p_equals_1_;

            if (this.action != clickevent.action)
            {
                return false;
            }
            else
            {
                if (this.value != null)
                {
                    if (!this.value.equals(clickevent.value))
                    {
                        return false;
                    }
                }
                else if (clickevent.value != null)
                {
                    return false;
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return "ClickEvent{action=" + this.action + ", value=\'" + this.value + '\'' + '}';
    }

    public int hashCode()
    {
        int i = Objects.hashCode(this.action);
        i = 31 * i + Objects.hashCode(this.value);
        return i;
    }

    public static enum Action
    {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        TWITCH_USER_INFO("twitch_user_info", false),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true);

        private static final Map<String, ClickEvent.Action> nameMapping = Maps.<String, ClickEvent.Action>newHashMap();
        private final boolean allowedInChat;
        private final String canonicalName;

        private Action(String canonicalNameIn, boolean allowedInChatIn)
        {
            this.canonicalName = canonicalNameIn;
            this.allowedInChat = allowedInChatIn;
        }

        public boolean shouldAllowInChat()
        {
            return this.allowedInChat;
        }

        public String getCanonicalName()
        {
            return this.canonicalName;
        }

        public static ClickEvent.Action getValueByCanonicalName(String canonicalNameIn)
        {
            return (ClickEvent.Action)nameMapping.get(canonicalNameIn);
        }

        static
        {
            for (ClickEvent.Action clickevent$action : values())
            {
                nameMapping.put(clickevent$action.getCanonicalName(), clickevent$action);
            }
        }
    }
}
