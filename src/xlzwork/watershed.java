package xlzwork;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

public class watershed {
	
	//ͼƬλ��
	private String pathstring = new String("/image/timg.jpg");
	
	//����������
	private HashMap<Integer,Integer> map=new HashMap<>();
	
	private int maxGrad = 0;
	
	private int [][] block ;
	
	private int [][] maplabel;
	//ɽ�����
	private int RIDGE = -100;
	
	private int height ;
	
	private int width;
	//��ˮ���������
	public  void start() throws IOException {
		
		init_seedblock();//��ʼ����ˮ��
		
		startwatering();//��ˮ������������
		
		WatershededImage();//��ˮ���㷨�����ͼ��
		
		imageSegmentation();//���ݱ����ȡǰ���ͱ���
	}
	
	//��ʼ����ˮ��
	public void init_seedblock(){
		width = image_init.gradimage.getWidth();
		height =  image_init.gradimage.getHeight();
		block = new int[height][width];
		int flag = 0;
		//�ݶȻ��� �ݶ�ֵ��Ϊ�Ҷ�ֵ
		
		for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				//�˴��ĻҶ�ֵ��Ϊ�ݶ�ֵ
				int grad=(image_init.gradimage.getRGB(i, j)>>16)&0xFF;
			
				if(grad>maxGrad){
					maxGrad=grad;
				}
				
				
				//������ʹ�ò��鼯˼��ʵ�������Ǻͺϲ�
				//����ݶ�ֵС�ڵȵ�����ֵ������sobelͼ��ʱ�Ѿ�ͳһ��ֵΪ�㣩�����֮
				if(grad==0){
					//System.out.println(i+" "+j);
					//��Ӧ�ü���������ݶ�ֵ
					//���ڲ������д�����ɨ�裬����Ҳֻ���ж�������������һ�����ĸ�  ���·񱻱��
					//�˴��������������Χֻ��ͬһ������ǵ����򣬻�������������ǵ�����������Ϊһ�����򣬶����ϵ�Ϊһ���������Ϸ��϶��������
					int top=-1;
					if(j>0){
						top=(image_init.gradimage.getRGB(i,j-1)>>16)&0xFF;
						
						//System.out.println("grad:"+grad+" top:"+top);
					}
					int left=-1;
					if(i>0){
						left=(image_init.gradimage.getRGB(i-1,j)>>16)&0xFF;
					}
					
					int top_left=-1;
					if(i>0&&j>0){
						top_left=(image_init.gradimage.getRGB(i-1,j-1)>>16)&0xFF;
					}
					int top_right=-1;
					if(i<width-1&&j>0){
						top_right=(image_init.gradimage.getRGB(i+1,j-1)>>16)&0xFF;
					}
					
					//�˴����������ϡ������������ǣ���϶�Ϊͳһ����������ͬһ��������ʾ����������һ��������ʾ
					int left_top_area=-1;
					int right_top_area=-1;
					if(top==0){
						left_top_area=block[j-1][i];
					}
					if(left==0){
						left_top_area=block[j][i-1];
					}
					if(top_left==0){
						left_top_area=block[j-1][i-1];
					}
					
					if(top_right==0){
						right_top_area=block[j-1][i+1];
					}
					
					
					
					
					if(left_top_area==-1&&right_top_area==-1){
						//������,�ò��鼯���ҷ�ʽ
						block[j][i]=j*width+i;
						map.put(j*width+i, j*width+i); //hashmap��ֵ
					}else if(left_top_area!=-1&&right_top_area==-1){
						///ֻ�е�һ�����򱻱��
						
						int parentIndex=left_top_area;
						//��ȡ��Ӧ���ڵ���±�
						int p_j=parentIndex/width;///�̱�ʾ��
						int p_i=parentIndex%width;///������ʾ��
					//ָ�򸸽ڵ�ĸ��ڵ㣬������������Ч��С���ĸ߶�
						block[j][i]=block[p_j][p_i];
						
					}else if(left_top_area==-1&&right_top_area!=-1){
						//ֻ�еڶ������򱻱��
						
						int parentIndex=right_top_area;
						////��ȡ��Ӧ���ڵ���±�
						int p_j=parentIndex/width;///�̱�ʾ��
						int p_i=parentIndex%width;///������ʾ��
					//ָ�򸸽ڵ�ĸ��ڵ�
						block[j][i]=block[p_j][p_i];
					}else if(left_top_area!=-1&&right_top_area!=-1){
						//�õ���Χ��������������ж��Ƿ�Ϊ��ͬ���������Ҫ���кϲ���
						//if(j==184&&i==4) System.out.println(left_top_area+" "+right_top_area);//����
						
						//��ȡ��һ�������Ӧ���ڵ���±�							
						int first_parentIndex=left_top_area;	
						
						int first_j=first_parentIndex/width;//�̱�ʾ��
						int first_i=first_parentIndex%width;//������ʾ��
					//	if(j==184&&i==4) System.out.println(first_j+" "+first_i);//����
					//	if(j==184&&i==4) System.out.println(block[148][4]);
					//	int count = 0;
						while(block[first_j][first_i]!=first_parentIndex){
						//	System.out.println(++count + " " +block[first_j][first_i]+" "+first_parentIndex);
							first_parentIndex=block[first_j][first_i];						
							//��ȡ��Ӧ���ڵ���±�						
							first_j=first_parentIndex/width;//�̱�ʾ��
							first_i=first_parentIndex%width;//������ʾ��
						}
						//
						//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
						int second_parentIndex=right_top_area;						
						int second_j=second_parentIndex/width;//�̱�ʾ��
						int second_i=second_parentIndex%width;//������ʾ��
						
						while(block[second_j][second_i]!=second_parentIndex){
							second_parentIndex=block[second_j][second_i];						
							second_j=second_parentIndex/width;///�̱�ʾ��
							second_i=second_parentIndex%width;///������ʾ��
						}
						
						
						if(first_parentIndex!=second_parentIndex){
						//��Ҫ�ϲ����ڶ�������ָ���һ������,��Ȼ ֻ�ı���ڸ��ڵ��е�ֵ
							block[second_j][second_i]=first_parentIndex;
							map.remove(second_parentIndex);
						}
						
						block[j][i]=first_parentIndex;
							
					}
					
				
					
				}else{
					//��ʾδ֪����
					block[j][i]=-1;
				}//System.out.println(block[148][4]);
				/*if(block[148][4]!=44404){
					if(flag == 0&&block[148][4]==54915){
						System.out.println("("+j+" "+i+")");
						flag++;}
					//System.out.println("("+j+" "+i+")");
				}else if (block[148][4]==44404){
					if(flag == 1){
						System.out.println("("+j+" "+i+")");
						flag++;}
				} */
				
			}
		}
		/*for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				System.out.printf("%6d",block[j][i]);
			}System.out.println(" ");
		}*/
		//System.out.println(block[148][4]);
	} 
	
	//��ʼ��ˮ������������
	public void startwatering() {

		
		for(int altitude=image_init.threshold;altitude<=maxGrad;altitude++){
			//���ѭ������ʾÿ�ι�ˮ�ﵽ�ĸ߶�
			int addedPixes=0;//��¼ÿ��ѭ���ж��ٸ����ص�����˼�ˮ�裬
			
			do{
				addedPixes=0;
				//��ĳ���߶��£�һֱѭ����û�������ص���뵽��ˮ��Ϊֹ
			for(int j=0;j<height;j++){
				for(int i=0;i<width;i++){
					//�ڲ�ѭ������ͼƬ
					int grad=(image_init.gradimage.getRGB(i, j)>>16)&0xFF;
					if((grad<=altitude)&&(block[j][i]==-1)){
						//����δ��ǵ�����
						int num=0;//��¼������ˮ����Ŀ
						int parentIndex=-1;
						
						
						///���
						if(i>0){//�������߽�
							int value=block[j][i-1];
							if(value!=-1&&value!=-100){ //�����ߵ�ֵ����δ�����ɽ��
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����

								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								//��ô���������߽߱�
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}//ͬ��ʼ���еķ������ҵ����ڵ�
								parentIndex=left_parentIndex;
								num++;
							}
						}
						//�ұ�
						if(i<width-1){////�������߽�
							int value=block[j][i+1];
							if(value!=-1&&value!=-100){
								
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
								
								
							}
						}
						//�ϱ�
						if(j>0){//�������߽�
							int value=block[j-1][i];
							if(value!=-1&&value!=-100){
							//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}

						//�±�
						if(j<height-1){//�������߽�
							int value=block[j+1][i];
							if(value!=-1&&value!=-100){
							
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}
					

						//����
						if(i>0&&j>0){//�������߽�
							int value=block[j-1][i-1];
							if(value!=-1&&value!=-100){
							
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}
					

						//����
						if(i<width-1&&j>0){//�������߽�
							int value=block[j-1][i+1];
							if(value!=-1&&value!=-100){
							
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}
					

						//����
						if(i>0&&j<height-1){//�������߽�
							int value=block[j+1][i-1];
							if(value!=-1&&value!=-100){
							
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}
					

						//����
						if(i<width-1&&j<height-1){//�������߽�
							int value=block[j+1][i+1];
							if(value!=-1&&value!=-100){
							
								//���ñ�ǵ����������ֵ��֮ǰ������ֵ��һ��ʱ��������ˮ����Ŀ��һ
								//�ҵ��������ĸ��ڵ㼴ĳ�ڵ�ָ������						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//�̱�ʾ��
								int left_i=left_parentIndex%width;//������ʾ��
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//�̱�ʾ��
									left_i=left_parentIndex%width;//������ʾ��
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//�Ѹü�ˮ��Ĵ���ڵ�ĸ��ڵ��±�ֵ��¼����
								parentIndex=left_parentIndex;
							}
						}
					
						
						
						
						//����ˮ����Ŀ�պõ���1��
						if(num==1){
							//��ȡ��Ӧ���ڵ���±�
							int p_j=parentIndex/width;//�̱�ʾ��
							int p_i=parentIndex%width;//������ʾ��
							
							//���õ�����Ӧ��ˮ��
							block[j][i]=block[p_j][p_i];
							
							
							
							addedPixes++;
						}else if(num>=2){
							//�õ���Ϊɽ��
							block[j][i]=RIDGE;
							
							addedPixes++;
						}					
						//�����Χû�м�ˮ���򲻹�
						
					}
					
					
				}
			}
			
		//System.out.println(addedPixes+"::::"+altitude);
		}while(addedPixes>0);
      }

	}
	
    public void WatershededImage() throws IOException{   	
		//BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		maplabel= new int[height][width];
		for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				//��ɽ�����ص�����Ϊ��ɫ������Ϊ��ɫ��������
				if(block[j][i]!=RIDGE){
					//int rgb=(255<<16)|(255<<8)|255;
					//image.setRGB(i, j, rgb);
					maplabel[j][i] = 0;
				}else{
					//image.setRGB(i, j, 0);
					maplabel[j][i] = 1;
				}
			}
		}
		
	  // �����������ˮ���㷨������ͼ��
	  /*File file = new File(main.path+"/image/watershed.jpg");
		if(!file.exists()){
			file.createNewFile();
		}
		try {
			ImageIO.write(image, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
    
    public void imageSegmentation() throws IOException {
    	
    	
		BufferedImage ls = null;
		try {
			ls=ImageIO.read(new File(main.path+"/image/label.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int w = ls.getWidth();
		int h = ls.getHeight();
		
		int []widthl = new int[w*h];
		int []heightl = new int[w*h];
		int k = 0;
		
		//���������
		/*for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				System.out.print(maplabel[j][i]);
			}
			System.out.println(" ");
		}*/
		
		//��ȡ��� �˴���¼���Ϊ��ɫ
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int rgb = ls.getRGB(i, j);
				int b = rgb & 0xFF;
				int r = rgb >> 16 & 0xFF ;
				int g = rgb >> 8 & 0xFF ;
				if(b == 0 && g==255 && r==0){
					widthl[k] = i;
					heightl[k++] = j;
					maplabel[j][i] = 2;
				}
			}
		}
		
		//���������
		//System.out.println("-------------------------------------------------");
		
		/*for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				System.out.print(maplabel[j][i]);
			}
			System.out.println(" ");
		}
		*/
		
		//����ʵ�ֱ������
		Queue<labelbkg> bkg = new LinkedList<labelbkg>();
		for (int i = 0; i < k; i++) {
			bkg.offer(new labelbkg(heightl[i],widthl[i]));
		}	
		while(!bkg.isEmpty()){
			labelbkg temp = bkg.poll();
			
			//���
			int left = 0;
			if(temp.w>0&&temp.w<w){
				if(maplabel[temp.h][temp.w-1]==0){
					maplabel[temp.h][temp.w-1]=2;
					left++;
				}
			}
			//�ұ�
			int right = 0;
			if(temp.w>=0 && temp.w<w-1){
				if(maplabel[temp.h][temp.w+1]==0){
					maplabel[temp.h][temp.w+1]=2;
					right++;
				}
			}
			//��
			int top = 0;
			if(temp.h>0&&temp.h<h){
				if(maplabel[temp.h-1][temp.w]==0){
					maplabel[temp.h-1][temp.w]=2;
					top++;
				}
			}
			//��
			int bottom = 0;
			if(temp.h>=0&&temp.h<h-1){
				if(maplabel[temp.h+1][temp.w]==0){
					maplabel[temp.h+1][temp.w]=2;
					bottom++;
				}
			}
			
			if(left==1){bkg.offer(new labelbkg(temp.h,temp.w-1));} 
			if(right==1){bkg.offer(new labelbkg(temp.h,temp.w+1));}
			if(top==1){bkg.offer(new labelbkg(temp.h-1,temp.w));}
			if(bottom==1){bkg.offer(new labelbkg(temp.h+1,temp.w));}
			//System.out.println("ok");
			
		}
		
		//�������ͼ��
		File f1 = new File(main.path+pathstring);
		BufferedImage bkgimage = ImageIO.read(f1);
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				if(maplabel[j][i]!=2){
					int rgb=(255<<16)|(255<<8)|255;
					bkgimage.setRGB(i, j, rgb);
				}
			}			
		}
		
		File fbkg = new File(main.path+"/image/bkg.jpg");
		if(!fbkg.exists()){
			fbkg.createNewFile();
		}
		try {
			ImageIO.write(bkgimage, "jpg", fbkg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//���ǰ��ͼ��
		File f2 = new File(main.path+pathstring);
		BufferedImage frontimage = ImageIO.read(f2);
		
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				if(maplabel[j][i]==2){
					int rgb=(255<<16)|(255<<8)|255;
					frontimage.setRGB(i, j, rgb);
				}
			}			
		}		
		File ffront = new File(main.path+"/image/front.jpg");
		if(!ffront.exists()){
			ffront.createNewFile();
		}
		try {
			ImageIO.write(frontimage, "jpg", ffront);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
