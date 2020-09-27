import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Test {
    final static Random random = new Random();
    final static Scanner scanner = new Scanner(System.in);

    // encode mode
    private static void encode(byte[] inputText) {
        String binaryString = byteArrayToString(inputText); // text as binary numbers in a string
        StringBuilder sb2 = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 4) {
            int parity1, parity2, parity3;
            parity1 = (binaryString.charAt(i) + binaryString.charAt(i + 1) + binaryString.charAt(i + 3)) % 2 == 0 ?
                    0 : 1;
            parity2 = (binaryString.charAt(i) + binaryString.charAt(i + 2) + binaryString.charAt(i + 3)) % 2 == 0 ?
                    0 : 1;
            parity3 = (binaryString.charAt(i + 1) + binaryString.charAt(i + 2) + binaryString.charAt(i + 3)) % 2 == 0 ?
                    0 : 1;
            sb2.append(parity1).append(parity2).append(binaryString.charAt(i)).append(parity3)
                    .append(binaryString.charAt(i + 1)).append(binaryString.charAt(i + 2))
                    .append(binaryString.charAt(i + 3)).append("0").append(" ");  // 1 byte
        }
        // export file
        byte[] input = StringToByteArray(sb2);
        File file = new File("encoded.txt");
        exportFile(input, file);
    }

    // send mode (simulate errors)
    private static void send() throws IOException {
        byte[] encodedText = readFile("encoded.txt");
        // simulate errors
        for (int i = 0; i < encodedText.length; i++) {
            int index = random.nextInt(8);
            encodedText[i] = (byte) (encodedText[i] ^ (1 << index));
        }
        File file = new File("received.txt");
        exportFile(encodedText, file);
    }

    // decode mode
    private static void decode() throws IOException {
        byte[] receivedText = readFile("received.txt");
        int[] receivedUnsigned = new int[receivedText.length];
        for (int i = 0; i < receivedText.length; i++) {
            receivedUnsigned[i] = Byte.toUnsignedInt(receivedText[i]);
        }
        System.out.println(Arrays.toString(receivedUnsigned));

        // convert int[] to binary string representations
        String inputString = Arrays.toString(receivedUnsigned).replace("[", "").replace("]", "").replace(" ", "");
        StringBuilder sbBinaryRep = new StringBuilder();
        for (int i = 0; i < inputString.split(",").length; i++) {
            String temp = Integer.toBinaryString(Integer.parseInt(inputString.split(",")[i]));
            while (temp.length() < 8) { // add leading 0's if byte length < 8
                temp = "0" + temp;
            }
            sbBinaryRep.append(temp);
        }
        String receivedString = sbBinaryRep.toString(); // binary representations

        // add a space every 8 bits
        StringBuilder sb = new StringBuilder(receivedString);
        for (int i = 8; i <= sb.length() - 8; i += 9) {
            sb.insert(i, " ");
        }

        // find the wrong parity bits
        StringBuilder correct = new StringBuilder();
        String[] decodeStr = sb.toString().split(" ");
        for (int i = 0; i < decodeStr.length; i++) {
            int wrongParity = 0;
            if ((Character.getNumericValue(decodeStr[i].charAt(2)) + Character.getNumericValue(decodeStr[i].charAt(4))
                    + Character.getNumericValue(decodeStr[i].charAt(6))) % 2
                    != Character.getNumericValue(decodeStr[i].charAt(0))) {
                wrongParity += 1;
            }
            if ((Character.getNumericValue(decodeStr[i].charAt(2)) + Character.getNumericValue(decodeStr[i].charAt(5))
                    + Character.getNumericValue(decodeStr[i].charAt(6))) % 2
                    != Character.getNumericValue(decodeStr[i].charAt(1))) {
                wrongParity += 2;
            }
            if ((Character.getNumericValue(decodeStr[i].charAt(4)) + Character.getNumericValue(decodeStr[i].charAt(5))
                    + Character.getNumericValue(decodeStr[i].charAt(6))) % 2
                    != Character.getNumericValue(decodeStr[i].charAt(3))) {
                wrongParity += 4;
            }

            // fix the bit in the original byte and append the correct 4 bits in a sb
            StringBuilder sbTemp = new StringBuilder(decodeStr[i]);
            if (wrongParity != 0) {
                sbTemp.setCharAt(wrongParity - 1, sbTemp.charAt(wrongParity - 1) == '0' ? '1' : '0');
            }
            correct.append(sbTemp.charAt(2)).append(sbTemp.charAt(4)).append(sbTemp.charAt(5))
                    .append(sbTemp.charAt(6));
        }
        for (int i = 8; i <= correct.length() - 8; i += 9) {
            correct.insert(i, " ");
        }
        // export file
        byte[] input = StringToByteArray(correct);
        File file = new File("decoded.txt");
        exportFile(input, file);
    }

    // Byte[] to String with binary representations
    private static String byteArrayToString(byte[] byteArray) {
        String inputString = Arrays.toString(byteArray).replace("[", "").replace("]", "").replace(" ", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inputString.split(",").length; i++) {
            String temp = Integer.toBinaryString(Integer.parseInt(inputString.split(",")[i]));
            while (temp.length() < 8) { // add leading 0's if byte length < 8
                temp = "0" + temp;
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    private static byte[] StringToByteArray(StringBuilder sb) {
        byte[] input = new byte[sb.toString().split(" ").length];
        for (int i = 0; i < sb.toString().split(" ").length; i++) {
            input[i] = (byte) Integer.parseInt(sb.toString().split(" ")[i], 2);
        }
        return input;
    }

    // reads a file and return a byte[] representation of the text
    private static byte[] readFile(String fileName) throws IOException {
        byte[] input = null;
        try (FileInputStream inputStream = new FileInputStream(fileName);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            input = bufferedInputStream.readAllBytes();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return input;
    }

    private static void exportFile(byte[] output, File file) {
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            bufferedOutputStream.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Write a mode:");
        String mode = scanner.next();
        byte[] inputText = readFile("send.txt");
        switch (mode) {
            case "encode":
                encode(inputText);
                break;
            case "send":
                encode(inputText);
                send();
                break;
            case "decode":
                encode(inputText);
                send();
                decode();
                break;
        }
    }
}