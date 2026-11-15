package net.minecraft.world.storage;

import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISaveFormat
{
    @SideOnly(Side.CLIENT)
    String getName();

    ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata);

    @SideOnly(Side.CLIENT)
    List<SaveFormatComparator> getSaveList() throws AnvilConverterException;

    void flushCache();

    @SideOnly(Side.CLIENT)
    WorldInfo getWorldInfo(String saveName);

    @SideOnly(Side.CLIENT)
    boolean isNewLevelIdAcceptable(String saveName);

    boolean deleteWorldDirectory(String saveName);

    @SideOnly(Side.CLIENT)
    void renameWorld(String dirName, String newName);

    @SideOnly(Side.CLIENT)
    boolean isConvertible(String saveName);

    boolean isOldMapFormat(String saveName);

    boolean convertMapFormat(String filename, IProgressUpdate progressCallback);

    @SideOnly(Side.CLIENT)
    boolean canLoadWorld(String saveName);
}
