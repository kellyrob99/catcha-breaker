package org.kar

import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Color
import java.awt.RenderingHints
import java.awt.color.ColorSpace
import java.awt.image.ColorConvertOp
import javax.media.jai.JAI
import javax.media.jai.RenderedOp
import com.sun.media.jai.codec.ImageCodec
import com.sun.media.jai.codec.ImageEncoder
import com.sun.media.jai.codec.TIFFEncodeParam

/**
 * @author Kelly Robinson
 */
class CaptchaBreaker
{
//    from PIL import Image
//
//img = Image.open('input.gif')
//img = img.convert("RGBA")
//
//pixdata = img.load()
//
//# Clean the background noise, if color != black, then set to white.
//for y in xrange(img.size[1]):
//    for x in xrange(img.size[0]):
//        if pixdata[x, y] != (0, 0, 0, 255):
//            pixdata[x, y] = (255, 255, 255, 255)
//
//img.save("input-black.gif", "GIF")
//
//#   Make the image bigger (needed for OCR)
//im_orig = Image.open('input-black.gif')
//big = im_orig.resize((116, 56), Image.NEAREST)
//
//ext = ".tif"
//big.save("input-NEAREST" + ext)
//
//#   Perform OCR using pytesser library
//from pytesser import *
//image = Image.open('input-NEAREST.tif')
//print image_to_string(image)

    def imageToString = {String fileName ->
        BufferedImage image = ImageIO.read(new File(fileName))
        BufferedImage dimg = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

        dimg.createGraphics().with {
            setComposite(AlphaComposite.Src)
            drawImage(image, null, 0, 0)
            dispose()
        }
        (0..<dimg.height).each {i ->
            (0..<dimg.width).each {j ->
                if (dimg.getRGB(j, i) != Color.BLACK.RGB)
                {
                    dimg.setRGB(j, i, Color.WHITE.RGB)
                }
            }
        }
        dimg = resizeImage(dimg, 116, 56)
        return doOcr(dimg)
    }

    def doOcr = {image ->
        def tmpDir = System.properties['java.io.tmpdir']
        def tmpGif = "${tmpDir}tmp.gif"
        def tmpTif = "${tmpDir}tmp.tif"
        def tmpTesseract = "${tmpDir}tmp"
        ImageIO.write(image, 'gif', new File(tmpGif))
        convertToTiff(tmpGif, tmpTif)
        def tesseract = ['/opt/local/bin/tesseract', tmpTif, tmpTesseract].execute()
        tesseract.waitFor()
        return new File("${tmpTesseract}.txt").readLines()[0]
    }

    def resizeImage = {BufferedImage image, int w, int h ->
        BufferedImage dimg = new BufferedImage(w, h, image.type)
        dimg.createGraphics().with {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            drawImage(image, 0, 0, w, h, 0, 0, image.width, image.height, null)
            dispose()
        }
        return dimg
    }

     void convertToTiff(String inputFile, String outputFile)
    {
        OutputStream ios
        try
        {
            ios = new BufferedOutputStream(new FileOutputStream(new File(outputFile)))
            ImageEncoder enc = ImageCodec.createImageEncoder("tiff", ios, new TIFFEncodeParam(compression: TIFFEncodeParam.COMPRESSION_NONE, littleEndian: false))
            RenderedOp src = JAI.create("fileload", inputFile)

            //Apply the color filter and return the result.
            ColorConvertOp filterObj = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null)
            BufferedImage dst = new BufferedImage(src.width, src.height, BufferedImage.TYPE_3BYTE_BGR)
            filterObj.filter(src.getAsBufferedImage(), dst)

            // save the output file
            enc.encode(dst)
        }
        catch (Exception e)
        {
            println e
        }
        finally
        {
            ios.close()
        }
    }
}
