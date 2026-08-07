package xaero.common.minimap.region;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import net.minecraft.class_1011.class_1012;
import org.joml.Math;
import xaero.hud.minimap.Minimap;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.client.graphics.util.TextureUtils;

public class MinimapChunk {
   public static final int SIZE_TILES = 4;
   public static final int INT_BUFFER_SIZE = 4096;
   public static final int LIGHT_LEVELS = 5;
   private boolean blockTextureUpload;
   private int X;
   private int Z;
   private boolean hasSomething;
   private MinimapTile[][] tiles;
   private GpuTextureAndView[] glTexture;
   private boolean[] refreshRequired;
   private boolean refreshed;
   private ByteBuffer[] bufferBase;
   private IntBuffer[] buffer;
   private boolean changed;
   private int levelsBuffered = 0;

   public MinimapChunk(int X, int Z) {
      this.X = X;
      this.Z = Z;
      this.tiles = new MinimapTile[4][4];
      this.glTexture = new GpuTextureAndView[5];
      this.refreshRequired = new boolean[5];
      this.buffer = new IntBuffer[5];
      this.bufferBase = new ByteBuffer[5];
   }

   public void reset(int X, int Z) {
      this.X = X;
      this.Z = Z;
      this.hasSomething = false;

      for(int i = 0; i < this.glTexture.length; ++i) {
         this.glTexture[i] = null;
         this.refreshRequired[i] = false;
         if (this.buffer[i] != null) {
            this.buffer[i].clear();
         }
      }

      this.refreshed = false;
      this.changed = false;
      this.levelsBuffered = 0;

      for(int i = 0; i < this.tiles.length; ++i) {
         for(int j = 0; j < this.tiles.length; ++j) {
            this.tiles[i][j] = null;
         }
      }

      this.blockTextureUpload = false;
   }

   public void recycleTiles() {
      for(int i = 0; i < this.tiles.length; ++i) {
         for(int j = 0; j < this.tiles.length; ++j) {
            MinimapTile tile = this.tiles[i][j];
            if (tile != null) {
               if (!tile.isWasTransfered()) {
                  tile.recycle();
               } else {
                  tile.setWasTransfered(false);
               }
            }
         }
      }

   }

   public int getLevelToRefresh(int currentLevel) {
      if (!this.refreshed && this.levelsBuffered != 0 && currentLevel != -1) {
         int prev = currentLevel - 1;
         if (prev < 0) {
            prev = this.levelsBuffered - 1;
         }

         int i;
         for(i = currentLevel; !this.refreshRequired[i]; i = (i + 1) % this.levelsBuffered) {
            if (i == prev) {
               this.refreshed = true;
               return -1;
            }
         }

         return i;
      } else {
         return -1;
      }
   }

   public GpuTextureAndView bindTexture(int level) {
      synchronized(this) {
         if (!this.hasSomething) {
            return null;
         } else {
            if (!this.blockTextureUpload) {
               int levelToRefresh = this.getLevelToRefresh(Math.min(level, this.levelsBuffered - 1));
               if (levelToRefresh != -1) {
                  boolean result = false;
                  if (this.glTexture[levelToRefresh] == null) {
                     GpuTexture texture = RenderSystem.getDevice().createTexture((String)null, 5, TextureFormat.RGBA8, 64, 64, 1, 1);
                     GpuTextureView view = RenderSystem.getDevice().createTextureView(texture);
                     this.glTexture[levelToRefresh] = new GpuTextureAndView(texture, view);
                     result = true;
                  }

                  RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.glTexture[levelToRefresh].texture, this.bufferBase[levelToRefresh], class_1012.field_4997, 0, 0, 0, 0, 64, 64);
                  this.refreshRequired[levelToRefresh] = false;
               }
            }

            GpuTextureAndView levelTexture = this.glTexture[level];
            return levelTexture;
         }
      }
   }

   public void updateBuffers(int levelsToLoad) {
      this.refreshed = true;

      for(int l = 0; l < levelsToLoad; ++l) {
         this.refreshRequired[l] = false;
         if (this.buffer[l] == null) {
            this.bufferBase[l] = TextureUtils.allocateByteBuffer(16384, ByteOrder.LITTLE_ENDIAN);
            this.buffer[l] = this.bufferBase[l].asIntBuffer();
         }
      }

      for(int o = 0; o < this.tiles.length; ++o) {
         int offX = o * 16;

         for(int p = 0; p < this.tiles.length; ++p) {
            MinimapTile tile = this.tiles[o][p];
            int offZ = p * 16;

            for(int z = 0; z < 16; ++z) {
               for(int x = 0; x < 16; ++x) {
                  for(int i = 0; i < levelsToLoad; ++i) {
                     if (tile == null) {
                        this.putColour(offX + x, offZ + z, 0, 0, 0, 0, this.buffer[i], 64);
                     } else {
                        this.putColour(offX + x, offZ + z, tile.getRed(i, x, z), tile.getGreen(i, x, z), tile.getBlue(i, x, z), 255, this.buffer[i], 64);
                     }
                  }
               }
            }
         }
      }

      for(int i = 0; i < levelsToLoad; ++i) {
         this.buffer[i].clear();
         this.buffer[i].limit(4096);
         this.refreshRequired[i] = true;
      }

      this.refreshed = false;
   }

   public void putColour(int x, int y, int red, int green, int blue, int alpha, IntBuffer buffer, int size) {
      int pos = y * size + x;
      buffer.put(pos, alpha << 24 | blue << 16 | green << 8 | red);
   }

   public void copyBuffer(int level, IntBuffer toCopy) {
      if (this.buffer[level] == null) {
         this.bufferBase[level] = TextureUtils.allocateByteBuffer(16384, ByteOrder.LITTLE_ENDIAN);
         this.buffer[level] = this.bufferBase[level].asIntBuffer();
      } else {
         this.buffer[level].clear();
      }

      this.buffer[level].put(toCopy);
      this.buffer[level].flip();
   }

   public int getLevelsBuffered() {
      return this.levelsBuffered;
   }

   public boolean isHasSomething() {
      return this.hasSomething;
   }

   public void setHasSomething(boolean hasSomething) {
      this.hasSomething = hasSomething;
   }

   public int getX() {
      return this.X;
   }

   public int getZ() {
      return this.Z;
   }

   public GpuTextureAndView getGlTexture(int l) {
      return this.glTexture[l];
   }

   public void setGlTexture(int l, GpuTextureAndView t) {
      this.glTexture[l] = t;
   }

   public MinimapTile getTile(int x, int z) {
      return this.tiles[x][z];
   }

   public void setTile(int x, int z, MinimapTile t) {
      this.tiles[x][z] = t;
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void setChanged(boolean changed) {
      this.changed = changed;
   }

   public void setLevelsBuffered(int levelsBuffered) {
      this.levelsBuffered = levelsBuffered;
   }

   public boolean isBlockTextureUpload() {
      return this.blockTextureUpload;
   }

   public void setBlockTextureUpload(boolean blockTextureUpload) {
      this.blockTextureUpload = blockTextureUpload;
   }

   public boolean isRefreshRequired(int l) {
      return this.refreshRequired[l];
   }

   public void setRefreshRequired(int l, boolean r) {
      this.refreshRequired[l] = r;
   }

   public IntBuffer getBuffer(int l) {
      return this.buffer[l];
   }

   public void cleanup(Minimap minimap) {
      for(int l = 0; l < this.glTexture.length; ++l) {
         if (this.glTexture[l] != null) {
            this.glTexture[l].close();
         }
      }

   }
}
