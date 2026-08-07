package xaero.map.region.texture;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.class_1959;
import net.minecraft.class_5321;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.biome.BlockTintProvider;
import xaero.map.cache.BlockStateShortShapeCache;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.exception.OpenGLException;
import xaero.map.file.IOHelper;
import xaero.map.graphics.OpenGlHelper;
import xaero.map.graphics.TextureUploader;
import xaero.map.highlight.DimensionHighlighterHandler;
import xaero.map.misc.ConsistentBitArray;
import xaero.map.palette.FastIntPalette;
import xaero.map.palette.Paletted2DFastBitArrayIntStorage;
import xaero.map.pool.buffer.PoolTextureDirectBufferUnit;
import xaero.map.region.BranchLeveledRegion;
import xaero.map.region.LeveledRegion;
import xaero.map.region.MapRegion;
import xaero.map.region.MapUpdateFastConfig;
import xaero.map.region.OverlayManager;

public abstract class RegionTexture<T extends RegionTexture<T>> {
   public static final TextureFormat DEFAULT_INTERNAL_FORMAT;
   public static final int PBO_UNPACK_LENGTH = 16384;
   public static final int PBO_PACK_LENGTH = 16384;
   private static final long[] ONE_BIOME_PALETTE_DATA;
   private static final ThreadLocal<ConsistentBitArray> OLD_HEIGHT_VALUES_SUPPORT;
   protected int textureVersion;
   protected GpuTextureAndView glColorTexture = null;
   protected boolean textureHasLight;
   protected PoolTextureDirectBufferUnit colorBuffer;
   protected boolean bufferHasLight;
   protected TextureFormat colorBufferFormat = null;
   protected int bufferedTextureVersion;
   protected GpuBuffer packPbo;
   protected GpuBuffer[] unpackPbo = new GpuBuffer[2];
   protected boolean shouldDownloadFromPBO;
   protected int timer;
   private boolean cachePrepared;
   protected boolean toUpload;
   protected LeveledRegion<T> region;
   protected ConsistentBitArray heightValues;
   protected ConsistentBitArray topHeightValues;
   protected RegionTextureBiomes biomes;

   public RegionTexture(LeveledRegion<T> region) {
      this.region = region;
      this.bufferedTextureVersion = this.textureVersion = -1;
      this.heightValues = new ConsistentBitArray(13, 4096);
      this.topHeightValues = new ConsistentBitArray(13, 4096);
   }

   private void setupTextureParameters() {
   }

   public void prepareBuffer() {
      if (this.colorBuffer != null) {
         this.colorBuffer.reset();
      } else {
         this.colorBuffer = WorldMap.textureDirectBufferPool.get(true);
      }

   }

   protected int getMipMapLevels() {
      return 1;
   }

   public GpuTextureAndView bindColorTexture(boolean create) {
      boolean result = false;
      GpuTextureAndView texture = this.glColorTexture;
      if (texture == null) {
         if (!create) {
            return null;
         }

         int levels = this.getMipMapLevels();
         GpuTexture createdTexture = RenderSystem.getDevice().createTexture((String)null, 15, DEFAULT_INTERNAL_FORMAT, 64, 64, 1, levels);
         texture = this.glColorTexture = new GpuTextureAndView(createdTexture, RenderSystem.getDevice().createTextureView(createdTexture));
         result = true;
      }

      if (result) {
         this.setupTextureParameters();
      }

      return texture;
   }

   public long uploadBuffer(DimensionHighlighterHandler highlighterHandler, TextureUploader textureUploader, LeveledRegion<T> inRegion, BranchTextureRenderer branchTextureRenderer, int x, int y) throws OpenGLException, IllegalArgumentException, IllegalAccessException {
      long result = this.uploadBufferHelper(highlighterHandler, textureUploader, inRegion, branchTextureRenderer);
      if (!this.shouldDownloadFromPBO()) {
         this.setToUpload(false);
         if (this.getColorBufferFormat() == null) {
            this.deleteColorBuffer();
         } else {
            this.setCachePrepared(true);
         }
      }

      return result;
   }

   public void postBufferWrite(PoolTextureDirectBufferUnit buffer) {
   }

   private long uploadBufferHelper(DimensionHighlighterHandler highlighterHandler, TextureUploader textureUploader, LeveledRegion<T> inRegion, BranchTextureRenderer branchTextureRenderer) throws OpenGLException, IllegalArgumentException, IllegalAccessException {
      return this.uploadBufferHelper(highlighterHandler, textureUploader, inRegion, branchTextureRenderer, false);
   }

   private long uploadBufferHelper(DimensionHighlighterHandler highlighterHandler, TextureUploader textureUploader, LeveledRegion<T> inRegion, BranchTextureRenderer branchTextureRenderer, boolean retrying) throws OpenGLException, IllegalArgumentException, IllegalAccessException {
      if (this.colorBufferFormat != null) {
         PoolTextureDirectBufferUnit colorBufferToUpload = this.applyHighlights(highlighterHandler, this.colorBuffer, true);
         this.updateTextureVersion(this.bufferedTextureVersion);
         if (colorBufferToUpload == null) {
            return 0L;
         } else {
            this.writeToUnpackPBO(0, colorBufferToUpload);
            TextureFormat internalFormat = this.colorBufferFormat;
            this.textureHasLight = this.bufferHasLight;
            this.colorBufferFormat = null;
            this.bufferedTextureVersion = -1;
            boolean subsequent = this.glColorTexture != null;
            this.bindColorTexture(true);
            long totalEstimatedTime = 0L;
            if (this.unpackPbo[0] == null) {
               return 0L;
            } else {
               if (subsequent) {
                  totalEstimatedTime = textureUploader.requestSubsequentNormal(this.glColorTexture, this.unpackPbo[0], 0, 64, 64, 0, 0L, 0, 0);
               } else {
                  totalEstimatedTime = textureUploader.requestNormal(this.glColorTexture, this.unpackPbo[0], 0, internalFormat, 64, 64, 0, 0L);
               }

               this.onCacheUploadRequested();
               return totalEstimatedTime;
            }
         }
      } else if (!this.shouldDownloadFromPBO) {
         return this.uploadNonCache(highlighterHandler, textureUploader, branchTextureRenderer);
      } else {
         this.ensurePackPBO();
         if (this.packPbo == null) {
            this.onDownloadedBuffer((ByteBuffer)null);
            this.endPBODownload(DEFAULT_INTERNAL_FORMAT, false);
            return 0L;
         } else {
            GpuBuffer.MappedView mappedPBO;
            try {
               mappedPBO = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.packPbo, true, false);
            } catch (Throwable t) {
               WorldMap.LOGGER.warn("Exception trying to map PBO", t);
               mappedPBO = null;
            }

            if (mappedPBO == null) {
               this.unbindPackPBO();
               WorldMap.LOGGER.warn("Failed to map PBO {} {} (uploadBufferHelper).", this.packPbo, retrying);
               this.packPbo.close();
               OpenGlHelper.clearErrors(true, "uploadBufferHelper");
               this.packPbo = null;
               if (retrying) {
                  this.onDownloadedBuffer((ByteBuffer)null);
                  this.endPBODownload(DEFAULT_INTERNAL_FORMAT, false);
                  return 0L;
               } else {
                  return this.uploadBufferHelper(highlighterHandler, textureUploader, inRegion, branchTextureRenderer, true);
               }
            } else {
               OpenGLException.checkGLError();
               this.onDownloadedBuffer(mappedPBO.data());
               mappedPBO.close();
               OpenGLException.checkGLError();
               this.unbindPackPBO();
               OpenGLException.checkGLError();
               this.endPBODownload(DEFAULT_INTERNAL_FORMAT, true);
               return 0L;
            }
         }
      }
   }

   protected PoolTextureDirectBufferUnit applyHighlights(DimensionHighlighterHandler highlighterHandler, PoolTextureDirectBufferUnit colorBuffer, boolean separateBuffer) {
      return colorBuffer;
   }

   protected void onDownloadedBuffer(ByteBuffer mappedPBO) {
      ByteBuffer directBuffer = this.colorBuffer.getDirectBuffer();
      directBuffer.clear();
      if (mappedPBO != null) {
         directBuffer.put(mappedPBO);
         directBuffer.flip();
      } else {
         directBuffer.limit(16384);
      }

   }

   protected void endPBODownload(TextureFormat format, boolean success) {
      this.bufferHasLight = this.textureHasLight;
      this.colorBufferFormat = format;
      this.shouldDownloadFromPBO = false;
      this.bufferedTextureVersion = this.textureVersion;
      if (format == null) {
         throw new RuntimeException("Invalid texture internal format returned by the driver.");
      }
   }

   protected void ensurePackPBO() {
      if (this.packPbo == null) {
         this.packPbo = RenderSystem.getDevice().createBuffer((Supplier)null, 29, 16384L);
      }
   }

   private void ensureUnpackPBO(int index) {
      if (this.unpackPbo[index] == null) {
         this.unpackPbo[index] = RenderSystem.getDevice().createBuffer((Supplier)null, 30, 16384L);
      }
   }

   protected void unbindPackPBO() {
      OpenGlHelper.unbindPackBuffer();
   }

   private void unbindUnpackPBO() {
      OpenGlHelper.unbindUnpackBuffer();
   }

   protected void writeToUnpackPBO(int pboIndex, PoolTextureDirectBufferUnit buffer) throws OpenGLException {
      this.ensureUnpackPBO(pboIndex);
      if (this.unpackPbo[pboIndex] == null) {
         this.postBufferWrite(buffer);
      } else {
         OpenGlHelper.clearErrors(false, (String)null);
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.unpackPbo[pboIndex].slice(), buffer.getDirectBuffer());
         OpenGlHelper.clearErrors(true, "writeToUnpackPBO");
         this.postBufferWrite(buffer);
      }
   }

   public void deleteColorBuffer() {
      if (this.colorBuffer != null) {
         if (!WorldMap.textureDirectBufferPool.addToPool(this.colorBuffer)) {
            WorldMap.bufferDeallocator.deallocate(this.colorBuffer.getDirectBuffer(), WorldMapClientConfigUtils.getDebug());
         }

         this.colorBuffer = null;
      }

      this.colorBufferFormat = null;
      this.bufferedTextureVersion = -1;
   }

   public void deletePBOs() {
      if (this.packPbo != null) {
         WorldMap.gpuObjectDeleter.requestBufferToDelete(this.packPbo);
      }

      this.packPbo = null;

      for(int i = 0; i < this.unpackPbo.length; ++i) {
         if (this.unpackPbo[i] != null) {
            WorldMap.gpuObjectDeleter.requestBufferToDelete(this.unpackPbo[i]);
            this.unpackPbo[i] = null;
         }
      }

   }

   public void writeCacheMapData(DataOutputStream output, byte[] usableBuffer, byte[] integerByteBuffer, LeveledRegion<T> inRegion) throws IOException {
      ByteBuffer directBuffer = this.colorBuffer.getDirectBuffer();
      directBuffer.get(usableBuffer, 0, 16384);
      directBuffer.position(0);
      output.write(usableBuffer, 0, 16384);
      output.writeBoolean(this.bufferHasLight);
      long[] heightData = this.heightValues.getData();

      for(int i = 0; i < heightData.length; ++i) {
         output.writeLong(heightData[i]);
      }

      long[] topHeightData = this.topHeightValues.getData();

      for(int i = 0; i < topHeightData.length; ++i) {
         output.writeLong(topHeightData[i]);
      }

      this.saveBiomeIndexStorage(output);
   }

   public void readCacheData(int minorSaveVersion, int majorSaveVersion, DataInputStream input, byte[] usableBuffer, byte[] integerByteBuffer, LeveledRegion<T> inRegion, MapProcessor mapProcessor, int x, int y, boolean leafShouldAffectBranches) throws IOException {
      if (minorSaveVersion >= 7 && (minorSaveVersion < 9 || minorSaveVersion > 11)) {
         this.bufferedTextureVersion = inRegion.getAndResetCachedTextureVersion(x, y);
      } else {
         this.bufferedTextureVersion = 1;
      }

      if (minorSaveVersion == 6) {
         input.readInt();
      }

      int lightLevelsInCache = minorSaveVersion < 3 ? 4 : 1;

      for(int i = 0; i < lightLevelsInCache; ++i) {
         boolean colorBufferCompressed = false;
         if (i == 0) {
            if (majorSaveVersion < 2) {
               colorBufferCompressed = true;
               if (minorSaveVersion > 1) {
                  colorBufferCompressed = input.read() == 1;
               }

               input.readInt();
            }
         } else {
            if (minorSaveVersion > 1) {
               input.read();
            }

            input.readInt();
         }

         int length = 16384;
         if (majorSaveVersion < 2) {
            length = input.readInt();
         }

         IOHelper.readToBuffer(usableBuffer, length, input);
         if (i == 0) {
            this.colorBufferFormat = DEFAULT_INTERNAL_FORMAT;
            if (inRegion.getLevel() == 0 && colorBufferCompressed) {
               if (this.colorBuffer == null) {
                  this.colorBuffer = WorldMap.textureDirectBufferPool.get(true);
               }

               inRegion.setShouldCache(true, "ignoring compressed textures");
               this.colorBuffer.getDirectBuffer().limit(16384);
            } else {
               if (this.colorBuffer == null) {
                  this.colorBuffer = WorldMap.textureDirectBufferPool.get(false);
               }

               ByteBuffer directBuffer = this.colorBuffer.getDirectBuffer();
               directBuffer.put(usableBuffer, 0, length);
               directBuffer.flip();
            }
         }
      }

      if (minorSaveVersion >= 14) {
         this.bufferHasLight = input.readBoolean();
      } else if (minorSaveVersion > 2) {
         int lightLength = input.readInt();
         if (lightLength > 0) {
            IOHelper.readToBuffer(usableBuffer, lightLength, input);
         }

         this.bufferHasLight = false;
      }

      if (minorSaveVersion >= 13) {
         long[] heightData = new long[majorSaveVersion == 0 ? 586 : 1024];

         for(int i = 0; i < heightData.length; ++i) {
            heightData[i] = input.readLong();
         }

         if (majorSaveVersion == 0) {
            ConsistentBitArray oldHeightArray = (ConsistentBitArray)OLD_HEIGHT_VALUES_SUPPORT.get();
            oldHeightArray.setData(heightData);

            for(int i = 0; i < 4096; ++i) {
               int oldValue = oldHeightArray.get(i);
               if (oldValue >> 8 != 0) {
                  this.putHeight(i, oldValue & 255);
               }
            }
         } else {
            this.heightValues.setData(heightData);
         }

         if (minorSaveVersion < 17) {
            long[] copyFrom = this.heightValues.getData();
            long[] topHeightData = new long[this.topHeightValues.getData().length];
            System.arraycopy(copyFrom, 0, topHeightData, 0, copyFrom.length);
            this.topHeightValues.setData(topHeightData);
         } else {
            long[] topHeightData = new long[majorSaveVersion == 0 ? 586 : 1024];

            for(int i = 0; i < topHeightData.length; ++i) {
               topHeightData[i] = input.readLong();
            }

            if (majorSaveVersion == 0) {
               ConsistentBitArray oldHeightArray = (ConsistentBitArray)OLD_HEIGHT_VALUES_SUPPORT.get();
               oldHeightArray.setData(topHeightData);

               for(int i = 0; i < 4096; ++i) {
                  int oldValue = oldHeightArray.get(i);
                  if (oldValue >> 8 != 0) {
                     this.putTopHeight(i, oldValue & 255);
                  }
               }
            } else {
               this.topHeightValues.setData(topHeightData);
            }
         }

         this.loadBiomeIndexStorage(input, minorSaveVersion, majorSaveVersion);
         if (minorSaveVersion == 16) {
            for(int i = 0; i < 64; ++i) {
               input.readLong();
            }
         }
      }

      this.toUpload = true;
   }

   private void saveBiomeIndexStorage(DataOutputStream output) throws IOException {
      Paletted2DFastBitArrayIntStorage biomeIndexStorage = this.biomes == null ? null : this.biomes.getBiomeIndexStorage();
      int paletteSize = biomeIndexStorage == null ? 0 : biomeIndexStorage.getPaletteSize();
      if (paletteSize > 0) {
         if (this.region.getBiomePalette() == null) {
            throw new RuntimeException("saving biomes for a texture in a biomeless region");
         }

         if (biomeIndexStorage.getPaletteNonNullCount() <= 1 && biomeIndexStorage.getDefaultValueCount() == 0) {
            int paletteElement = biomeIndexStorage.getPaletteElement(paletteSize - 1);
            int paletteElementCount = biomeIndexStorage.getPaletteElementCount(paletteSize - 1);
            output.writeInt(1);
            output.writeInt(paletteElement);
            output.writeShort(paletteElementCount);
            output.write(0);
         } else {
            output.writeInt(paletteSize);

            for(int i = 0; i < paletteSize; ++i) {
               int paletteElement = biomeIndexStorage.getPaletteElement(i);
               output.writeInt(paletteElement);
               if (paletteElement != -1) {
                  output.writeShort(biomeIndexStorage.getPaletteElementCount(i));
               }
            }

            output.write(1);
            biomeIndexStorage.writeData(output);
         }
      } else {
         output.writeInt(0);
      }

   }

   private void loadBiomeIndexStorage(DataInputStream input, int minorSaveVersion, int majorSaveVersion) throws IOException {
      if (minorSaveVersion >= 19) {
         int paletteSize = input.readInt();
         if (paletteSize > 0) {
            int defaultValueCount = 4096;
            FastIntPalette fastIntPalette = FastIntPalette.Builder.begin().setMaxCountPerElement(4096).build();

            for(int i = 0; i < paletteSize; ++i) {
               int paletteElementValue = input.readInt();
               if (paletteElementValue == -1) {
                  fastIntPalette.addNull();
               } else {
                  int count = input.readShort() & '\uffff';
                  fastIntPalette.append(paletteElementValue, count);
                  defaultValueCount -= count;
               }
            }

            long[] data = new long[1024];
            if (minorSaveVersion != 19 && input.read() != 1) {
               System.arraycopy(ONE_BIOME_PALETTE_DATA, 0, data, 0, data.length);
            } else {
               for(int i = 0; i < data.length; ++i) {
                  data[i] = input.readLong();
               }
            }

            ConsistentBitArray dataStorage = new ConsistentBitArray(13, 4096, data);
            Paletted2DFastBitArrayIntStorage biomeIndexStorage = Paletted2DFastBitArrayIntStorage.Builder.begin().setPalette(fastIntPalette).setData(dataStorage).setWidth(64).setHeight(64).setDefaultValueCount(defaultValueCount).setMaxPaletteElements(4096).build();
            if (this.region.getBiomePalette() != null) {
               for(int i = 0; i < fastIntPalette.getSize(); ++i) {
                  int paletteElement = fastIntPalette.get(i, -1);
                  if (paletteElement != -1) {
                     this.region.getBiomePalette().count(paletteElement, true);
                  }
               }

               this.biomes = new RegionTextureBiomes(biomeIndexStorage, this.region.getBiomePalette());
            }
         }
      }

   }

   public void deleteTexturesAndBuffers() {
      GpuTextureAndView textureToDelete = this.getGlColorTexture();
      this.glColorTexture = null;
      if (textureToDelete != null) {
         WorldMap.gpuObjectDeleter.requestTextureDeletion(textureToDelete);
      }

      this.onTextureDeletion();
      if (this.getColorBuffer() != null) {
         this.deleteColorBuffer();
      }

      this.deletePBOs();
   }

   public PoolTextureDirectBufferUnit getColorBuffer() {
      return this.colorBuffer;
   }

   public ByteBuffer getDirectColorBuffer() {
      return this.colorBuffer == null ? null : this.colorBuffer.getDirectBuffer();
   }

   public void setShouldDownloadFromPBO(boolean shouldDownloadFromPBO) {
      this.shouldDownloadFromPBO = shouldDownloadFromPBO;
   }

   public TextureFormat getColorBufferFormat() {
      return this.colorBufferFormat;
   }

   public boolean shouldDownloadFromPBO() {
      return this.shouldDownloadFromPBO;
   }

   public int getTimer() {
      return this.timer;
   }

   public void decTimer() {
      --this.timer;
   }

   public void resetTimer() {
      this.timer = 0;
   }

   public final GpuTextureAndView getGlColorTexture() {
      return this.glColorTexture;
   }

   public void onTextureDeletion() {
      this.updateTextureVersion(0);
   }

   public boolean shouldUpload() {
      return this.toUpload;
   }

   public void setToUpload(boolean value) {
      this.toUpload = value;
   }

   public boolean isCachePrepared() {
      return this.cachePrepared;
   }

   public void setCachePrepared(boolean cachePrepared) {
      this.cachePrepared = cachePrepared;
   }

   public boolean canUpload() {
      return true;
   }

   public boolean isUploaded() {
      return !this.shouldUpload();
   }

   public int getTextureVersion() {
      return this.textureVersion;
   }

   public int getBufferedTextureVersion() {
      return this.bufferedTextureVersion;
   }

   public void setBufferedTextureVersion(int bufferedTextureVersion) {
      this.bufferedTextureVersion = bufferedTextureVersion;
   }

   public LeveledRegion<T> getRegion() {
      return this.region;
   }

   protected void updateTextureVersion(int newVersion) {
      this.textureVersion = newVersion;
   }

   public int getHeight(int x, int z) {
      int index = (z << 6) + x;
      int value = this.heightValues.get(index);
      return value >> 12 == 0 ? 32767 : (value & 4095) << 20 >> 20;
   }

   public void putHeight(int x, int z, int height) {
      int index = (z << 6) + x;
      this.putHeight(index, height);
   }

   public void putHeight(int index, int height) {
      int value = 4096 | height & 4095;
      this.heightValues.set(index, value);
   }

   public void removeHeight(int x, int z) {
      int index = (z << 6) + x;
      this.heightValues.set(index, 0);
   }

   public int getTopHeight(int x, int z) {
      int index = (z << 6) + x;
      int value = this.topHeightValues.get(index);
      return value >> 12 == 0 ? 32767 : (value & 4095) << 20 >> 20;
   }

   public void putTopHeight(int x, int z, int height) {
      int index = (z << 6) + x;
      this.putTopHeight(index, height);
   }

   public void putTopHeight(int index, int height) {
      int value = 4096 | height & 4095;
      this.topHeightValues.set(index, value);
   }

   public void removeTopHeight(int x, int z) {
      int index = (z << 6) + x;
      this.topHeightValues.set(index, 0);
   }

   public void ensureBiomeIndexStorage() {
      if (this.biomes == null) {
         Paletted2DFastBitArrayIntStorage biomeIndexStorage = Paletted2DFastBitArrayIntStorage.Builder.begin().setMaxPaletteElements(4096).setDefaultValue(-1).setWidth(64).setHeight(64).build();
         this.region.ensureBiomePalette();
         this.biomes = new RegionTextureBiomes(biomeIndexStorage, this.region.getBiomePalette());
      }

   }

   public class_5321<class_1959> getBiome(int x, int z) {
      RegionTextureBiomes biomes = this.biomes;
      if (biomes == null) {
         return null;
      } else {
         int biomePaletteIndex = biomes.getBiomeIndexStorage().get(x, z);
         return biomePaletteIndex == -1 ? null : (class_5321)biomes.getRegionBiomePalette().get(biomePaletteIndex);
      }
   }

   public void setBiome(int x, int z, class_5321<class_1959> biome) {
      this.ensureBiomeIndexStorage();
      Paletted2DFastBitArrayIntStorage biomeIndexStorage = this.biomes.getBiomeIndexStorage();
      int currentBiomePaletteIndex = biomeIndexStorage.get(x, z);
      int biomePaletteIndex = biome == null ? -1 : this.region.getBiomePaletteIndex(biome);
      if (biome == null || biomePaletteIndex != -1 && biomeIndexStorage.contains(biomePaletteIndex)) {
         if (biomePaletteIndex == currentBiomePaletteIndex) {
            return;
         }
      } else {
         biomePaletteIndex = this.region.onBiomeAddedToTexture(biome);
      }

      try {
         biomeIndexStorage.set(x, z, biomePaletteIndex);
      } catch (Throwable t) {
         WorldMap.LOGGER.error("weird biomes " + String.valueOf(this.region) + " pixel x:" + x + " z:" + z + " " + currentBiomePaletteIndex + " " + biomePaletteIndex, t);

         for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
               if (this.region.getTexture(i, j) == this) {
                  WorldMap.LOGGER.info("texture " + i + " " + j);
               }
            }
         }

         WorldMap.LOGGER.error(biomeIndexStorage.getBiomePaletteDebug());
         int[] realCounts = new int[biomeIndexStorage.getPaletteSize()];

         for(int p = 0; p < 64; ++p) {
            String line = "";

            for(int o = 0; o < 64; ++o) {
               int rawIndex = biomeIndexStorage.getRaw(o, p) - 1;
               line = line + " " + rawIndex;
               if (rawIndex >= 0 && rawIndex < realCounts.length) {
                  int var10002 = realCounts[rawIndex]++;
               }
            }

            WorldMap.LOGGER.error(line);
         }

         WorldMap.LOGGER.error("real counts: " + Arrays.toString(realCounts));
         WorldMap.LOGGER.error("suppressed exception", t);
         this.region.setShouldCache(true, "broken cache biome data");
         if (this.region.getLevel() > 0) {
            this.textureVersion = (new Random()).nextInt();
            ((BranchLeveledRegion)this.region).setShouldCheckForUpdatesRecursive(true);
         } else {
            ((MapRegion)this.region).setCacheHashCode(0);
         }

         this.biomes = null;
      }

      if (currentBiomePaletteIndex != -1 && !biomeIndexStorage.contains(currentBiomePaletteIndex)) {
         this.region.onBiomeRemovedFromTexture(currentBiomePaletteIndex);
      }

   }

   public boolean getTextureHasLight() {
      return this.textureHasLight;
   }

   public void addDebugLines(List<String> debugLines) {
      boolean var10001 = this.shouldUpload();
      debugLines.add("shouldUpload: " + var10001 + " timer: " + this.getTimer());
      debugLines.add(String.format("buffer exists: %s", this.getColorBuffer() != null));
      String var2 = String.valueOf(this.getGlColorTexture());
      debugLines.add("glColorTexture: " + var2 + " textureHasLight: " + this.textureHasLight);
      debugLines.add("cachePrepared: " + this.isCachePrepared());
      debugLines.add("textureVersion: " + this.textureVersion);
      debugLines.add("colorBufferFormat: " + String.valueOf(this.colorBufferFormat));
      if (this.biomes != null) {
         debugLines.add(this.biomes.getBiomeIndexStorage().getBiomePaletteDebug());
      }

   }

   protected void onCacheUploadRequested() {
   }

   public boolean shouldBeUsedForBranchUpdate(int usedVersion) {
      return (this.shouldHaveContentForBranchUpdate() ? this.textureVersion : 0) != usedVersion;
   }

   public boolean shouldHaveContentForBranchUpdate() {
      return true;
   }

   public boolean shouldIncludeInCache() {
      return true;
   }

   public RegionTextureBiomes getBiomes() {
      return this.biomes;
   }

   public void resetBiomes() {
      this.biomes = null;
   }

   public abstract boolean hasSourceData();

   public abstract void preUpload(MapProcessor var1, BlockTintProvider var2, OverlayManager var3, LeveledRegion<T> var4, boolean var5, BlockStateShortShapeCache var6, MapUpdateFastConfig var7);

   public abstract void postUpload(MapProcessor var1, LeveledRegion<T> var2, boolean var3);

   protected abstract long uploadNonCache(DimensionHighlighterHandler var1, TextureUploader var2, BranchTextureRenderer var3);

   public boolean getBufferHasLight() {
      return this.bufferHasLight;
   }

   static {
      DEFAULT_INTERNAL_FORMAT = TextureFormat.RGBA8;
      ConsistentBitArray dataStorage = new ConsistentBitArray(13, 4096);

      for(int i = 0; i < 4096; ++i) {
         dataStorage.set(i, 1);
      }

      ONE_BIOME_PALETTE_DATA = dataStorage.getData();
      OLD_HEIGHT_VALUES_SUPPORT = ThreadLocal.withInitial(() -> new ConsistentBitArray(9, 4096));
   }
}
