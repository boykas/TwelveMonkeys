package com.twelvemonkeys.imageio.util;

import com.twelvemonkeys.image.ImageUtil;

import javax.imageio.IIOParam;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IIOUtil
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @author last modified by $Author: haraldk$
 * @version $Id: IIOUtil.java,v 1.0 May 8, 2008 3:04:54 PM haraldk Exp$
 */
public final class IIOUtil {
    private IIOUtil() {}

    /**
     * Creates an {@code InputStream} adapter that reads from an underlying {@code ImageInputStream}.
     * The input stream will read until the end of {@code pStream}.
     *
     * @param pStream the stream to read from.
     * @return an {@code InputStream} reading from {@code pStream}.
     */
    public static InputStream createStreamAdapter(final ImageInputStream pStream) {
        // TODO: Include stream start pos?
        // TODO: Skip buffering for known in-memory implementations?
        return new BufferedInputStream(new IIOInputStreamAdapter(pStream));
    }

    /**
     * Creates an {@code InputStream} adapter that reads from an underlying {@code ImageInputStream}.
     * The input stream will read until the end of {@code pStream}, or at most {@code pLength} bytes has been read.
     *
     * @param pStream the stream to read from.
     * @param pLength the maximum number of bytes that can be read from {@code pStream}.
     * @return an {@code InputStream} reading from {@code pStream}.
     */
    public static InputStream createStreamAdapter(final ImageInputStream pStream, final long pLength) {
        // TODO: Include stream start pos?
        // TODO: Skip buffering for known in-memory implementations?
        return new BufferedInputStream(new IIOInputStreamAdapter(pStream, pLength));
    }

    /**
     * Creates an {@code OutputStream} adapter that writes to an underlying {@code ImageOutputStream}.
     *
     * @param pStream the stream to write to.
     * @return an {@code OutputSteam} writing to {@code pStream}.
     */
    public static OutputStream createStreamAdapter(final ImageOutputStream pStream) {
        return new BufferedOutputStream(new IIOOutputStreamAdapter(pStream));
    }

    /*
     * THIS METHOD WILL ME MOVED/RENAMED, DO NOT USE.
     */
    public static <T> void deregisterProvider(final ServiceRegistry pRegistry, final IIOServiceProvider pProvider, final Class<T> pCategory) {
        // http://www.ibm.com/developerworks/java/library/j-jtp04298.html
        // TODO: Consider placing this method in a ImageReaderSpiBase class or similar
        pRegistry.deregisterServiceProvider(pCategory.cast(pProvider), pCategory);
    }

    public static Image fakeSubsampling(final Image pImage, final IIOParam pParam) {
        if (pImage == null) {
            return null;
        }

        if (pParam != null) {
            int x = pParam.getSourceXSubsampling();
            int y = pParam.getSourceYSubsampling();

            // 1 is default
            if (x > 1 || y > 1) {
                int w = (ImageUtil.getWidth(pImage) + x - 1) / x;
                int h = (ImageUtil.getHeight(pImage) + y - 1) / y;

                // Fake subsampling by scaling fast
                return pImage.getScaledInstance(w, h, Image.SCALE_FAST);
            }
        }

        return pImage;
    }

    public static Rectangle getSourceRegion(final IIOParam pParam, final int pSrcWidth, final int pSrcHeight) {
        Rectangle sourceRegion = new Rectangle(pSrcWidth, pSrcHeight);

        // If param is present, calculate region
        if (pParam != null) {
            // Get intersection with source region
            Rectangle region = pParam.getSourceRegion();
            if (region != null) {
                sourceRegion = sourceRegion.intersection(region);
            }

            // Scale according to subsampling offsets
            int subsampleXOffset = pParam.getSubsamplingXOffset();
            int subsampleYOffset = pParam.getSubsamplingYOffset();
            sourceRegion.x += subsampleXOffset;
            sourceRegion.y += subsampleYOffset;
            sourceRegion.width -= subsampleXOffset;
            sourceRegion.height -= subsampleYOffset;
        }

        return sourceRegion;
    }

    public static BufferedImage fakeAOI(final BufferedImage pImage, final Rectangle pSourceRegion) {
        if (pImage == null) {
            return null;
        }

        if (pSourceRegion != null) {
            if (pSourceRegion.x != 0 || pSourceRegion.y != 0 || pSourceRegion.width != pImage.getWidth() || pSourceRegion.height != pImage.getHeight()) {
                return pImage.getSubimage(pSourceRegion.x, pSourceRegion.y, pSourceRegion.width, pSourceRegion.height);
            }
        }

        return pImage;
    }
}