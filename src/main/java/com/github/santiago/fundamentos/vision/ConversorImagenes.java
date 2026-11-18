package com.github.santiago.fundamentos.vision;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/** Utilidades de conversion entre Mat (OpenCV) y BufferedImage (Java AWT/Swing) */
public final class ConversorImagenes {

    private ConversorImagenes() {
    }

    public static BufferedImage matABufferedImage(Mat mat) {
        int tipo = (mat.channels() == 1) ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;

        int tamanoBuffer = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[tamanoBuffer];
        mat.get(0, 0, buffer);

        BufferedImage imagen = new BufferedImage(mat.cols(), mat.rows(), tipo);
        byte[] destino = ((DataBufferByte) imagen.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, destino, 0, buffer.length);
        return imagen;
    }

    /** Asegura 3 canales BGR (Tess4J y AWT esperan RGB/BGR de 3 bytes, no binarios de 1 canal en todos los casos). */
    public static Mat aTresCanales(Mat mat) {
        if (mat.channels() == 3) {
            return mat;
        }
        Mat salida = new Mat();
        org.opencv.imgproc.Imgproc.cvtColor(mat, salida, org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR);
        return salida;
    }

    public static boolean esValida(Mat mat) {
        return mat != null && !mat.empty() && mat.type() >= 0 && mat.depth() == CvType.CV_8U;
    }
}
