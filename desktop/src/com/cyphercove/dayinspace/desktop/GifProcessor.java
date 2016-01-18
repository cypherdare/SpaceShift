/*******************************************************************************
 * Copyright 2015 Cypher Cove LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.dayinspace.desktop;

import com.badlogic.gdx.tools.*;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * Take all gif files along with potential .alpha counterparts, and write png images
 * to destination in like directories.
 */
public class GifProcessor extends FileProcessor {
    private static final float GIF_DELAYTIME_TO_SECONDS = 0.01f;
    private static final Color TRANSPARENT = new Color(0x00000000, true);
    private float maxAnimationDelayError;
    private ArrayList<File> generatedFiles = new ArrayList<File>();

    public GifProcessor (float maxAnimationDelayError){
        this.maxAnimationDelayError = maxAnimationDelayError;

        addInputSuffix(".gif");
        setInputFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.toLowerCase().endsWith(".alpha.gif");
            }
        });
    }

    @Override
    public ArrayList<Entry> process (File inputFile, File outputRoot) throws Exception {
        generatedFiles.clear();
        return super.process(inputFile, outputRoot);
    }

    @Override
    protected void processFile (Entry entry) throws Exception {
        File file = entry.inputFile;
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        String name = (dotIndex != -1) ? fileName.substring(0, dotIndex) : fileName;
        String alphaFileName = name + ".alpha.gif";
        File alphaFile = new File(file.getParent(), alphaFileName);
        if (!alphaFile.exists()){
            alphaFileName = name + ".alpha.GIF";
            alphaFile = new File(file.getParent(), alphaFileName);
        }

        LinkedHashMap<BufferedImage, Float> images = getGifImages(file, true);
        ArrayList<BufferedImage> alphaImages = null;
        if (alphaFile.exists()){
            LinkedHashMap<BufferedImage, Float> alphaImagesMap = getGifImages(alphaFile, false);
            alphaImages = new ArrayList<BufferedImage>(alphaImagesMap.size());
            for (BufferedImage image : alphaImagesMap.keySet()){
                alphaImages.add(image);
            }
        }

        boolean oneFrame = images.size()==1;
        float delay = 1;
        if (!oneFrame) {
            Float[] delays = new Float[images.size()];
            delay = FloatingPointGCD.findFloatingPointGCD(
                    images.values().toArray(delays), maxAnimationDelayError);
            System.out.println("Animation \"" + name + "\" uses delay of " + delay);
        }

        int index = -1;
        int imageNum = 0;
        for (Map.Entry<BufferedImage, Float> imageEntry : images.entrySet()){
            BufferedImage image = imageEntry.getKey();
            if (alphaImages!=null){
                applyGrayscaleMaskToAlpha(image, alphaImages.get(imageNum));
            }

            int count = oneFrame ? 1 : Math.round(imageEntry.getValue() / delay);
            for (int i = 0; i < count; i++) {
                index++;
                if (!entry.outputDir.exists()) entry.outputDir.mkdir();
                File outputFile = new File(entry.outputDir, oneFrame? name + ".png" : name + "_" + index + ".png");
                ImageIO.write(image, "png", outputFile);
                generatedFiles.add(outputFile);
            }

            imageNum++;
        }

    }

    public ArrayList<File> getGeneratedFiles() {
        return generatedFiles;
    }

    /** From http://stackoverflow.com/a/8058442/506796 */
    public void applyGrayscaleMaskToAlpha(BufferedImage image, BufferedImage mask)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width);
        int[] maskPixels = mask.getRGB(0, 0, width, height, null, 0, width);

        for (int i = 0; i < imagePixels.length; i++)
        {
            int color = imagePixels[i] & 0x00ffffff; // Mask preexisting alpha
            int alpha = maskPixels[i] << 24; // Shift green to alpha
            imagePixels[i] = color | alpha;
        }

        image.setRGB(0, 0, width, height, imagePixels, 0, width);
    }

    private LinkedHashMap<BufferedImage, Float> getGifImages (File file, boolean withDelayTimes){
        ImageReader imageReader = null;
        LinkedHashMap<BufferedImage, Float> images = new LinkedHashMap<BufferedImage, Float>();
        try {
            ImageInputStream inputStream = ImageIO.createImageInputStream(file);
            imageReader = ImageIO.getImageReaders(inputStream).next();
            imageReader.setInput(inputStream);
            int numImages = imageReader.getNumImages(true);
            Image generalImage = ImageIO.read(file);
            int width = generalImage.getWidth(null);
            int height = generalImage.getHeight(null);
            //start with blank image of the complete size, in case first frame isn't full size (possible?)
            BufferedImage previousImage =
                    new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            for (int i = 0; i < numImages; i++) {
                IIOMetadata imageMetaData = imageReader.getImageMetadata(i);
                String metaFormatName = imageMetaData.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
                IIOMetadataNode imageDescriptorNode = getMetaDataNode(root, "ImageDescriptor");
                int top = Integer.parseInt(imageDescriptorNode.getAttribute("imageTopPosition"));
                int left = Integer.parseInt(imageDescriptorNode.getAttribute("imageLeftPosition"));
                BufferedImage image = imageReader.read(i);
                BufferedImage combinedImage =
                        new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D graphics = combinedImage.createGraphics();
                graphics.drawImage(previousImage, 0, 0, null);
                graphics.setBackground(TRANSPARENT);
                graphics.clearRect(left, top, image.getWidth(), image.getHeight());
                combinedImage.getGraphics().drawImage(image, left, top, null);
                float frameDelay = 0;
                if (withDelayTimes){
                    IIOMetadataNode graphicsControlExtensionNode = getMetaDataNode(root, "GraphicControlExtension");
                    frameDelay = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"))
                            * GIF_DELAYTIME_TO_SECONDS;
                }
                images.put(combinedImage, frameDelay);
                previousImage = combinedImage;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading image: " + file, ex);
        } finally {
            if (imageReader != null)
                imageReader.setInput(null);
        }
        return images;
    }

    private static IIOMetadataNode getMetaDataNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)== 0) {
                return((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return(node);
    }
}