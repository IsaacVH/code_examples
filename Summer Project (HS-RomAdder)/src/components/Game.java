package components;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.ini4j.*;

// This is the class that every 
// game in the library will belong to.
public class Game {

	public File GAMEFILE;
	public File VIDEOFILE;
	public File ARTFILE;
	private BufferedImage ART;
	
	public boolean EXISTS;
	public boolean ARTEXISTS;
	public boolean VIDEOEXISTS;

	private static String PATH;
	private String GAMEPATH;
	private String ARTPATH;
	private String VIDEOPATH;
	private static String NAME;
	private static String SYSTEM;
	private static String EXTENSION;

	// Setting defaults
	Game() {
		GAMEFILE = null;
		ART = null;
		VIDEOFILE = null;
		
		EXISTS = false;
		ARTEXISTS = false;
		VIDEOEXISTS = false;
		
		PATH = null;
		GAMEPATH = null;
		ARTPATH = null;
		VIDEOPATH = null;
		NAME = null;
		SYSTEM = null;
		EXTENSION = null;
	}

	// Initialized defaults
	Game(String name, String system, String path) {
		
		NAME = name;
		SYSTEM = system;
		PATH = path;
		
		File settings = null;
		
		settings = new File(path + "\\Settings\\" + system + ".ini");
		Wini ini = null;
				
		try {
			ini = new Wini(settings);
		} catch (InvalidFileFormatException e) {
			System.out.println("Invalid Format");
		} catch (IOException e) {
			System.out.println("Error in reading 'ini' file.");
		}
		
		Config.getGlobal().setEscapeNewline(false);
		EXTENSION = ini.get("exe info", "romextension");
		String userompath = ini.get("exe info", "userompath");
		
		if(userompath.equals("true")){
			GAMEPATH = ini.get("exe info", "rompath") + NAME + "." + EXTENSION;
			
		} else if(ini.get("exe info", "rompath") != null) {
			GAMEPATH = ini.get("exe info", "rompath") + NAME + "." + EXTENSION;
		}else {
			GAMEPATH = path + "\\Emulators\\RomFiles\\" + SYSTEM + "\\" + NAME
					+ "." + EXTENSION;
		}
		
		ARTPATH = PATH + "\\Media\\" + SYSTEM + "\\images\\Wheel\\" + NAME + ".png";
		VIDEOPATH = PATH + "\\Media\\" + SYSTEM + "\\Video\\" + NAME + ".flv";
		
		GAMEFILE = new File(GAMEPATH);
		ART = loadArt(ARTPATH);
		VIDEOFILE = loadVideo(VIDEOPATH);
				
		EXISTS = false;
		if (GAMEFILE.exists()) {
			EXISTS = true;
		} else {
			GAMEPATH = ini.get("exe info", "rompath") + NAME + "\\" + NAME + "." + EXTENSION;
			GAMEFILE = new File(GAMEPATH);
			if(GAMEFILE.exists()) {
				EXISTS = true;
			} else {
				GAMEPATH = ini.get("exe info", "rompath") + NAME + "." + EXTENSION;
				GAMEFILE = null;
			}
		}
	}

	//Function to load artwork from specified file.
	private BufferedImage loadArt(String artpath) {

		BufferedImage art = null;
		ARTFILE = new File(artpath);
		
		try {
			art = ImageIO.read(ARTFILE);
		} catch (IOException e) {
			ARTEXISTS = false;
			return (null);
		}

		ARTEXISTS = true;
		return (art);
	}
	
	//Function to load video from specified file.
	private File loadVideo(String videopath) {
		
		File video = new File(videopath);
		
		if(video.exists()){
			VIDEOEXISTS = true;
			return(video);
		} else {
			VIDEOEXISTS = false;
			return(null);
		}
		
	}

	//Some more get/set functions.
	public BufferedImage getART() {
		return ART;
	}
	
	public String getGAMEPATH() {
		return GAMEPATH;
	}

	public String getARTPATH() {
		return ARTPATH;
	}
	
	public String getVIDEOPATH() {
		return VIDEOPATH;
	}
	
	public String getNAME() {
		return NAME;
	}
	
	public String getSYSTEM() {
		return SYSTEM;
	}
	
	public String getEXTENSION() {
		return EXTENSION;
	}

}
