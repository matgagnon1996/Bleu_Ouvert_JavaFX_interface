/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mainpiapp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 *
 * @author mathieu
 */
public class QRCodeGenerator {
    

    QRCodeGenerator()
    {
        
    }
    
    public static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
    
    public static byte[] getQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        System.out.println("Taille bitmap : " + bitMatrix.getWidth()*bitMatrix.getHeight());
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        
        
        // convert 2d matrix to vector
        boolean[] bitMapVec = new boolean[width*height];
        
        for(int i = 0; i<height; i++)
        {
            for(int j = 0;j < width; j++)
            {
                bitMapVec[(i * width) + j] = bitMatrix.get(j, i);
            }
        }
        
        // generate qr encoding for printer
        int  pixelNum, byteNum, bytesOnLine = 99,
                    x, y, b, rowBytes, totalBytes, lastBit, sum, index;
         
        rowBytes   = (width + 7) / 8;
        totalBytes = rowBytes * width;
        index = 0;
       byte[] bitMapEncode = new byte[totalBytes];
       String generatedCode = "";
        
        // Generate body of array
        for(pixelNum=byteNum=y=0; y<width; y++) { // Each row...
          for(x=0; x<rowBytes; x++) { // Each 8-pixel block within row...
            lastBit = (x < rowBytes - 1) ? 1 : (1 << (rowBytes * 8 - width));
            sum     = 0; // Clear accumulated 8 bits
            for(b=128; b>=lastBit; b >>= 1) { // Each pixel within block...
              
              if((bitMapVec[pixelNum++]) == false) sum |= b; // If black pixel, set bit
              
            }
            if(++bytesOnLine >= 10) { // Wrap nicely
                generatedCode += "\n";
                bytesOnLine = 0;
            }
            //String str = " " + sum;
            bitMapEncode[index++] = (byte)sum;
            generatedCode += String.format(" %#x",  sum);//output.format(" 0x%02X", sum); // Write accumulated bits
            if(++byteNum < totalBytes) generatedCode +=',';
          }
      }
        
        return bitMapEncode;
    }
}
