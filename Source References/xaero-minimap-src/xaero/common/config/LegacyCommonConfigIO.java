package xaero.common.config;

import com.google.common.collect.Sets;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import xaero.common.HudMod;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.common.config.primary.option.MinimapPrimaryCommonConfigOptions;
import xaero.lib.common.config.profile.ConfigProfile;
import xaero.lib.common.util.IOUtils;

public class LegacyCommonConfigIO {
   private final Path configFilePath;
   private boolean allowCaveModeOnServer;
   private boolean allowNetherCaveModeOnServer;
   private boolean shouldEnableEveryoneTracksEveryone;

   public LegacyCommonConfigIO(Path configFilePath) {
      this.configFilePath = configFilePath;
   }

   public void load() {
      ConfigProfile defaultEnforcedProfile = HudMod.INSTANCE.getHudConfigs().getServerConfigManager().getDefaultEnforcedProfile();

      try {
         BufferedInputStream bufferedOutput = new BufferedInputStream(new FileInputStream(this.configFilePath.toFile()));

         label191: {
            label190: {
               label189: {
                  try {
                     BufferedReader reader;
                     label186: {
                        label185: {
                           label197: {
                              reader = new BufferedReader(new InputStreamReader(bufferedOutput));

                              try {
                                 try {
                                    while(true) {
                                       String line;
                                       if ((line = reader.readLine()) == null) {
                                          if (this.allowCaveModeOnServer && this.allowNetherCaveModeOnServer) {
                                             break label185;
                                          }

                                          if (!this.allowCaveModeOnServer && !this.allowNetherCaveModeOnServer) {
                                             defaultEnforcedProfile.set(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED, false);
                                             break label197;
                                          }

                                          if (this.allowCaveModeOnServer) {
                                             defaultEnforcedProfile.set(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS, Sets.newHashSet(new class_2960[]{class_1937.field_25179.method_29177(), class_1937.field_25181.method_29177()}));
                                             break;
                                          }

                                          defaultEnforcedProfile.set(MinimapProfiledConfigOptions.CAVE_MODE_ALLOWED_DIMENSIONS, Sets.newHashSet(new class_2960[]{class_1937.field_25180.method_29177()}));
                                          break label186;
                                       }

                                       this.readLine(line.split(":"));
                                    }
                                 } finally {
                                    HudMod.INSTANCE.getHudConfigs().getPrimaryCommonConfigManagerIO().save();
                                    HudMod.INSTANCE.getHudConfigs().getServerConfigProfileIO().save(defaultEnforcedProfile);
                                    reader.close();
                                    IOUtils.tryQuickFileBackupMove(this.configFilePath, 10);
                                 }
                              } catch (Throwable var15) {
                                 try {
                                    reader.close();
                                 } catch (Throwable var13) {
                                    var15.addSuppressed(var13);
                                 }

                                 throw var15;
                              }

                              reader.close();
                              break label189;
                           }

                           reader.close();
                           break label190;
                        }

                        reader.close();
                        break label191;
                     }

                     reader.close();
                  } catch (Throwable var16) {
                     try {
                        bufferedOutput.close();
                     } catch (Throwable var12) {
                        var16.addSuppressed(var12);
                     }

                     throw var16;
                  }

                  bufferedOutput.close();
                  return;
               }

               bufferedOutput.close();
               return;
            }

            bufferedOutput.close();
            return;
         }

         bufferedOutput.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private boolean readLine(String[] args) {
      if (args[0].equals("allowCaveModeOnServer")) {
         this.allowCaveModeOnServer = args[1].equals("true");
         return true;
      } else if (args[0].equals("allowNetherCaveModeOnServer")) {
         this.allowNetherCaveModeOnServer = args[1].equals("true");
         return true;
      } else if (args[0].equals("allowRadarOnServer")) {
         if (!args[1].equals("true")) {
            HudMod.INSTANCE.getHudConfigs().getServerConfigManager().getDefaultEnforcedProfile().set(MinimapProfiledConfigOptions.DISPLAY_RADAR, false);
         }

         return true;
      } else if (args[0].equals("registerStatusEffects")) {
         HudMod.INSTANCE.getHudConfigs().getPrimaryCommonConfigManager().getConfig().set(MinimapPrimaryCommonConfigOptions.REGISTER_EFFECTS, args[1].equals("true"));
         return true;
      } else if (args[0].equals("everyoneTracksEveryone") && args[1].equals("true")) {
         this.shouldEnableEveryoneTracksEveryone = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean shouldEnableEveryoneTracksEveryone() {
      return this.shouldEnableEveryoneTracksEveryone;
   }
}
