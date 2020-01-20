package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    private static final String IMAGE_FILE   = "/Users/DimKa_N7/Documents/IS/lab7/self1.png";
    private static final String ENCODED_FILE = "/Users/DimKa_N7/Documents/IS/lab7/encoded.txt";
    private static final String DECODED_FILE = "/Users/DimKa_N7/Documents/IS/lab7/selfDecoded.png";

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        print("Введите простое число p: ");
        BigInteger p = new BigInteger(br.readLine());
        print("Введите простое число q: ");
        BigInteger q = new BigInteger(br.readLine());

        BigInteger n = p.multiply(q);
        BigInteger m = p.subtract((new BigInteger("1"))).multiply(q.subtract((new BigInteger("1"))));
        print("n: " + n);
        print("m: " + m);

        BigInteger secretKeyKey = getSecretKey(m);
        print("Secret key: " + secretKeyKey);
        BigInteger openKey = getOpenKey(secretKeyKey, m);
        print("Open key: " + openKey);

        encodeImage(openKey, n);
        decodeImage(secretKeyKey, n);
    }

    private static void encodeImage(BigInteger openKey, BigInteger n) throws IOException {
        BufferedImage image = ImageIO.read(new File(IMAGE_FILE));
        ArrayList<BigInteger> encoded = new ArrayList<>();
        for (int c = 0; c < 3; c++) {
            int[][] imagePixels = getPixels(image, c);
            for (int i = 0; i < getRowCount(imagePixels); i++) {
                for (int j = 0; j < getColumnCount(imagePixels); j++) {
                    BigInteger encodedPixel = BigInteger.valueOf(imagePixels[i][j]).modPow(openKey, n);
                    encoded.add(encodedPixel);
                }
            }
        }
        FileWriter fw = new FileWriter(new File(ENCODED_FILE));
        for (BigInteger b : encoded) {
            fw.write(b.toString() + "\n");
        }
        fw.close();
    }

    private static ArrayList<BigInteger> readFromFile() {
        ArrayList<BigInteger> result = new ArrayList<>();
        String encodedData = "";
        try {
            encodedData = new String(Files.readAllBytes(Paths.get(ENCODED_FILE)));
            String[] arr = encodedData.split("\n");
            for (String s : arr) {
                result.add(new BigInteger(s));
            }
        } catch (Exception e) { }
        return result;
    }

    private static void decodeImage(BigInteger secretKey, BigInteger n) throws IOException {
        ArrayList<BigInteger> encodedArr = readFromFile();
        ArrayList<BigInteger> decodedArr = new ArrayList<>();
        for (BigInteger b : encodedArr) {
            decodedArr.add(b.modPow(secretKey, n));
        }
        ArrayList<int[][]> colorMatrixs = new ArrayList<>();
        for (int c = 0; c < 3; c++) {
            int[][] temp = new int[50][50];
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    temp[i][j] = decodedArr.get(50 * i + j + 2500 * c).intValue();
                }
            }
            colorMatrixs.add(temp);
        }
//        for (int[][] m: colorMatrixs) {
//            printMatrix(m, "m");
//        }
        BufferedImage image = ImageIO.read(new File(DECODED_FILE));
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                int pixel = (255 << 24) | (colorMatrixs.get(0)[i][j] << 16) | (colorMatrixs.get(1)[i][j] << 8) | colorMatrixs.get(2)[i][j];
                image.setRGB(j, i, pixel);
            }
        }
        ImageIO.write(image, "png", new File(DECODED_FILE));
    }

    private static int[][] getPixels(BufferedImage image, int color) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color c = new Color(image.getRGB(col, row));
                switch (color) {
                    case 0:
                        result[row][col] = c.getRed();
                        break;
                    case 1:
                        result[row][col] = c.getGreen();
                        break;
                    case 2:
                        result[row][col] = c.getBlue();
                        break;
                }
            }
        }
        return result;
    }

    private static BigInteger getSecretKey(BigInteger m) {
        BigInteger result = m.subtract(new BigInteger("1"));
        ArrayList<BigInteger> arr = new ArrayList<>();
        // result > 1
        while(result.compareTo(new BigInteger("1")) == 1) {
            if (m.gcd(result).compareTo(new BigInteger("1")) == 0) {
                arr.add(result);
            }
            result = result.subtract(new BigInteger("1"));
        }
        int randIndex = (int) (Math.random() * arr.size());
        return arr.get(randIndex);
    }

    private static BigInteger getOpenKey(BigInteger secretKey, BigInteger m) {
        BigInteger result = secretKey;
        while (true) {
            if (result.multiply(secretKey).mod(m).compareTo(new BigInteger("1")) == 0) {
                break;
            }
            else result = result.add(new BigInteger("1"));
        }
        return result;
    }

    private static void printMatrix(int[][] matrix, String matrixName) {
        print(matrixName + ": ");
        for (int i = 0; i < getRowCount(matrix); i++) {
            for (int j = 0; j < getColumnCount(matrix); j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static int getRowCount(int[][] matrix) {
        return matrix.length;
    }

    private static int getColumnCount(int[][] matrix) {
        return matrix[0].length;
    }

    private static void print(String text) {
        System.out.println(text);
    }
}