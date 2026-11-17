# OpenAdjust

A complete Java source restoration of the **Adjust Client** for **Minecraft 1.8.9** — based on **Adjust (`November 8th` build, changelog version `99`)**, with all `102` modules and `29` commands running standalone offline on Java 21 + LWJGL 3.3.3.

*This text is here as a placeholder commit so this repo doesn't get taken down if upstream gets DMCA'd.*<br/>
*If you have an issue with this being up, DM me on Discord: unleg1tt, and I'll take down, please don't DMCA my account* :sob:

- **Website:** https://adjust.wtf/
- **Discord:** https://discord.adjust.wtf/ — come join the community for updates, support, and discussion!

> **Backup / Mirror:** https://git.yxz.st/yxzero/openadjust  
> Please bookmark and use this mirror if you can — the main git repo might get hit with a DMCA at any time!

## Credits

**Source completely restored by AI! Zero human work.**

- **Claude Opus 5.5** — initial planning and ~20% of the job
- **GPT 6.1 Sol** — large parts of restoration
- **Gemini 4 Carbon** — large parts of restoration and final bug fixes

Native Image cannot stop open source! :)

## Requirements

- **JDK 21** (automatically resolved via Gradle's Java toolchain)
- Windows x64 (for bundled LWJGL 3.3.3 Windows natives and `opus.dll`; adjust `lwjglNatives` in `build.gradle` for other platforms)

## Building & Running

Launch directly in offline dev mode:

```bat
.\gradlew.bat runClient
```

Or use the included helper script on Windows:

```bat
launch_client.bat
```

Build the client JAR (`build/libs/adjust-client-1.8.9.jar`):

```bat
.\gradlew.bat build
```
