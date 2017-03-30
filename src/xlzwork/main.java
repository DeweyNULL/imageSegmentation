package xlzwork;
import java.io.File;
import java.io.IOException;
import java.net.URL;



public class main {
	
	public static String path;/////Èí¼þÂ·¾¶
	static{		
		URL url=new main().getClass().getProtectionDomain().getCodeSource().getLocation();
    	path=url.getPath();
    	path=path.substring(1, path.lastIndexOf("bin/"));
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		image_init.init(new File(path+"/image/timg.jpg"));
		
		watershed watershed = new watershed();
		watershed.start();		
		System.out.println("ok!");
		
	}
	
	
}
