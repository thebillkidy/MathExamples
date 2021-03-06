package detection;


import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.filter.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;


public class Detector {
/** The list of classifiers that the test image should pass to be considered as an image.*/
List<Stage> stages;
Point size;

	/**Factory method. Builds a detector from an XML file.
	 * @param filename The XML file (generated by OpenCV) describing the Haar Cascade.
	 * @return The corresponding detector.
	 */
	public static Detector create(String filename) {
		/* Read XML file */ 
		org.jdom.Document document=null;
		SAXBuilder sxb = new SAXBuilder();
	      try
	      {
	         document = sxb.build(new File(filename));
	      }
	      catch(Exception e){e.printStackTrace();}
	      return new Detector(document);
	}
	
	/** Detector constructor.
	 * Builds, from a XML document (i.e. the result of parsing an XML file, the corresponding Haar cascade.
	 * @param document The XML document (parsing of file generated by OpenCV) describing the Haar cascade.
	 */
	public Detector(org.jdom.Document document)
	{
		/* The detector is constituted by stages, each of them telling whether the considered zone represents the object
		 * with probability a bit greater than 0.5. If a zone passes all stages, it is considered as representing the object.*/
		stages=new LinkedList<Stage>();
		
		/* Read the size (in pixels) of the detector. */
		 Element racine = (Element) document.getRootElement().getChildren().get(0);
		  Scanner scanner = new Scanner(racine.getChild("size").getText());
		  size = new Point(scanner.nextInt(),scanner.nextInt());
		  
		  /* Iterate over the stages nodes to read the stages. */
	      Iterator it=racine.getChild("stages").getChildren("_").iterator();
	      while(it.hasNext())
	      {
	    	  Element stage=(Element)it.next();
	    	  /* Read the stage threshold. */
	    	  float thres=Float.parseFloat(stage.getChild("stage_threshold").getText());
	    	  
	    	  /*Read all trees of the stage. */
	    	  Iterator it2=stage.getChild("trees").getChildren("_").iterator();
	    	  Stage st=new Stage(thres);
	    	 while(it2.hasNext())
	    	 {
	    		 Element tree = ((Element)it2.next());
	    		 Tree t = new Tree();
	    		 Iterator it4 = tree.getChildren("_").iterator();
	    		 while(it4.hasNext())
	    		 {
	    		 Element feature=(Element) it4.next();
	    		 float thres2=Float.parseFloat(feature.getChild("threshold").getText());
	    		 int left_node=-1;
	    		 float left_val = 0;
	    		 boolean has_left_val =false;
	    		 int right_node=-1;
	    		 float right_val = 0;
	    		 boolean has_right_val =false;
	    		 Element e;
	    		 if((e=feature.getChild("left_val"))!=null)
	    		 {
	    			 left_val = Float.parseFloat(e.getText());
	    			 has_left_val=true;
	    		 }
	    		 else
	    		 {
	    			 left_node = Integer.parseInt(feature.getChild("left_node").getText());
	    			 has_left_val=false;
	    		 }
	    		 
	    		 if((e=feature.getChild("right_val"))!=null)
	    		 {
	    			 right_val = Float.parseFloat(e.getText());
	    			 has_right_val=true;
	    		 }
	    		 else
	    		 {
	    			 right_node = Integer.parseInt(feature.getChild("right_node").getText());
	    			 has_right_val=false;
	    		 }
	    		 Feature f = new Feature(thres2,left_val,left_node,has_left_val,right_val,right_node,has_right_val,size);
	    		 Iterator it3=feature.getChild("feature").getChild("rects").getChildren("_").iterator();
	    		 while(it3.hasNext())
	    		 {
	    			 String s = ((Element) it3.next()).getText().trim();
	    			 Rect r = Rect.fromString(s);
	    			 f.add(r);
	    		 }

	    		 t.addFeature(f);
	    		 }
	    		 st.addTree(t);
	    		 }
	    	 stages.add(st);
	    	 }
	      }

	/** Returns the list of detected objects in an image applying the Viola-Jones algorithm.
	 * 
	 * The algorithm tests, from sliding windows on the image, of variable size, which regions should be considered as searched objects.
	 * Please see Wikipedia for a description of the algorithm.
	 * @param file The image file to scan.
	 * @param baseScale The initial ratio between the window size and the Haar classifier size (default 2).
	 * @param scale_inc The scale increment of the window size, at each step (default 1.25).
	 * @param increment The shift of the window at each sub-step, in terms of percentage of the window size.
	 * @return the list of rectangles containing searched objects, expressed in pixels.
	 */
	public List<java.awt.Rectangle> getFaces(String file,float baseScale, float scale_inc,float increment, int min_neighbors,boolean doCannyPruning)
	{
		try {
			BufferedImage image = ImageIO.read(new File(file));
			return getFaces(image, baseScale, scale_inc, increment, min_neighbors, doCannyPruning);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<java.awt.Rectangle> getFaces(BufferedImage image,float baseScale, float scale_inc,float increment, int min_neighbors,boolean doCannyPruning)
	{
			//StopWatch sw = new StopWatch();
			//sw.start();
			List<Rectangle> ret=new ArrayList<Rectangle>();
			int width=image.getWidth();
			int height=image.getHeight();
			/* Compute the max scale of the detector, i.e. the size of the image divided by the size of the detector. */
			float maxScale = (Math.min((width+0.f)/size.x,(height+0.0f)/size.y));
			
			/* Compute the grayscale image, the integral image and the squared integral image.*/
			int[][] grayImage=new int[width][height];
			int[][] img = new int[width][height];
			int[][] squares=new int[width][height];
			for(int i=0;i<width;i++)
			{
				int col=0;
				int col2=0;
				for(int j=0;j<height;j++)
				{
					int c = image.getRGB(i,j);
					int  red = (c & 0x00ff0000) >> 16;
					int  green = (c & 0x0000ff00) >> 8;
					int  blue = c & 0x000000ff;
					int value=(30*red +59*green +11*blue)/100;
					img[i][j]=value;
					grayImage[i][j]=(i>0?grayImage[i-1][j]:0)+col+value;
					squares[i][j]=(i>0?squares[i-1][j]:0)+col2+value*value;
					col+=value;
					col2+=value*value;
				}
			}
			
			/* Eventually compute the gradient of the image, if option is on. */
			int[][] canny = null;
			if(doCannyPruning)
				canny = CannyPruner.getIntegralCanny(img);
			
			/*Heart of the algorithm : detection */
			/*For each scale of the detection window */
			for(float scale=baseScale;scale<maxScale;scale*=scale_inc)
			{
				/*Compute the sliding step of the window*/
				int step=(int) (scale*size.x*increment);
				int size=(int) (scale*this.size.x);
				/*For each position of the window on the image, check whether the object is detected there.*/
				for(int i=0;i<width-size;i+=step)
				{
					for(int j=0;j<height-size;j+=step)
					{
                        /* If Canny pruning is on, compute the edge density of the zone.
						 * If it is too low, the object should not be there so skip the region.*/
						if(doCannyPruning)
						{
						int edges_density = canny[i+size][j+size]+canny[i][j]-canny[i][j+size]-canny[i+size][j];
						int d = edges_density/size/size;
						if(d<20||d>100)
							continue;
						}
						boolean pass=true;
						/* Perform each stage of the detector on the window. If one stage fails, the zone is rejected.*/
						for(Stage s:stages)
						{
							
							if(!s.pass(grayImage,squares,i,j,scale))
								{pass=false;
								break;}
						}
						/* If the window passed all stages, add it to the results. */
						if(pass) ret.add(new Rectangle(i,j,size,size));
					}
				}
			}
			//sw.print("Single threaded : ");
			return merge(ret,min_neighbors);
	}
	
	/** Merge the raw detections resulting from the detection step to avoid multiple detections of the same object.
	 * A threshold on the minimum numbers of rectangles that need to be merged for the resulting detection to be kept can be given,
	 * to lower the rate of false detections.
	 * Two rectangles need to be merged if they overlap enough.
	 * @param rects The raw detections returned by the detection algorithm.
	 * @param min_neighbors The minimum number of rectangles needed for the corresponding detection to be kept.
	 * @return The merged rectangular detections.
	 */
	public List<java.awt.Rectangle> merge(List<java.awt.Rectangle> rects, int min_neighbors)
	{
		 List<java.awt.Rectangle> retour=new  LinkedList<java.awt.Rectangle>();
		int[] ret=new int[rects.size()];
		int nb_classes=0;
		for(int i=0;i<rects.size();i++)
		{
			boolean found=false;
			for(int j=0;j<i;j++)
			{
				if(equals(rects.get(j),rects.get(i)))
				{
					found=true;
					ret[i]=ret[j];
				}
			}
			if(!found)
			{
				ret[i]=nb_classes;
				nb_classes++;
			}
		}
		int[] neighbors=new int[nb_classes];
		Rectangle[] rect=new Rectangle[nb_classes];
		for(int i=0;i<nb_classes;i++)
		{
			neighbors[i]=0;
			rect[i]=new Rectangle(0,0,0,0);
		}
		for(int i=0;i<rects.size();i++)
		{
			neighbors[ret[i]]++;
			rect[ret[i]].x+=rects.get(i).x;
			rect[ret[i]].y+=rects.get(i).y;
			rect[ret[i]].height+=rects.get(i).height;
			rect[ret[i]].width+=rects.get(i).width;
		}
		for(int i = 0; i < nb_classes; i++ )
        {
            int n = neighbors[i];
            if( n >= min_neighbors)
            {
            	Rectangle r=new Rectangle(0,0,0,0);
                r.x = (rect[i].x*2 + n)/(2*n);
                r.y = (rect[i].y*2 + n)/(2*n);
                r.width = (rect[i].width*2 + n)/(2*n);
                r.height = (rect[i].height*2 + n)/(2*n);
                retour.add(r);
            }
        }
		return retour;
	}
	
	/** Returns true if two rectangles overlap and should be merged.*/
	public boolean equals(Rectangle r1, Rectangle r2)
	{
		int distance = (int)(r1.width*0.2);

		if(r2.x <= r1.x + distance &&
	           r2.x >= r1.x - distance &&
	           r2.y <= r1.y + distance &&
	           r2.y >= r1.y - distance &&
	           r2.width <= (int)( r1.width * 1.2 ) &&
	           (int)( r2.width * 1.2 ) >= r1.width) return true;
		if(r1.x>=r2.x&&r1.x+r1.width<=r2.x+r2.width&&r1.y>=r2.y&&r1.y+r1.height<=r2.y+r2.height) return true;
		return false;
	}
}
