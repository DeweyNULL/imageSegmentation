package xlzwork;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class image_init {
	
	public static BufferedImage gradimage;
	public static int threshold = 30; 
    private static BufferedImage source; 
    
    //sobel
    
    public static void init(File f){

		try{
			source = ImageIO.read(f);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		double[][] result = transToArray(source);
		

		double[][] gaussmode = gaussTrans(result);
		

		BufferedImage bufferimage = TransToGreyImage(gaussmode);
		

		 gradimage = sobelTran(bufferimage);
    }
		 
		 
	public static double[][] transToArray(BufferedImage i){
		int w = i.getWidth();
		int h = i.getHeight();
		System.out.println(w+" "+h);
		double temp[][] = new double[h][w];
		
		for (int m = 0; m < h; m++) {
			for (int l = 0; l < w; l++) {
				int rgb = i.getRGB(l, m); 
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;

				int grey = (int) (0.3 * r + 0.59 * g + 0.11 * b); //RGB 
				
				temp[m][l] = grey;

			}

		}
		return temp;
	} 
	

	
	public static double[][] gaussTrans(double[][] s){
		
		int h = s.length;
		int w =	s[0].length;
		double[][] temp = new double[h][w];
		

		double[] template = {6.875504209566184E-4,0.0018595456766296074,0.004529233348436208,0.009934776233057956,0.019624880936688967,0.03491174196593651,0.05593086745577397,0.08069509525828301,0.10484754591228278,0.12268315848327596,0.12927875903831346,0.12268315848327596,0.10484754591228278,0.08069509525828301,0.05593086745577397,0.03491174196593651,0.019624880936688967,0.009934776233057956,0.004529233348436208,0.0018595456766296074};
		int tempWH = template.length;

		
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {


				double sum = 0.0;
				for (int m = 0; m < tempWH; m++) {
					// /������ģ���Ӧ��ͼ���ϵ�λ��
					int x = j - (int) tempWH / 2 + m;
					int y = i;

					// ���ģ������û�г����߽�
					if (x >= 0 && x < w) {
						sum = sum + s[y][x] * template[m];
					}
				}

				for (int m = 0; m < tempWH; m++) {
					// /������ģ���Ӧ��ͼ���ϵ�λ��
					int x = j;
					int y = i - (int) tempWH / 2 + m;

					// ���ģ������û�г����߽�
					if (y >= 0 && y < h) {
						sum = sum + s[y][x] * template[m];
					}
				}
				temp[i][j] = sum / 2;
			}
		}
		
		return temp;
	}
	
	//������ת��Ϊ�Ҷ�ͼ��
	//
	//
	public static BufferedImage TransToGreyImage(double[][] sourceArray) {
		int w = sourceArray[0].length;
		int h = sourceArray.length;
		BufferedImage Image = new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int greyRGB = (int) sourceArray[j][i];
				int rgb = (greyRGB << 16) | (greyRGB << 8) | greyRGB;
				Image.setRGB(i, j, rgb);
			}
		}

		return Image;
	}
	
	public static BufferedImage sobelTran(BufferedImage sourceImage) {
		
		int [][] sobleX =  { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int [][] sobleY =  { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		BufferedImage targetImage = new BufferedImage(width, height,
				sourceImage.getType());

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {

				int rgb = 0;
				if (i > 0 && j > 0 && j < height - 1 & i < width - 1) {
					
					//�����㷨��ȡ����������б��8������ĻҶ�ֵ
					int grayRGB0 = sourceImage.getRGB(i - 1, j - 1) >> 16;
					int grayRGB1 = sourceImage.getRGB(i - 1, j) >> 16;
					int grayRGB2 = sourceImage.getRGB(i - 1, j + 1) >> 16;
					int grayRGB3 = sourceImage.getRGB(i, j - 1) >> 16;
					int grayRGB4 = sourceImage.getRGB(i, j) >> 16;
					int grayRGB5 = sourceImage.getRGB(i, j + 1) >> 16;
					int grayRGB6 = sourceImage.getRGB(i + 1, j - 1) >> 16;
					int grayRGB7 = sourceImage.getRGB(i + 1, j) >> 16;
					int grayRGB8 = sourceImage.getRGB(i + 1, j + 1) >> 16;

					// /soble���ӻ�ȡ�ݶ�
					int result = 0;
					int dx = sobleX[0][0] * grayRGB0 + sobleX[0][1] * grayRGB1
							+ sobleX[0][2] * grayRGB2 + sobleX[1][0] * grayRGB3
							+ sobleX[1][1] * grayRGB4 + sobleX[1][2] * grayRGB5
							+ sobleX[2][0] * grayRGB6 + sobleX[2][1] * grayRGB7
							+ sobleX[2][2] * grayRGB8;
					int dy = sobleY[0][0] * grayRGB0 + sobleY[0][1] * grayRGB1
							+ sobleY[0][2] * grayRGB2 + sobleY[1][0] * grayRGB3
							+ sobleY[1][1] * grayRGB4 + sobleY[1][2] * grayRGB5
							+ sobleY[2][0] * grayRGB6 + sobleY[2][1] * grayRGB7
							+ sobleY[2][2] * grayRGB8;
					
					result = (int) Math.sqrt(dx * dx + dy * dy);
					
					
					if (result <= threshold) { 
						rgb = 0;
					} else {
						
						int grayRGB = result;
						rgb = (grayRGB << 16) | (grayRGB << 8) | grayRGB;
					}

				} else {
					rgb = sourceImage.getRGB(i, j);
				}
				targetImage.setRGB(i, j, rgb);
			}
		}
		return targetImage;
	}
}
