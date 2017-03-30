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
	
	//图片位于
	private String pathstring = new String("/image/timg.jpg");
	
	//用来存数据
	private HashMap<Integer,Integer> map=new HashMap<>();
	
	private int maxGrad = 0;
	
	private int [][] block ;
	
	private int [][] maplabel;
	//山脊标记
	private int RIDGE = -100;
	
	private int height ;
	
	private int width;
	//分水岭操作步骤
	public  void start() throws IOException {
		
		init_seedblock();//初始化集水盆
		
		startwatering();//灌水（区域增长）
		
		WatershededImage();//分水岭算法处理后图像
		
		imageSegmentation();//根据标记提取前景和背景
	}
	
	//初始化集水盆
	public void init_seedblock(){
		width = image_init.gradimage.getWidth();
		height =  image_init.gradimage.getHeight();
		block = new int[height][width];
		int flag = 0;
		//梯度化后 梯度值即为灰度值
		
		for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				//此处的灰度值即为梯度值
				int grad=(image_init.gradimage.getRGB(i, j)>>16)&0xFF;
			
				if(grad>maxGrad){
					maxGrad=grad;
				}
				
				
				//这里是使用并查集思想实现区域标记和合并
				//如果梯度值小于等等于阈值（在求sobel图像时已经统一赋值为零），标记之
				if(grad==0){
					//System.out.println(i+" "+j);
					//本应该检查八邻域的梯度值
					//由于采用逐行从左到右扫描，所以也只需判断上面三个、左一个共四个  点事否被标记
					//此处有两种情况，周围只有同一个被标记的区域，或者有两个被标记的区域：左、左上为一个区域，而右上点为一个区域，正上方肯定不被标记
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
					
					//此处的正左、左上、正上如果被标记，则肯定为统一区域，所以用同一个变量表示，右上用另一个变量表示
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
						//新区域,用并查集查找方式
						block[j][i]=j*width+i;
						map.put(j*width+i, j*width+i); //hashmap存值
					}else if(left_top_area!=-1&&right_top_area==-1){
						///只有第一个区域被标记
						
						int parentIndex=left_top_area;
						//获取对应父节点的下标
						int p_j=parentIndex/width;///商表示行
						int p_i=parentIndex%width;///余数表示列
					//指向父节点的父节点，这样做可以有效减小树的高度
						block[j][i]=block[p_j][p_i];
						
					}else if(left_top_area==-1&&right_top_area!=-1){
						//只有第二个区域被标记
						
						int parentIndex=right_top_area;
						////获取对应父节点的下标
						int p_j=parentIndex/width;///商表示行
						int p_i=parentIndex%width;///余数表示列
					//指向父节点的父节点
						block[j][i]=block[p_j][p_i];
					}else if(left_top_area!=-1&&right_top_area!=-1){
						//该点周围有两个标记区域，判断是否为不同的区域而需要进行合并！
						//if(j==184&&i==4) System.out.println(left_top_area+" "+right_top_area);//测试
						
						//获取第一个区域对应根节点的下标							
						int first_parentIndex=left_top_area;	
						
						int first_j=first_parentIndex/width;//商表示行
						int first_i=first_parentIndex%width;//余数表示列
					//	if(j==184&&i==4) System.out.println(first_j+" "+first_i);//测试
					//	if(j==184&&i==4) System.out.println(block[148][4]);
					//	int count = 0;
						while(block[first_j][first_i]!=first_parentIndex){
						//	System.out.println(++count + " " +block[first_j][first_i]+" "+first_parentIndex);
							first_parentIndex=block[first_j][first_i];						
							//获取对应父节点的下标						
							first_j=first_parentIndex/width;//商表示行
							first_i=first_parentIndex%width;//余数表示列
						}
						//
						//找到左边区域的根节点即某节点指向自身						
						int second_parentIndex=right_top_area;						
						int second_j=second_parentIndex/width;//商表示行
						int second_i=second_parentIndex%width;//余数表示列
						
						while(block[second_j][second_i]!=second_parentIndex){
							second_parentIndex=block[second_j][second_i];						
							second_j=second_parentIndex/width;///商表示行
							second_i=second_parentIndex%width;///余数表示列
						}
						
						
						if(first_parentIndex!=second_parentIndex){
						//需要合并，第二个区域指向第一个区域,当然 只改变存在根节点中的值
							block[second_j][second_i]=first_parentIndex;
							map.remove(second_parentIndex);
						}
						
						block[j][i]=first_parentIndex;
							
					}
					
				
					
				}else{
					//表示未知区域
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
	
	//开始灌水（区域增长）
	public void startwatering() {

		
		for(int altitude=image_init.threshold;altitude<=maxGrad;altitude++){
			//外层循环，表示每次灌水达到的高度
			int addedPixes=0;//记录每次循环有多少个像素点加入了集水盆，
			
			do{
				addedPixes=0;
				//在某个高度下，一直循环到没有新像素点加入到集水盆为止
			for(int j=0;j<height;j++){
				for(int i=0;i<width;i++){
					//内部循环遍历图片
					int grad=(image_init.gradimage.getRGB(i, j)>>16)&0xFF;
					if((grad<=altitude)&&(block[j][i]==-1)){
						//处理未标记的区域
						int num=0;//记录四领域集水盆数目
						int parentIndex=-1;
						
						
						///左边
						if(i>0){//不超出边界
							int value=block[j][i-1];
							if(value!=-1&&value!=-100){ //如果左边的值不是未处理和山脊
								//把该集水盆的代表节点的父节点下标值记录下来

								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								//这么做不会出左边边界
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}//同初始化中的方法，找到根节点
								parentIndex=left_parentIndex;
								num++;
							}
						}
						//右边
						if(i<width-1){////不超出边界
							int value=block[j][i+1];
							if(value!=-1&&value!=-100){
								
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
								
								
							}
						}
						//上边
						if(j>0){//不超出边界
							int value=block[j-1][i];
							if(value!=-1&&value!=-100){
							//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}

						//下边
						if(j<height-1){//不超出边界
							int value=block[j+1][i];
							if(value!=-1&&value!=-100){
							
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}
					

						//左上
						if(i>0&&j>0){//不超出边界
							int value=block[j-1][i-1];
							if(value!=-1&&value!=-100){
							
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}
					

						//右上
						if(i<width-1&&j>0){//不超出边界
							int value=block[j-1][i+1];
							if(value!=-1&&value!=-100){
							
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}
					

						//左下
						if(i>0&&j<height-1){//不超出边界
							int value=block[j+1][i-1];
							if(value!=-1&&value!=-100){
							
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}
					

						//右下
						if(i<width-1&&j<height-1){//不超出边界
							int value=block[j+1][i+1];
							if(value!=-1&&value!=-100){
							
								//当该标记点所在区域的值与之前的区域值不一致时，四邻域集水盆数目加一
								//找到左边区域的根节点即某节点指向自身						
								int left_parentIndex=value;						
								int left_j=left_parentIndex/width;//商表示行
								int left_i=left_parentIndex%width;//余数表示列
								
								while(block[left_j][left_i]!=left_parentIndex){
									left_parentIndex=block[left_j][left_i];						
									left_j=left_parentIndex/width;//商表示行
									left_i=left_parentIndex%width;//余数表示列
								}
								if(left_parentIndex!=parentIndex){
									num++;
								}
								//把该集水盆的代表节点的父节点下标值记录下来
								parentIndex=left_parentIndex;
							}
						}
					
						
						
						
						//领域集水盆数目刚好等于1个
						if(num==1){
							//获取对应父节点的下标
							int p_j=parentIndex/width;//商表示行
							int p_i=parentIndex%width;//余数表示列
							
							//将该点加入对应集水盆
							block[j][i]=block[p_j][p_i];
							
							
							
							addedPixes++;
						}else if(num>=2){
							//该点标记为山脊
							block[j][i]=RIDGE;
							
							addedPixes++;
						}					
						//如果周围没有集水盆则不管
						
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
				//将山脊像素点设置为白色，其它为黑色，测试用
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
		
	  // 测试用输出分水岭算法处理后的图像
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
		
		//测试用输出
		/*for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				System.out.print(maplabel[j][i]);
			}
			System.out.println(" ");
		}*/
		
		//获取标记 此处记录标记为绿色
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
		
		//测试用输出
		//System.out.println("-------------------------------------------------");
		
		/*for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				System.out.print(maplabel[j][i]);
			}
			System.out.println(" ");
		}
		*/
		
		//队列实现标记作用
		Queue<labelbkg> bkg = new LinkedList<labelbkg>();
		for (int i = 0; i < k; i++) {
			bkg.offer(new labelbkg(heightl[i],widthl[i]));
		}	
		while(!bkg.isEmpty()){
			labelbkg temp = bkg.poll();
			
			//左边
			int left = 0;
			if(temp.w>0&&temp.w<w){
				if(maplabel[temp.h][temp.w-1]==0){
					maplabel[temp.h][temp.w-1]=2;
					left++;
				}
			}
			//右边
			int right = 0;
			if(temp.w>=0 && temp.w<w-1){
				if(maplabel[temp.h][temp.w+1]==0){
					maplabel[temp.h][temp.w+1]=2;
					right++;
				}
			}
			//上
			int top = 0;
			if(temp.h>0&&temp.h<h){
				if(maplabel[temp.h-1][temp.w]==0){
					maplabel[temp.h-1][temp.w]=2;
					top++;
				}
			}
			//下
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
		
		//输出背景图像
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
		
		
		//输出前景图像
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
