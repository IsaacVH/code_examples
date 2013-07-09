package components;

import java.io.File;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Database {

	public String PATH;
	public boolean ERROR;
	public String ERROR_SOURCE;

	// ***Header Files***
	public static String systemName; // The Name of the Game System
	private static String lastListUpdate; // The Last List Update
	private static String listVersion; // The List Version
	private static String exporterVersion; // The Exporter Version
	// ******************
	public String[] gameDatabase; // Simple list of games
	public static String[][] gameDatabase2; // The list of games with details
	public static int numberOfGames; // # in systemName's database
	public Document dataBaseXML; // The actual XML document

	public Database() {
		PATH = null;
		ERROR = false;
		ERROR_SOURCE = "";

		systemName = "NotSpecified";
		lastListUpdate = "NotSpecified";
		listVersion = "NotSpecified";
		exporterVersion = "NotSpecified";
	}

	// pathNm is the Path to the Original HyperSpin XML database
	public Database(String pathNm) {

		PATH = pathNm;
		ERROR = false;
		ERROR_SOURCE = "";

		// Need this to get info out of string.
		int slash = pathNm.lastIndexOf("\\") + 1;
		int dot = pathNm.indexOf(".");
		systemName = pathNm.substring(slash, dot);

		// Now trying to load database files
		try {
			File input = new File(pathNm);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(input);
			doc.getDocumentElement().normalize();
			dataBaseXML = doc;

			String[] root = new String[1];
			root[0] = doc.getDocumentElement().getNodeName();

			// Using a list of Nodes to get the header Node.
			NodeList headerLst = doc.getElementsByTagName("header");
			Node headNode = headerLst.item(0);

			if (headNode != null) {
				// Checking header Node for child nodes.
				Element headElmnt = (Element) headNode;

				// First Child Element of Header: List Name.
				NodeList listNmElmntLst = headElmnt
						.getElementsByTagName("listname");
				Element listNmElmnt = (Element) listNmElmntLst.item(0);
				if (listNmElmnt != null) {
					NodeList listNm = listNmElmnt.getChildNodes();
					systemName = ((Node) listNm.item(0)).getNodeValue();
				}

				// Second Child Element of Header: Last List Update.
				NodeList lluElmntLst = headElmnt
						.getElementsByTagName("lastlistupdate");
				Element lluElmnt = (Element) lluElmntLst.item(0);
				if (lluElmnt != null) {
					NodeList llu = lluElmnt.getChildNodes();
					lastListUpdate = ((Node) llu.item(0)).getNodeValue();
				}

				// Third Child Element of Header: List Version
				NodeList lstVerElmntLst = headElmnt
						.getElementsByTagName("listversion");
				Element lstVerElmnt = (Element) lstVerElmntLst.item(0);
				if (lstVerElmnt != null) {
					NodeList lstVer = lstVerElmnt.getChildNodes();
					listVersion = ((Node) lstVer.item(0)).getNodeValue();
				}

				// Third Child Element of Header: Exporter Version
				NodeList expVerElmntLst = headElmnt
						.getElementsByTagName("exporterversion");
				Element expVerElmnt = (Element) expVerElmntLst.item(0);
				if (expVerElmnt != null) {
					NodeList expVer = expVerElmnt.getChildNodes();
					exporterVersion = ((Node) expVer.item(0)).getNodeValue();
				}

			}
			// End Checking Header Node.

			// Begin Checking Game Database.
			NodeList nodeLst = doc.getElementsByTagName("game");

			// Make 2D Game Database Array
			/*
			 * gameArray Rules: gameArray[i][0] -> "name" attribute of game 'i'.
			 * gameArray[i][1] -> "description" of game 'i' (else null).
			 * gameArray[i][2] -> "cloneOf" of game 'i' (else null).
			 * gameArray[i][3] -> "CRC" of game 'i' (else null). gameArray[i][4]
			 * -> "manufacturer" of game 'i' (else null). gameArray[i][5] ->
			 * "year" of game 'i' (else null). gameArray[i][6] -> "genre" of
			 * game 'i' (else null). gameArray[i][7] -> "rating" of game 'i'
			 * (else null). gameArray[i][8] -> "enabled" of game 'i' ('Yes' or
			 * 'No').
			 */
			String[][] gameArray = new String[nodeLst.getLength()][9];

			// The array shown above will be our go-to source
			// for the dipslayed info.
			for (int s = 0; s < nodeLst.getLength(); s++) {

				Node fstNode = nodeLst.item(s);

				if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

					Element fstElmnt = (Element) fstNode;

					// Game Name (Attribute of "game" node)
					String gameName = fstElmnt.getAttribute("name");
					if (gameName != null) {
						gameArray[s][0] = gameName;
					}

					// Game Description (Usually same as Name w/o, (USA),
					// (JAPAN), etc...)
					NodeList desElmntLst = fstElmnt
							.getElementsByTagName("description");
					Element desElmnt = (Element) desElmntLst.item(0);
					if (desElmnt != null) {
						NodeList des = desElmnt.getChildNodes();
						if (des.item(0) != null) {
							gameArray[s][1] = ((Node) des.item(0))
									.getNodeValue();
						}
					}

					// Clone of (Boolean Value)
					NodeList clnOfElmntLst = fstElmnt
							.getElementsByTagName("cloneof");
					Element clnOfElmnt = (Element) clnOfElmntLst.item(0);
					if (clnOfElmnt != null) {
						NodeList clnOf = clnOfElmnt.getChildNodes();
						if (clnOf.item(0) != null) {
							gameArray[s][2] = ((Node) clnOf.item(0))
									.getNodeValue();
						}
					}

					// CRC (???)
					NodeList crcElmntLst = fstElmnt.getElementsByTagName("crc");
					Element crcElmnt = (Element) crcElmntLst.item(0);
					if (crcElmnt != null) {
						NodeList crc = crcElmnt.getChildNodes();
						if (crc.item(0) != null) {
							gameArray[s][3] = ((Node) crc.item(0))
									.getNodeValue();
						}
					}

					// Manufacturer (Company Name)
					NodeList manufacElmntLst = fstElmnt
							.getElementsByTagName("manufacturer");
					Element manufacElmnt = (Element) manufacElmntLst.item(0);
					if (manufacElmnt != null) {
						NodeList manufac = manufacElmnt.getChildNodes();
						if (manufac.item(0) != null) {
							gameArray[s][4] = ((Node) manufac.item(0))
									.getNodeValue();
						}
					}

					// Year
					NodeList yrElmntLst = fstElmnt.getElementsByTagName("year");
					Element yrElmnt = (Element) yrElmntLst.item(0);
					if (yrElmnt != null) {
						NodeList yr = yrElmnt.getChildNodes();
						if (yr.item(0) != null) {
							gameArray[s][5] = ((Node) yr.item(0))
									.getNodeValue();
						}
					}

					// Genre
					NodeList gnreElmntLst = fstElmnt
							.getElementsByTagName("genre");
					Element gnreElmnt = (Element) gnreElmntLst.item(0);
					if (gnreElmnt != null) {
						NodeList gnre = gnreElmnt.getChildNodes();
						if (gnre.item(0) != null) {
							gameArray[s][6] = ((Node) gnre.item(0))
									.getNodeValue();
						}
					}

					// Rating
					NodeList rtngElmntLst = fstElmnt
							.getElementsByTagName("rating");
					Element rtngElmnt = (Element) rtngElmntLst.item(0);
					if (rtngElmnt != null) {
						NodeList rtng = rtngElmnt.getChildNodes();
						if (rtng.item(0) != null) {
							gameArray[s][7] = ((Node) rtng.item(0))
									.getNodeValue();
						}
					}

					// Enabled (Boolean)
					NodeList enbldElmntLst = fstElmnt
							.getElementsByTagName("enabled");
					Element enbldElmnt = (Element) enbldElmntLst.item(0);
					if (enbldElmnt != null) {
						NodeList enbld = enbldElmnt.getChildNodes();
						if (enbld.item(0) != null) {
							gameArray[s][8] = ((Node) enbld.item(0))
									.getNodeValue();
						}
					}

				}// End IF statement
			}// end FOR loop

			numberOfGames = nodeLst.getLength();
			gameDatabase2 = gameArray;
			gameDatabase = new String[nodeLst.getLength()];
			for (int i = 0; i < nodeLst.getLength(); i++) {
				gameDatabase[i] = gameDatabase2[i][0];
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"<html>ERROR!<br>Could not find:<br> " + PATH
							+ "</html>");
			ERROR = true;
			ERROR_SOURCE = "Failed to Open " + PATH;
		}
	}

	//Some get/set functions.
	public static String getLastListUpdate() {
		return lastListUpdate;
	}

	public static void setLastListUpdate(String lastListUpdate) {
		Database.lastListUpdate = lastListUpdate;
	}

	public static String getListVersion() {
		return listVersion;
	}

	public static void setListVersion(String listVersion) {
		Database.listVersion = listVersion;
	}

	public static String getExporterVersion() {
		return exporterVersion;
	}

	public static void setExporterVersion(String exporterVersion) {
		Database.exporterVersion = exporterVersion;
	}
}