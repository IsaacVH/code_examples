package components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

// This is the main program class. All of the UI and 
// display functions are here.
public class AddROM extends WindowAdapter implements ActionListener {

	final static Preferences prefs = Preferences
			.userNodeForPackage(AddROM.class);

	// Original Databases Path
	static String PATH = "C:\\HyperSpin"; 					//Path to HyperSpin program location.
	static String ODBPATH = PATH + "\\Original Databases";	//Path to original databases.
	static String DBPATH = PATH + "\\Databases";			//Path to current databases.
	static String SYSTEM;	 	// Name of system that game is for.
	static String imagePath; 	// Artwork Label image's Path

	static Game GAME1;	//Create 2 game instances.
	static Game GAME2;	//

	static int lastSelectedList = 0;		// Most of these helper variables
	static String[] globalAllList;			// are self-explanatory by name.
	static String[] globalLibraryList;		// And are primarily used to keep
	static Database allGamesList;			// track of related information.
	static Database libraryGamesList;		// 
	static boolean modified = false;		//
	static JFrame globalFrame;				//

	// Create Yes/No image icons. For positive/negative feedback.
	final static ImageIcon yesImage = new ImageIcon(
			AddROM.class.getResource("Yes.gif"));
	final static ImageIcon noImage = new ImageIcon(
			AddROM.class.getResource("No.gif"));
	final static ImageIcon noImg = new ImageIcon(
			AddROM.class.getResource("NoImage.png"));
	final static ImageIcon openFolder = new ImageIcon(
			AddROM.class.getResource("OpenFolder.png"));
	final static JFrame frame = new JFrame("AddROM - A HyperSpin Tool");

	static JProgressBar jpb = null;

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	// This will display the GUI
	private static void createAndShowGUI() {

		final JPanel jPan1 = new JPanel(new GridBagLayout());

		ImageIcon frame_img = new ImageIcon();
		ImageIcon addRom = new ImageIcon();
		ImageIcon removeRom = new ImageIcon();

		try {
			// Load Images.
			ImageIcon img1 = new ImageIcon(
					AddROM.class.getResource("ROMPlus.gif"));
			frame_img = img1;

			ImageIcon img2 = new ImageIcon(
					AddROM.class.getResource("AddGame.gif"));
			addRom = img2;

			ImageIcon img3 = new ImageIcon(
					AddROM.class.getResource("RemoveGame.gif"));
			removeRom = img3;

		} catch (Exception e) {
			System.out.println("Failed to open image file!");
			// e.printStackTrace();
		}

		// Create and set up the window.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(frame_img.getImage());

		// Create the menu bar.
		JMenuBar menuBar;
		JMenu menu1, menu2;
		JMenuItem menuItem1, menuItem2;

		menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setPreferredSize(new Dimension(200, 20));

		menu1 = new JMenu("File");
		menu1.getAccessibleContext().setAccessibleDescription("First Menu");
		{

			menuItem1 = new JMenuItem("Exit", KeyEvent.VK_X);
			menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
					ActionEvent.ALT_MASK));
			menuItem1.getAccessibleContext().setAccessibleDescription(
					"Exit the program.");
			menuItem1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					prefs.put("root", PATH);
					prefs.put("databases", DBPATH);
					prefs.put("original", ODBPATH);
					System.exit(0);
				}
			});
			menu1.add(menuItem1);
		}

		menu2 = new JMenu("Help");
		menu2.getAccessibleContext().setAccessibleDescription("Help Menu");
		{
			menuItem2 = new JMenuItem("About", KeyEvent.VK_A);
			menuItem2.addActionListener(new java.awt.event.ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog(frame, "<html>Created by:"
							+ "<br>Isaac N. Van Houten<br>"
							+ "<br>Date: July, 2012");
				}

			});
			menu2.add(menuItem2);
		}

		menuBar.add(menu1);
		menuBar.add(menu2);

		// Get the main Menu XML list
		Database db0 = new Database(DBPATH + "\\Main Menu\\Main Menu.xml");

		// * ALL JComponent Items *
		// ************************
		final JLabel artworkName;
		final JLabel artwork;

		// Create a two JLists to hold games from selected system
		final JList<String> gameChoose = new JList<String>();
		final JList<String> gamesChosen = new JList<String>();

		final JComboBox<String> systemChoose;

		final JButton cleanLibrary;
		final JButton add;
		final JButton remove;

		final JTextField allSearchBar;
		final JTextField librarySearchBar;

		// Two Scroll Panes to hold the JLists
		final JScrollPane gameScrollPane = new JScrollPane(gameChoose);
		final JScrollPane gameChosenPane = new JScrollPane(gamesChosen);

		final JLabel gamePathLabel = new JLabel("Game File:");
		final JTextField gamePath = new JTextField("");
		final JLabel videoPathLabel = new JLabel("Video File:");
		final JTextField videoPath = new JTextField("");

		final JLabel gamePathYesOrNo = new JLabel(noImage);
		final JLabel videoPathYesOrNo = new JLabel(noImage);

		final JButton openGameFolder = new JButton(openFolder);
		final JButton openVideoFolder = new JButton(openFolder);
		// ************************

		// Create some labels for the artwork later on
		artworkName = new JLabel("Artwork:");

		Border blackline = BorderFactory.createLineBorder(Color.black);

		artwork = new JLabel(noImg, JLabel.CENTER);
		artwork.setEnabled(false);
		artwork.setPreferredSize(new Dimension(300, 150));
		artwork.setBorder(blackline);
		artwork.addMouseListener(new MouseListener() {

			// These are event listeners, most of which are auto-generated
			// by Eclipse and the event listener library used.
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (artwork.isEnabled()) {
					Game game = null;
					if (lastSelectedList == 1) {
						game = GAME1;
					} else if (lastSelectedList == 2) {
						game = GAME2;
					}
					if (game != null) {

						String[] info = { game.getARTPATH(), ".png",
								"Choose a new image for this game's title: ",
								"PNG image file ", "Picture" };
						String[] fileInfo = openFolder(frame, info);

						if (fileInfo[0] != null) {
							moveFile(game.getARTPATH(), fileInfo, jPan1);
						}

						refresh(gameChoose, gamesChosen, gamePathYesOrNo,
								videoPathYesOrNo, gamePath, videoPath, artwork,
								openGameFolder, openVideoFolder);
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				artwork.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent arg0) {

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {

			}

		});

		// Set some textFields for paths to games and artwork
		gamePathLabel.setPreferredSize(new Dimension(275, 25));
		gamePathYesOrNo.setPreferredSize(new Dimension(20, 25));

		gamePath.setPreferredSize(new Dimension(270, 25));
		gamePath.setBackground(new Color(220, 220, 220));
		gamePath.setEditable(false);

		videoPathLabel.setPreferredSize(new Dimension(275, 25));
		videoPathYesOrNo.setPreferredSize(new Dimension(20, 25));

		videoPath.setPreferredSize(new Dimension(270, 25));
		videoPath.setBackground(new Color(220, 220, 220));
		videoPath.setEditable(false);

		// Change the lists that hold games from selected system
		// This list holds the games that are in the xml list,
		// but not in the user's library.
		gameChoose.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				lastSelectedList = 1;
				refresh(gameChoose, gamesChosen, gamePathYesOrNo,
						videoPathYesOrNo, gamePath, videoPath, artwork,
						openGameFolder, openVideoFolder);
			}

		});
		gameChoose.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {

				lastSelectedList = 1;
				gamesChosen.clearSelection();
				refresh(gameChoose, gamesChosen, gamePathYesOrNo,
						videoPathYesOrNo, gamePath, videoPath, artwork,
						openGameFolder, openVideoFolder);
			}

			@Override
			public void focusLost(FocusEvent arg0) {

			}

		});

		// This list holds games that are already in user's library.
		gamesChosen.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				lastSelectedList = 2;
				refresh(gameChoose, gamesChosen, gamePathYesOrNo,
						videoPathYesOrNo, gamePath, videoPath, artwork,
						openGameFolder, openVideoFolder);
			}

		});
		gamesChosen.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {

				lastSelectedList = 2;
				gameChoose.clearSelection();
				refresh(gameChoose, gamesChosen, gamePathYesOrNo,
						videoPathYesOrNo, gamePath, videoPath, artwork,
						openGameFolder, openVideoFolder);
			}

			@Override
			public void focusLost(FocusEvent arg0) {

			}

		});

		// ******Create a ComboBox (Drop-down menu)******
		systemChoose = new JComboBox<String>(db0.gameDatabase);
		systemChoose.setBackground(new Color(255, 255, 255));
		systemChoose.setPreferredSize(new Dimension(300, 25));
		systemChoose.setSelectedIndex(-1);
		systemChoose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String chosenSystem;
				chosenSystem = (String) systemChoose.getSelectedItem();

				artwork.setIcon(noImg);
				artwork.setEnabled(false);
				gameScrollPane.getVerticalScrollBar().setValue(0);
				gameScrollPane.getHorizontalScrollBar().setValue(0);
				gamePath.setText("");
				gamePathYesOrNo.setIcon(noImage);
				openGameFolder.setEnabled(false);
				videoPath.setText("");
				videoPathYesOrNo.setIcon(noImage);
				openVideoFolder.setEnabled(false);

				if (systemChoose.getSelectedIndex() != -1) {
					buildDatabases(chosenSystem);

					SYSTEM = chosenSystem;

					gameChosenPane.getVerticalScrollBar().setValue(0);
					gameChosenPane.getHorizontalScrollBar().setValue(0);

					gameChoose.setListData(globalAllList);
					gamesChosen.setListData(globalLibraryList);
				}
			}
		});
		// **********************************************

		// Make a progress bar for the cleanlibrary button
		jpb = new JProgressBar(0, 100);
		jpb.setPreferredSize(new Dimension(165, 25));
		jpb.setStringPainted(false);
		jpb.setForeground(new Color(102, 205, 170));

		// Use JButton's ActionListener to display games when pressed
		cleanLibrary = new JButton("Clean-Up Files");
		cleanLibrary.setPreferredSize(new Dimension(134, 25));
		cleanLibrary.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {

				jpb.setValue(0);
				jpb.setStringPainted(true);

				if (systemChoose.getSelectedIndex() != -1) {
					JPanel jPan3 = new JPanel(new GridBagLayout());

					JLabel messageLabel = new JLabel(
							"<html>Select the resources from the "
									+ "inactive games<br>"
									+ "that you want to delete."
									+ "<br></html>");

					JRadioButton jrb1 = new JRadioButton("Game Files");
					jrb1.setEnabled(false);
					jrb1.setSelected(true);
					JRadioButton jrb2 = new JRadioButton("Video Files");
					JRadioButton jrb3 = new JRadioButton("Art Files");

					GridBagConstraints j = new GridBagConstraints();

					j.anchor = GridBagConstraints.WEST;

					j.gridx = 0;
					j.gridy = 0;
					jPan3.add(messageLabel, j);

					j.gridx = 0;
					j.gridy = 1;
					jPan3.add(jrb1, j);
					j.gridx = 0;
					j.gridy = 2;
					jPan3.add(jrb2, j);
					j.gridx = 0;
					j.gridy = 3;
					jPan3.add(jrb3, j);

					String title = "Remove Unused Resources";
					String[] options = { "Delete Files", "Cancel" };

					int dVal = JOptionPane.showOptionDialog(jPan1, jPan3,
							title, JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[1]);

					if (dVal == 0) {

						int vid = 0, art = 0;

						String message = "<html>Are you sure?<br>"
								+ "(Files cannot be recovered!)<br><br>"
								+ "Delete:<br>+ ROMs";
						if (jrb2.isSelected()) {
							message += "<br>+ Videos";
							vid = 1;
						}
						if (jrb3.isSelected()) {
							message += "<br>+ Art";
							art = 2;
						}
						message += "</html>";

						JLabel mLabel = new JLabel(message);

						int yVal = JOptionPane.showConfirmDialog(jPan1, mLabel,
								title, JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);

						if (yVal == 0) {
							int type = 0;
							type = type + vid + art;
							cleanUp(gameChoose.getModel(),
									(String) systemChoose.getSelectedItem(),
									type);
						}

						refresh(gameChoose, gamesChosen, gamePathYesOrNo,
								videoPathYesOrNo, gamePath, videoPath, artwork,
								openGameFolder, openVideoFolder);
					}

				} else {
					JOptionPane.showMessageDialog(jPan1,
							"You haven't chosen a System!");
				}
				jpb.setValue(0);
				jpb.setStringPainted(false);
			}
		});

		// Some "ADD" and "REMOVE" buttons.
		add = new JButton(addRom);
		add.setPreferredSize(new Dimension(35, 40));
		add.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {

				boolean openFile = true;
				if (gameChoose.getSelectedIndex() != -1) {
					Game game = GAME1;

					String[] info = { game.getGAMEPATH(),
							"." + game.getEXTENSION(),
							"Choose a new ROM file for this game: ",
							SYSTEM + " ROM ", "Download" };
					String[] fileInfo = openFolder(frame, info);

					if (fileInfo[0] != null) {
						moveFile(game.getGAMEPATH(), fileInfo, jPan1);
					}

					if (openFile) {

						Element outcast = null;
						outcast = searchDocument(gameChoose.getSelectedValue(),
								allGamesList.dataBaseXML);

						String outcastName = outcast.getAttribute("name");
						if (outcast != null) {
							copyNode((Element) libraryGamesList.dataBaseXML
									.getElementsByTagName("menu").item(0),
									outcast, libraryGamesList.dataBaseXML);
						}

						writeXML(libraryGamesList);

						String chosenSystem;
						chosenSystem = (String) systemChoose.getSelectedItem();

						buildDatabases((String) systemChoose.getSelectedItem());
						SYSTEM = chosenSystem;

						gameChosenPane.getVerticalScrollBar().setValue(0);
						gameChosenPane.getHorizontalScrollBar().setValue(0);

						gameChoose.setListData(globalAllList);
						gamesChosen.setListData(globalLibraryList);

						refresh(gameChoose, gamesChosen, gamePathYesOrNo,
								videoPathYesOrNo, gamePath, videoPath, artwork,
								openGameFolder, openVideoFolder);

						gamesChosen.setSelectedValue(outcastName, true);
					}
				} else {
					JOptionPane.showMessageDialog(frame,
							"You haven't chosen a game to add!");
				}
			}
		});

		// Button to Remove ROM from user's library.
		remove = new JButton(removeRom);
		remove.setPreferredSize(new Dimension(35, 40));
		remove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean deleteFile = true;
				if (gamesChosen.getSelectedIndex() != -1) {
					if (deleteFile) {

						Element outcast = null;
						outcast = searchDocument(
								gamesChosen.getSelectedValue(),
								libraryGamesList.dataBaseXML);

						String outcastName = outcast.getAttribute("name");
						if (outcast != null) {
							libraryGamesList.dataBaseXML
									.getElementsByTagName("menu").item(0)
									.removeChild(outcast);
						}

						writeXML(libraryGamesList);

						String chosenSystem;
						chosenSystem = (String) systemChoose.getSelectedItem();

						buildDatabases((String) systemChoose.getSelectedItem());
						SYSTEM = chosenSystem;

						gameChosenPane.getVerticalScrollBar().setValue(0);
						gameChosenPane.getHorizontalScrollBar().setValue(0);

						gameChoose.setListData(globalAllList);
						gamesChosen.setListData(globalLibraryList);

						refresh(gameChoose, gamesChosen, gamePathYesOrNo,
								videoPathYesOrNo, gamePath, videoPath, artwork,
								openGameFolder, openVideoFolder);

						gameChoose.setSelectedValue(outcastName, true);
					}
				} else {
					JOptionPane.showMessageDialog(frame,
							"You haven't chosen a game to remove!");
				}
			}

		});

		// Two more buttons for opening the ROM-FOLDER and VIDEO-FOLDER
		openGameFolder.setPreferredSize(new Dimension(29, 25));
		openGameFolder.setToolTipText("Add a new ROM file for this game");
		openGameFolder.setEnabled(false);
		openGameFolder.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Game game = null;
				if (lastSelectedList == 1) {
					game = GAME1;
				} else if (lastSelectedList == 2) {
					game = GAME2;
				}
				if (game != null) {

					String[] info = { gamePath.getText(),
							"." + game.getEXTENSION(),
							"Choose a new ROM file for this game: ",
							SYSTEM + " ROM ", "Download" };
					String[] fileInfo = openFolder(frame, info);

					if (fileInfo[0] != null) {
						moveFile(game.getGAMEPATH(), fileInfo, jPan1);
					}

					refresh(gameChoose, gamesChosen, gamePathYesOrNo,
							videoPathYesOrNo, gamePath, videoPath, artwork,
							openGameFolder, openVideoFolder);
				}
			}

		});
		openVideoFolder.setPreferredSize(new Dimension(29, 25));
		openVideoFolder.setToolTipText("Add a new Preview-"
				+ "Video for this game");
		openVideoFolder.setEnabled(false);
		openVideoFolder.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Game game = null;
				if (lastSelectedList == 1) {
					game = GAME1;
				} else if (lastSelectedList == 2) {
					game = GAME2;
				}

				if (game != null) {

					String[] info = { videoPath.getText(), ".flv",
							"Choose a Preview Video: ", "Flash Video File ",
							"Video" };
					String[] fileInfo = openFolder(frame, info);

					if (fileInfo[0] != null) {
						moveFile(game.getVIDEOPATH(), fileInfo, jPan1);
					}

					refresh(gameChoose, gamesChosen, gamePathYesOrNo,
							videoPathYesOrNo, gamePath, videoPath, artwork,
							openGameFolder, openVideoFolder);
				}
			}

		});

		// NEW STUFFFF
		// All Search Bar Setup
		allSearchBar = new JTextField("");
		allSearchBar.setPreferredSize(new Dimension(300, 25));
		allSearchBar.setEditable(true);
		allSearchBar.setText("Search");
		allSearchBar.setForeground(new Color(128, 128, 128));
		allSearchBar.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
				if (systemChoose.getSelectedIndex() != -1) {
					gameChoose.setListData(globalAllList);
				}
				allSearchBar.setText("");
				allSearchBar.setForeground(new Color(0, 0, 0));
			}

			public void focusLost(FocusEvent arg0) {
				allSearchBar.setText("Search");
				allSearchBar.setForeground(new Color(128, 128, 128));
			}

		});
		allSearchBar.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {

			}

			public void keyReleased(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {
				String term;
				if (Character.isLetter(e.getKeyChar())
						|| Character.isDigit(e.getKeyChar())) {
					term = allSearchBar.getText() + e.getKeyChar();
				} else {
					term = allSearchBar.getText();
				}

				if (gameChoose.getSelectedIndex() != -1) {
					gamePathYesOrNo.setIcon(noImage);
					gamePath.setText("");
					openGameFolder.setEnabled(false);
					videoPathYesOrNo.setIcon(noImage);
					videoPath.setText("");
					openVideoFolder.setEnabled(false);
					artwork.setIcon(noImg);
					artwork.setEnabled(false);
				}
				String[] temp = filterList(term, globalAllList);
				gameChoose.clearSelection();
				gameChoose.setListData(temp);
			}

		});

		// Library Search Bar Setup
		librarySearchBar = new JTextField("");
		librarySearchBar.setPreferredSize(new Dimension(300, 25));
		librarySearchBar.setEditable(true);
		librarySearchBar.setText("Search");
		librarySearchBar.setForeground(new Color(128, 128, 128));
		librarySearchBar.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent arg0) {
				if (systemChoose.getSelectedIndex() != -1) {
					gamesChosen.setListData(globalLibraryList);
				}
				librarySearchBar.setText("");
				librarySearchBar.setForeground(new Color(0, 0, 0));
			}

			public void focusLost(FocusEvent arg0) {
				librarySearchBar.setText("Search");
				librarySearchBar.setForeground(new Color(128, 128, 128));
			}

		});
		librarySearchBar.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {

			}

			public void keyReleased(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {
				String term;
				if (Character.isLetter(e.getKeyChar())
						|| Character.isDigit(e.getKeyChar())) {
					term = librarySearchBar.getText() + e.getKeyChar();
				} else {
					term = librarySearchBar.getText();
				}

				if (gamesChosen.getSelectedIndex() != -1) {
					gamePathYesOrNo.setIcon(noImage);
					gamePath.setText("");
					openGameFolder.setEnabled(false);
					videoPathYesOrNo.setIcon(noImage);
					videoPath.setText("");
					openVideoFolder.setEnabled(false);
					artwork.setIcon(noImg);
					artwork.setEnabled(false);
				}

				String[] temp = filterList(term, globalLibraryList);
				gamesChosen.clearSelection();
				gamesChosen.setListData(temp);
			}

		});
		// NEW STUFFFFFFFFFFFF

		// Create a JScrollPane to hold the generated list.
		gameScrollPane.setPreferredSize(new Dimension(300, 150));
		gameChosenPane.setPreferredSize(new Dimension(300, 150));

		// Create a few labels for our Components
		JLabel scLabel = new JLabel("Choose System: ");
		JLabel gspLabel = new JLabel("All Games (Inactive)");
		JLabel gcpLabel = new JLabel("Games in your Library (Active)");

		// Create some blank labels to act as spaces.

		// EDGE SPACES
		JLabel edgeSpace1 = new JLabel("");
		edgeSpace1.setPreferredSize(new Dimension(10, 10));
		JLabel edgeSpace2 = new JLabel("");
		edgeSpace2.setPreferredSize(new Dimension(10, 0));

		// INTERIOR SPACES
		JLabel centerSpace1 = new JLabel("");
		centerSpace1.setPreferredSize(new Dimension(40, 25));
		JLabel centerSpace2 = new JLabel("");
		centerSpace2.setPreferredSize(new Dimension(25, 10));

		// INTERIOR "ADD"/"REMOVE" BUTTON SPACES
		JLabel centerSpace3 = new JLabel("");
		centerSpace3.setPreferredSize(new Dimension(40, 75));
		JLabel centerSpace4 = new JLabel("");
		centerSpace4.setPreferredSize(new Dimension(40, 75));

		// Create a JPanel to hold lists and stuff. (TOP LEVEL)
		GridBagConstraints c = new GridBagConstraints();

		// Customize the Layout of jPan1 using GridBag
		// ***********Edge Space*************
		c.fill = GridBagConstraints.VERTICAL;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		jPan1.add(edgeSpace1, c);
		c.fill = 0;
		// **********************************

		c.gridx = 1;
		c.gridy = 1;
		jPan1.add(scLabel, c);

		c.gridx = 1;
		c.gridy = 2;
		jPan1.add(systemChoose, c);

		c.gridx = 3;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		jPan1.add(cleanLibrary, c);

		c.anchor = GridBagConstraints.EAST;
		jPan1.add(jpb, c);
		c.anchor = GridBagConstraints.CENTER;

		// *********Add in a Space***********
		c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 2;
		c.gridy = 3;
		jPan1.add(centerSpace1, c);
		c.fill = 0;
		// **********************************

		// CenterSpace3
		c.gridx = 2;
		c.gridy = 6;
		jPan1.add(centerSpace3, c);

		// "Add" Button
		c.gridx = 2;
		c.gridy = 6;
		jPan1.add(add, c);

		// CenterSpace4
		c.gridx = 2;
		c.gridy = 7;
		jPan1.add(centerSpace4, c);

		// "Remove" Button
		c.gridx = 2;
		c.gridy = 7;
		c.anchor = GridBagConstraints.NORTH;
		jPan1.add(remove, c);
		c.anchor = GridBagConstraints.CENTER;

		// Label for All Games.
		c.gridx = 1;
		c.gridy = 4;
		jPan1.add(gspLabel, c);

		// Search bar for All Games.
		c.gridx = 1;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		jPan1.add(allSearchBar, c);
		c.anchor = GridBagConstraints.CENTER;

		// Scroll Pane for All Games.
		c.gridx = 1;
		c.gridy = 6;
		c.gridheight = 2;
		jPan1.add(gameScrollPane, c);
		c.gridheight = 1;

		// Label for Library Games.
		c.gridx = 3;
		c.gridy = 4;
		jPan1.add(gcpLabel, c);

		// Search bar for Library Games.
		c.gridx = 3;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		jPan1.add(librarySearchBar, c);
		c.anchor = GridBagConstraints.CENTER;

		// Scroll Pane for Library Games.
		c.gridx = 3;
		c.gridy = 6;
		c.gridheight = 2;
		jPan1.add(gameChosenPane, c);
		c.gridheight = 1;

		// *******Add in another Space*******
		c.gridx = 2;
		c.gridy = 8;
		jPan1.add(centerSpace2, c);
		c.fill = 0;
		// **********************************

		// ***********Edge Space*************
		c.fill = GridBagConstraints.VERTICAL;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 4;
		c.gridy = 8;
		jPan1.add(edgeSpace2, c);
		c.fill = 0;
		// **********************************
		// jPan1 Layout Customization End

		// Create some components for the Second JPanel.

		// Some space Labels
		JLabel largeEdgeSpace1 = new JLabel("");
		largeEdgeSpace1.setPreferredSize(new Dimension(300, 0));

		JLabel largeEdgeSpace2 = new JLabel("");
		largeEdgeSpace2.setPreferredSize(new Dimension(300, 0));

		JLabel edgeSpace3 = new JLabel("");
		edgeSpace3.setPreferredSize(new Dimension(10, 0));

		JLabel edgeSpace4 = new JLabel("");
		edgeSpace4.setPreferredSize(new Dimension(40, 10));

		JLabel edgeSpace5 = new JLabel("");
		edgeSpace5.setPreferredSize(new Dimension(10, 0));

		JLabel edgeSpace6 = new JLabel("");
		edgeSpace6.setPreferredSize(new Dimension(10, 30));

		// jPan2 Layout Customization
		// Create a JPanel to hold search bars and other Stuff (LOWER LEVEL)
		JPanel jPan2 = new JPanel(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();

		// ***********EDGE SPACE*************
		gbc2.fill = GridBagConstraints.VERTICAL;
		gbc2.gridx = 0;
		gbc2.gridy = 0;
		jPan2.add(edgeSpace3, gbc2);
		gbc2.fill = GridBagConstraints.NONE;
		// **********************************

		// ***********EDGE SPACE*************
		gbc2.gridx = 1;
		gbc2.gridy = 0;
		jPan2.add(largeEdgeSpace1, gbc2);
		// **********************************

		// Images for yes or no artwork and gamefile
		gbc2.gridx = 1;
		gbc2.gridy = 2;
		gbc2.anchor = GridBagConstraints.WEST;
		jPan2.add(gamePathYesOrNo, gbc2);

		gbc2.gridx = 1;
		gbc2.gridy = 5;
		jPan2.add(videoPathYesOrNo, gbc2);

		// Label for the gamePathTextField
		gbc2.gridx = 1;
		gbc2.gridy = 2;
		gbc2.anchor = GridBagConstraints.EAST;
		jPan2.add(gamePathLabel, gbc2);

		// The gamePathTextField
		gbc2.gridx = 1;
		gbc2.gridy = 3;
		gbc2.anchor = GridBagConstraints.WEST;
		jPan2.add(gamePath, gbc2);

		// The JButton to open the gameFolder
		gbc2.gridx = 1;
		gbc2.gridy = 3;
		gbc2.anchor = GridBagConstraints.EAST;
		jPan2.add(openGameFolder, gbc2);

		// A Little space between the two textFields
		gbc2.gridx = 1;
		gbc2.gridy = 4;
		gbc2.anchor = GridBagConstraints.WEST;
		jPan2.add(edgeSpace6, gbc2);

		// The videoPathLabel
		gbc2.gridx = 1;
		gbc2.gridy = 5;
		gbc2.anchor = GridBagConstraints.EAST;
		jPan2.add(videoPathLabel, gbc2);

		// The JButton to open the videoFolder
		gbc2.gridx = 1;
		gbc2.gridy = 6;
		gbc2.anchor = GridBagConstraints.NORTHEAST;
		jPan2.add(openVideoFolder, gbc2);

		// The videoPathTextField
		gbc2.gridx = 1;
		gbc2.gridy = 6;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		jPan2.add(videoPath, gbc2);
		gbc2.anchor = GridBagConstraints.CENTER;

		// ***********EDGE SPACE*************
		gbc2.gridx = 3;
		gbc2.gridy = 0;
		jPan2.add(largeEdgeSpace2, gbc2);
		// **********************************

		// Label to name Artwork
		gbc2.gridx = 3;
		gbc2.gridy = 1;
		jPan2.add(artworkName, gbc2);

		// Label to hold Artwork
		gbc2.gridx = 3;
		gbc2.gridy = 2;
		gbc2.gridheight = 5;
		jPan2.add(artwork, gbc2);
		gbc2.gridheight = 1;

		// ***********EDGE SPACE*************
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.fill = GridBagConstraints.VERTICAL;
		gbc2.gridx = 2;
		gbc2.gridy = 7;
		jPan2.add(edgeSpace4, gbc2);
		// **********************************

		// ***********EDGE SPACE*************
		gbc2.fill = GridBagConstraints.VERTICAL;
		gbc2.gridx = 4;
		gbc2.gridy = 0;
		jPan2.add(edgeSpace5, gbc2);
		// **********************************
		// jPan2 Layout Customization

		// Set the menu bar and add the JPanel to the content pane
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(jPan1, BorderLayout.NORTH);
		frame.getContentPane().add(jPan2, BorderLayout.CENTER);

		// Display the window.
		frame.pack();

		// Get the size of the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Determine the new location of the window
		int w = frame.getSize().width;
		int h = frame.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;

		// Move the window
		frame.setLocation(x, y);
		frame.setMinimumSize(new Dimension(frame.getWidth(), frame.getHeight()));

		frame.setVisible(true);
		globalFrame = frame;

	}

	/**
	 * END of GUI Creation
	 */

	// ***************MAIN******************
	public static void main(String[] args) {

		PATH = prefs.get("root", "C:\\HyperSpin");
		DBPATH = prefs.get("databases", PATH + "\\Databases");
		ODBPATH = prefs.get("original", PATH + "\\Original Databases");

		File odb0 = new File(PATH + "\\HyperSpin.exe");

		if (!odb0.exists()) {
			boolean done = false;
			while (!done) {
				JPanel jPan4 = new JPanel(new GridBagLayout());
				setFirstPanel(jPan4, openFolder);
				JOptionPane.showMessageDialog(frame, jPan4);
				if (!new File(PATH + "\\HyperSpin.exe").exists()) {
					JOptionPane.showMessageDialog(frame,
							"You didn't choose a valid folder!");
					done = false;
				} else {
					done = true;
				}
			}
		}
		DBPATH = PATH + "\\Databases";
		ODBPATH = PATH + "\\Original Databases";

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	// *************************************

	// **********************************************************************
	// *FUNCTIONS FOR HELPFUL USE *
	// **********************************************************************
	public static String[] getSystemList(String path) {
		String[] systems;
		File systemFolder = new File(path);
		systems = systemFolder.list();
		return (systems);
	}

	public static void buildDatabases(String chosenSystem) {
		// Build globalAllList & globalLibraryList

		allGamesList = new Database(ODBPATH + "\\" + chosenSystem + "\\"
				+ chosenSystem + ".xml");
		libraryGamesList = new Database(DBPATH + "\\" + chosenSystem + "\\"
				+ chosenSystem + ".xml");

		Vector<String> nonDuplicates = new Vector<String>();
		boolean notDuplicate;

		for (int i = 0; i < allGamesList.gameDatabase.length; i++) {
			notDuplicate = true;
			for (int j = 0; j < libraryGamesList.gameDatabase.length; j++) {
				if (allGamesList.gameDatabase[i]
						.equals(libraryGamesList.gameDatabase[j])) {
					notDuplicate = false;
				}
			}
			if (notDuplicate) {
				nonDuplicates.add(allGamesList.gameDatabase[i]);
			}
		}

		globalAllList = new String[nonDuplicates.size()];
		for (int i = 0; i < nonDuplicates.size(); i++) {
			globalAllList[i] = nonDuplicates.get(i);
		}

		globalLibraryList = libraryGamesList.gameDatabase;
		return;
	}

	public static String[] filterList(String searchBar, String[] list) {

		String librarySearchTerm = searchBar;
		String Temp;
		Vector<String> filteredLibraryVector = new Vector<String>();
		String[] filteredLibraryArray;

		for (int i = 0; i < list.length; i++) {
			Temp = list[i];
			if (Temp.toUpperCase().contains(librarySearchTerm.toUpperCase())) {
				filteredLibraryVector.add(list[i]);
			}
		}

		filteredLibraryArray = new String[filteredLibraryVector.size()];
		for (int i = 0; i < filteredLibraryVector.size(); i++) {
			filteredLibraryArray[i] = filteredLibraryVector.get(i);
		}

		return filteredLibraryArray;
	}

	public static ImageIcon getArtwork(JLabel artwork, Game selectedGame) {

		BufferedImage art = null;
		imagePath = selectedGame.getARTPATH();
		if (selectedGame.ARTEXISTS) {
			art = selectedGame.getART();
		} else {
			return (noImg);
		}

		double width = art.getWidth();
		double height = art.getHeight();
		double widthRatio = (artwork.getWidth() / width);
		double heightRatio = (artwork.getHeight() / height);

		double modifier;
		if (widthRatio > heightRatio) {
			modifier = heightRatio;
		} else {
			modifier = widthRatio;
		}

		int newWidth = (int) (modifier * art.getWidth());
		int newHeight = (int) (modifier * art.getHeight());

		BufferedImage resizedImage = new BufferedImage(newWidth, newHeight,
				BufferedImage.TRANSLUCENT);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(art, 0, 0, newWidth, newHeight, null);
		g.dispose();

		ImageIcon realImage = new ImageIcon(resizedImage);
		return (realImage);
	}

	public static void refresh(JList<String> gameChoose,
			JList<String> gamesChosen, JLabel gamePathYesOrNo,
			JLabel videoPathYesOrNo, JTextField gamePath, JTextField videoPath,
			JLabel artwork, JButton openGameFolder, JButton openVideoFolder) {

		if (lastSelectedList == 1) {
			if (gameChoose.getSelectedIndex() == -1 || SYSTEM.equals(null)) {
				gamePathYesOrNo.setIcon(noImage);
				videoPathYesOrNo.setIcon(noImage);

				gamePath.setText("");
				videoPath.setText("");
				openGameFolder.setEnabled(false);
				openVideoFolder.setEnabled(false);
				artwork.setIcon(noImg);
				artwork.setEnabled(false);

			} else {
				GAME1 = new Game(gameChoose.getSelectedValue(), SYSTEM, PATH);
				artwork.setIcon(getArtwork(artwork, GAME1));
				openGameFolder.setEnabled(false);
				openVideoFolder.setEnabled(true);
				artwork.setEnabled(true);

				if (GAME1.EXISTS) {
					gamePathYesOrNo.setIcon(yesImage);
					gamePath.setText(GAME1.getGAMEPATH());
					gamePath.setCaretPosition(0);
				} else {
					gamePathYesOrNo.setIcon(noImage);
					gamePath.setText("");
				}
				if (GAME1.VIDEOEXISTS) {
					videoPathYesOrNo.setIcon(yesImage);
					videoPath.setText(GAME1.getVIDEOPATH());
					videoPath.setCaretPosition(0);
				} else {
					videoPathYesOrNo.setIcon(noImage);
					videoPath.setText("");
				}
			}
		} else if (lastSelectedList == 2) {
			if (gamesChosen.getSelectedIndex() == -1 || SYSTEM.equals(null)) {
				gamePathYesOrNo.setIcon(noImage);
				videoPathYesOrNo.setIcon(noImage);

				gamePath.setText("");
				videoPath.setText("");
				openGameFolder.setEnabled(false);
				openVideoFolder.setEnabled(false);
				artwork.setIcon(noImg);
				artwork.setEnabled(false);

			} else {
				GAME2 = new Game(gamesChosen.getSelectedValue(), SYSTEM, PATH);
				artwork.setIcon(getArtwork(artwork, GAME2));
				openGameFolder.setEnabled(true);
				openVideoFolder.setEnabled(true);
				artwork.setEnabled(true);

				if (GAME2.EXISTS) {
					gamePathYesOrNo.setIcon(yesImage);
					gamePath.setText(GAME2.getGAMEPATH());
					gamePath.setCaretPosition(0);
				} else {
					gamePathYesOrNo.setIcon(noImage);
					gamePath.setText("");

				}
				if (GAME2.VIDEOEXISTS) {
					videoPathYesOrNo.setIcon(yesImage);
					videoPath.setText(GAME2.getVIDEOPATH());
					videoPath.setCaretPosition(0);
				} else {
					videoPathYesOrNo.setIcon(noImage);
					videoPath.setText("");
				}
			}
		}
	}

	public static String[] openFolder(JFrame frame, String[] argv) {

		String path = argv[0];
		final String extension = argv[1];
		String title = argv[2];
		final String description = argv[3];
		String type = argv[4];

		String filename = null;
		String dir = null;
		File dir1 = new File(path);
		File dir2 = new File(System.getProperty("user.home") + "\\" + type
				+ "s");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle(title);
		if (dir1.exists()) {
			jfc.setCurrentDirectory(dir1);
		} else {
			jfc.setCurrentDirectory(dir2);
		}
		FileFilter ff = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.getPath().endsWith(extension) || f.isDirectory()) {
					return (true);
				} else {
					return false;
				}
			}

			@Override
			public String getDescription() {
				return (description + "(*" + extension + ")");
			}

		};
		jfc.setFileFilter(ff);

		int rVal = jfc.showOpenDialog(frame);

		if (rVal == JFileChooser.APPROVE_OPTION) {
			filename = jfc.getSelectedFile().getName();
			dir = jfc.getCurrentDirectory().toString();
		} else if (rVal == JFileChooser.CANCEL_OPTION) {
			filename = null;
			dir = null;
		}

		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		String[] array = { filename, dir };

		return (array);
	}

	public static Element searchDocument(String searchTerm, Document doc) {

		Element outcast = null;
		NodeList nl = doc.getElementsByTagName("game");
		String gName = null;
		for (int i = 0; i < nl.getLength(); i++) {
			gName = nl.item(i).getAttributes().getNamedItem("name")
					.getNodeValue();
			if (gName.equals(searchTerm)) {
				outcast = (Element) nl.item(i);
			}
		}
		return outcast;
	}

	public static void copyNode(Element parent, Element elementToCopy,
			Document parentdoc) {
		Element newElement;

		// create a deep clone for the target document:
		newElement = (Element) parent.getOwnerDocument().importNode(
				elementToCopy, true);

		NodeList nL = parent.getElementsByTagName("game");
		String[] gameList = new String[nL.getLength() + 1];
		for (int i = 0; i < nL.getLength(); i++) {
			gameList[i] = nL.item(i).getAttributes().getNamedItem("name")
					.getNodeValue();
		}

		gameList[gameList.length - 1] = elementToCopy.getAttribute("name");
		List<String> list = Arrays.asList(gameList);
		Collections.sort(list);
		gameList = (String[]) list.toArray();

		int index = 0;
		boolean endOfList = false;
		for (int j = 0; j < gameList.length; j++) {
			if (gameList[j].equals(elementToCopy.getAttribute("name"))) {
				if (j == (gameList.length - 1)) {
					endOfList = true;
				} else {
					endOfList = false;
					index = j + 1;
				}
				j = gameList.length;
			}
		}

		try {
			if (!endOfList) {
				Element comesAfter = searchDocument(gameList[index], parentdoc);
				parent.insertBefore((Node) newElement, (Node) comesAfter);
			} else {
				parent.appendChild(newElement);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean moveFile(String newpathnm, String[] fileInfo,
			JPanel jPan1) {

		String pathnm = fileInfo[1] + "\\" + fileInfo[0];
		try {

			String message = "<html>Do you want to copy, or move,"
					+ " this file?</html>";
			String title = "Copy, or Move";
			String[] options = { "Copy", "Move", "Cancel" };

			int rVal = JOptionPane.showOptionDialog(jPan1, message, title,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

			if (rVal == 0) {
				if (copyfile(pathnm, newpathnm)) {
					// System.out.println("Success " + video.getPath());
					return (true);
				} else {
					// System.out.println("Failed to open Video");
					return (false);
				}
			} else if (rVal == 1) {
				if ((new File(pathnm)).renameTo(new File(newpathnm))) {
					return (true);
				} else {
					return (false);
				}
			} else {
				return (false);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			return (false);
		}
	}

	public static void writeXML(Database db) {
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(db.dataBaseXML);

			// Prepare the output file
			File file = new File(db.PATH);
			Result result = new StreamResult(file);

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);

		} catch (TransformerConfigurationException e1) {
			// System.out.println("Failed to write XML list!");
		} catch (TransformerException e1) {
			// System.out.println("Failed to write XML list!");
		}
	}

	static int i;

	public static void cleanUp(final ListModel<String> games,
			final String system, final int type) {

		new Thread(new Runnable() {
			public void run() {
				jpb.setStringPainted(true);
				for (i = 0; i <= 100; i = i + 1) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							jpb.setValue(i);
						}
					});
					try {
						Game g1 = new Game(games.getElementAt(i), system, PATH);

						if (g1.EXISTS) {
							g1.GAMEFILE.delete();
						}

						if (type == 1) {

							if (g1.VIDEOEXISTS) {
								g1.VIDEOFILE.delete();
							}

						} else if (type == 2) {

							if (g1.ARTEXISTS) {
								g1.ARTFILE.delete();
							}

						} else if (type == 3) {

							if (g1.VIDEOEXISTS) {
								g1.VIDEOFILE.delete();
							}
							if (g1.ARTEXISTS) {
								g1.ARTFILE.delete();
							}

						}
						if (i == 100) {
							jpb.setStringPainted(false);
						}
					} catch (Exception e) {
						//
					}
				}
			}
		}).start();
	}

	private static boolean copyfile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();
			// System.out.println("File copied.");

		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified"
					+ "directory.");
			return (false);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return (false);
		}

		return (true);
	}

	public static String setFirstPanel(final JPanel jPan4, ImageIcon openFolder) {
		JButton openRoot = new JButton(openFolder);
		openRoot.setPreferredSize(new Dimension(30, 27));

		final JTextField pathTxtField = new JTextField("C:\\HyperSpin");
		pathTxtField.setPreferredSize(new Dimension(155, 28));
		pathTxtField.setEditable(false);

		openRoot.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e2) {

				}

				JFileChooser jfc = new JFileChooser();
				jfc.setDialogTitle("HyperSpin Folder");
				jfc.setCurrentDirectory(new File("C:\\"));
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				FileFilter ff = new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return (true);
						} else {
							return false;
						}
					}

					@Override
					public String getDescription() {
						return "Folder";
					}

				};
				jfc.setFileFilter(ff);

				int rVal = jfc.showOpenDialog(jPan4);

				if (rVal == JFileChooser.APPROVE_OPTION) {
					pathTxtField.setText(jfc.getSelectedFile().getPath());
					PATH = pathTxtField.getText();
					DBPATH = PATH + "\\Databases";
					ODBPATH = PATH + "\\Original Databases";
				} else if (rVal == JFileChooser.CANCEL_OPTION) {
					pathTxtField.setText("C:\\HyperSpin");
					PATH = pathTxtField.getText();
					DBPATH = PATH + "\\Databases";
					ODBPATH = PATH + "\\Original Databases";
				}

				try {
					UIManager.setLookAndFeel(UIManager
							.getCrossPlatformLookAndFeelClassName());
				} catch (Exception e2) {

				}
			}

		});

		GridBagConstraints hag = new GridBagConstraints();

		hag.gridx = 0;
		hag.gridy = 0;
		hag.gridwidth = 2;
		jPan4.add(new JLabel("<html>Select your HyperSpin root folder:<br>"
				+ "Default is \"C:\\HyperSpin\"</html>"), hag);

		hag.gridx = 0;
		hag.gridy = 1;
		hag.gridwidth = 1;
		jPan4.add(openRoot, hag);

		hag.gridx = 1;
		hag.gridy = 1;
		jPan4.add(pathTxtField, hag);
		PATH = pathTxtField.getText();
		
		return PATH;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}
}