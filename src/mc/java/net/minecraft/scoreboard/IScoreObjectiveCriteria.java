package net.minecraft.scoreboard;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatColor;

public interface IScoreObjectiveCriteria
{
    Map<String, IScoreObjectiveCriteria> INSTANCES = Maps.<String, IScoreObjectiveCriteria>newHashMap();
    IScoreObjectiveCriteria DUMMY = new ScoreDummyCriteria("dummy");
    IScoreObjectiveCriteria TRIGGER = new ScoreDummyCriteria("trigger");
    IScoreObjectiveCriteria deathCount = new ScoreDummyCriteria("deathCount");
    IScoreObjectiveCriteria playerKillCount = new ScoreDummyCriteria("playerKillCount");
    IScoreObjectiveCriteria totalKillCount = new ScoreDummyCriteria("totalKillCount");
    IScoreObjectiveCriteria health = new ScoreHealthCriteria("health");
    IScoreObjectiveCriteria[] field_178792_h = new IScoreObjectiveCriteria[] {new GoalColor("teamkill.", ChatColor.BLACK), new GoalColor("teamkill.", ChatColor.DARK_BLUE), new GoalColor("teamkill.", ChatColor.DARK_GREEN), new GoalColor("teamkill.", ChatColor.DARK_AQUA), new GoalColor("teamkill.", ChatColor.DARK_RED), new GoalColor("teamkill.", ChatColor.DARK_PURPLE), new GoalColor("teamkill.", ChatColor.GOLD), new GoalColor("teamkill.", ChatColor.GRAY), new GoalColor("teamkill.", ChatColor.DARK_GRAY), new GoalColor("teamkill.", ChatColor.BLUE), new GoalColor("teamkill.", ChatColor.GREEN), new GoalColor("teamkill.", ChatColor.AQUA), new GoalColor("teamkill.", ChatColor.RED), new GoalColor("teamkill.", ChatColor.LIGHT_PURPLE), new GoalColor("teamkill.", ChatColor.YELLOW), new GoalColor("teamkill.", ChatColor.WHITE)};
    IScoreObjectiveCriteria[] field_178793_i = new IScoreObjectiveCriteria[] {new GoalColor("killedByTeam.", ChatColor.BLACK), new GoalColor("killedByTeam.", ChatColor.DARK_BLUE), new GoalColor("killedByTeam.", ChatColor.DARK_GREEN), new GoalColor("killedByTeam.", ChatColor.DARK_AQUA), new GoalColor("killedByTeam.", ChatColor.DARK_RED), new GoalColor("killedByTeam.", ChatColor.DARK_PURPLE), new GoalColor("killedByTeam.", ChatColor.GOLD), new GoalColor("killedByTeam.", ChatColor.GRAY), new GoalColor("killedByTeam.", ChatColor.DARK_GRAY), new GoalColor("killedByTeam.", ChatColor.BLUE), new GoalColor("killedByTeam.", ChatColor.GREEN), new GoalColor("killedByTeam.", ChatColor.AQUA), new GoalColor("killedByTeam.", ChatColor.RED), new GoalColor("killedByTeam.", ChatColor.LIGHT_PURPLE), new GoalColor("killedByTeam.", ChatColor.YELLOW), new GoalColor("killedByTeam.", ChatColor.WHITE)};

    String getName();

    int setScore(List<EntityPlayer> p_96635_1_);

    boolean isReadOnly();

    IScoreObjectiveCriteria.EnumRenderType getRenderType();

    public static enum EnumRenderType
    {
        INTEGER("integer"),
        HEARTS("hearts");

        private static final Map<String, IScoreObjectiveCriteria.EnumRenderType> field_178801_c = Maps.<String, IScoreObjectiveCriteria.EnumRenderType>newHashMap();
        private final String field_178798_d;

        private EnumRenderType(String p_i45548_3_)
        {
            this.field_178798_d = p_i45548_3_;
        }

        public String func_178796_a()
        {
            return this.field_178798_d;
        }

        public static IScoreObjectiveCriteria.EnumRenderType func_178795_a(String p_178795_0_)
        {
            IScoreObjectiveCriteria.EnumRenderType iscoreobjectivecriteria$enumrendertype = (IScoreObjectiveCriteria.EnumRenderType)field_178801_c.get(p_178795_0_);
            return iscoreobjectivecriteria$enumrendertype == null ? INTEGER : iscoreobjectivecriteria$enumrendertype;
        }

        static
        {
            for (IScoreObjectiveCriteria.EnumRenderType iscoreobjectivecriteria$enumrendertype : values())
            {
                field_178801_c.put(iscoreobjectivecriteria$enumrendertype.func_178796_a(), iscoreobjectivecriteria$enumrendertype);
            }
        }
    }
}
