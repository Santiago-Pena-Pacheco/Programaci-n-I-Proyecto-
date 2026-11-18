package com.github.santiago.fundamentos.vision;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

/** Version en Java (OpenCV + Tess4J) de la logica que antes vivia en "Proyecto-fundamentos_definitivo.py" */
public class DetectorPlacas implements AutoCloseable {

    private static final Pattern PATRON_CARRO = Pattern.compile("^[A-Z]{3}[0-9]{3}$");
    private static final Pattern PATRON_MOTO = Pattern.compile("^[A-Z]{3}[0-9]{2}[A-Z]{1}$");

    private static final double AREA_MINIMA = 1000;
    private static final double AREA_MAXIMA = 30000;
    private static final double ASPECT_RATIO_MIN = 2.0;
    private static final double ASPECT_RATIO_MAX = 5.5;
    private static final int REPETICIONES_PARA_CONFIRMAR = 5;

    private final ITesseract tesseract;
    private final Deque<String> lecturasRecientes = new ArrayDeque<>();

    /** (por ejemplo "C:/Program Files/Tesseract-OCR/tessdata" o "/usr/share/tesseract-ocr/5/tessdata" en Linux) */
    public DetectorPlacas(String rutaTessdata) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(rutaTessdata);
        this.tesseract.setLanguage("eng");
        this.tesseract.setPageSegMode(8); // PSM 8 = una sola palabra, igual que --psm 8 en Python
        this.tesseract.setVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    /** Procesa un frame completo (BGR, como lo entrega VideoCapture) y devuelve el resultado de la deteccion para ese frame */
    public ResultadoDeteccion procesarFrame(Mat frame) {
        int ancho = frame.cols();
        int alto = frame.rows();

        // Region de interes central, igual que x1,x2,y1,y2 en el script Python
        int x1 = ancho / 3;
        int x2 = ancho * 2 / 3;
        int y1 = alto / 3;
        int y2 = alto * 2 / 3;
        Rect roi = new Rect(x1, y1, x2 - x1, y2 - y1);
        Mat recorte = new Mat(frame, roi);

        Mat gris = new Mat();
        Imgproc.cvtColor(recorte, gris, Imgproc.COLOR_BGR2GRAY);

        Mat suavizado = new Mat();
        Imgproc.GaussianBlur(gris, suavizado, new Size(5, 5), 0);

        Mat bordes = new Mat();
        Imgproc.Canny(suavizado, bordes, 100, 200);
        Imgproc.dilate(bordes, bordes, new Mat());

        List<MatOfPoint> contornos = new ArrayList<>();
        Mat jerarquia = new Mat();
        Imgproc.findContours(bordes, contornos, jerarquia, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Rect mejorRect = null;
        double mejorArea = 0;

        for (MatOfPoint contorno : contornos) {
            double area = Imgproc.contourArea(contorno);
            if (area > AREA_MINIMA && area < AREA_MAXIMA && area > mejorArea) {
                Rect rect = Imgproc.boundingRect(contorno);
                double aspectRatio = (double) rect.width / rect.height;
                if (aspectRatio > ASPECT_RATIO_MIN && aspectRatio < ASPECT_RATIO_MAX) {
                    MatOfPoint2f contorno2f = new MatOfPoint2f(contorno.toArray());
                    double perimetro = Imgproc.arcLength(contorno2f, true);
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(contorno2f, approx, 0.04 * perimetro, true);
                    if (approx.total() == 4) {
                        mejorRect = rect;
                        mejorArea = area;
                    }
                }
            }
        }

        if (mejorRect == null) {
            return new ResultadoDeteccion(null, null);
        }

        // Rect relativo al frame completo (para dibujarlo en la GUI)
        Rect rectEnFrame = new Rect(x1 + mejorRect.x, y1 + mejorRect.y, mejorRect.width, mejorRect.height);

        String textoConfirmado = leerTextoDePlaca(recorte, mejorRect);
        return new ResultadoDeteccion(rectEnFrame, textoConfirmado);
    }

    private String leerTextoDePlaca(Mat recorte, Rect regionPlaca) {
        Mat placaRecortada = new Mat(recorte, regionPlaca);

        Mat grisPlaca = new Mat();
        Imgproc.cvtColor(placaRecortada, grisPlaca, Imgproc.COLOR_BGR2GRAY);

        if (grisPlaca.rows() < 20 || grisPlaca.cols() < 50) {
            return null; // demasiado pequeña para un OCR confiable, igual que en Python
        }

        Mat binarizada = new Mat();
        Imgproc.adaptiveThreshold(grisPlaca, binarizada, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        if (Core.mean(binarizada).val[0] < 127) {
            Core.bitwise_not(binarizada, binarizada);
        }

        BufferedImage imagenParaOcr = ConversorImagenes.matABufferedImage(binarizada);

        String texto;
        try {
            texto = tesseract.doOCR(imagenParaOcr);
        } catch (TesseractException e) {
            return null;
        }

        texto = texto == null ? "" : texto.trim().toUpperCase().replace("\n", "").replace(" ", "");
        return confirmarPlaca(texto);
    }

    /** Buffer de confirmacion: equivalente a confirmar_placa() en Python */
    private String confirmarPlaca(String textoDetectado) {
        if (textoDetectado.isEmpty()) {
            return null;
        }
        lecturasRecientes.addLast(textoDetectado);
        if (lecturasRecientes.size() > REPETICIONES_PARA_CONFIRMAR) {
            lecturasRecientes.removeFirst();
        }
        boolean todasIguales = lecturasRecientes.size() == REPETICIONES_PARA_CONFIRMAR
                && lecturasRecientes.stream().allMatch(t -> t.equals(lecturasRecientes.peekFirst()));

        if (!todasIguales) {
            return null;
        }

        String candidata = lecturasRecientes.peekFirst();
        lecturasRecientes.clear();

        if (candidata.length() == 6
                && (PATRON_CARRO.matcher(candidata).matches() || PATRON_MOTO.matcher(candidata).matches())) {
            return candidata.substring(0, 3) + " " + candidata.substring(3);
        }
        return null;
    }

    /** Dibuja el recuadro de la region detectada sobre el frame, como retroalimentacion visual. */
    public static void dibujarRegion(Mat frame, Rect region) {
        Imgproc.rectangle(frame,
                new Point(region.x, region.y),
                new Point(region.x + region.width, region.y + region.height),
                new Scalar(0, 255, 0), 2);
    }

    @Override
    public void close() {
        lecturasRecientes.clear();
    }
}
