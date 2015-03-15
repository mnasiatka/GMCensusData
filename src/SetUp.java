import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SetUp extends JFrame implements ActionListener {

	static int NEW_ROW_LIMIT = 1000;

	static int ITERATION_INDEX = 0;
	static int NUM_COLS, NUM_ROWS, NUM_FILES;
	static int total_rows;
	static int finished_rows;

	public static String[] mFileNamesArray;
	static String[][] fileArray;
	static ArrayList<Item> zips;
	static File file;
	static JLabel la;

	static Workbook wb;
	static Sheet sh;

	JButton openButton, button;
	static String mFileName = "";
	JFileChooser fc;

	public static void main(String args[]) {
		new SetUp();
	}

	public SetUp() {

		JFrame frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		JPanel p1 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// For logging to the user. Can delete this and all it's references (or
		// hide) for much faster performance.
		// log = new JTextArea();
		// log.setText("");
		// log.setEditable(false);

		// Label/status of operation for user
		la = new JLabel("No file chosen");
		c.gridx = 0;
		c.gridy = 0;
		p1.add(la, c);

		// For choosing a .csv file to use
		fc = new JFileChooser();

		// Button to open a file
		openButton = new JButton("Choose file");
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		openButton.addActionListener(this);
		p1.add(openButton, c);

		// Button to begin running program
		button = new JButton("Work some magic!");
		c.gridx = 0;
		c.gridy = 2;
		button.addActionListener(this);
		button.setEnabled(false);
		p1.add(button, c);

		// Adds panel to frame
		// c.gridx = 0;
		// c.gridy = 3;
		// c.weighty = 1;
		// c.ipady = 50;
		// p1.add(log, c);
		frame.add(p1);
		frame.pack();
		frame.setVisible(true);
		// log.setVisible(false);
	}

	/**
	 * Handles clicks on buttons: chooses a file or starts program
	 *
	 * @param e
	 *            ActionEvent used to determine which button was clicked
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Open the dialog to choose a .csv file
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(SetUp.this);
			// Hit 'OK' (chooses file)
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// Stores file and file name for later use
				file = fc.getSelectedFile();
				mFileName = file.getName();
				la.setText(mFileName);
				// If a non .csv file was chosen, don't allow it
				if (mFileName.trim().length() > 0) {
					if (!file.getName().contains(".csv")) {
						la.setText("Choose a .csv file");
						button.setEnabled(false);
						openButton.doClick();
						return;
					}
					button.setEnabled(true);
				} else {
					button.setEnabled(false);
				}
			}
			// Start the program with the given data from the .csv file
		} else if (e.getSource() == button) {
			zips = new ArrayList<Item>();
			// The grunt work of the program
			try {
				// Hide 'Run' button to avoid unforeseen errors
				// Show data log for user
				button.setEnabled(false);
				// Instantiate scanner for file io
				Scanner io = new Scanner(file);
				mFileName = file.getName();
				mFileName = mFileName.split("[.]")[0];
				String inputLine = "";
				String splitLine[];
				// Reads in every line of .csv file
				while (io.hasNextLine()) {
					inputLine = io.nextLine();
					// Split line into zip and state tokens
					splitLine = inputLine.split(",");
					if (splitLine.length > 0) {
						zips.add(new Item(splitLine));
					}
					// zips.add(new Item(splitLine[0], splitLine[1]));
					// la.setText("Reading: " + zips.size());
				}
				// The number of columns is 2 (zip, state) + the number of
				// parameters provided for 'get:'
				NUM_COLS = zips.get(0).headers.length;
				// The number of rows is simply the number of zip, state
				// combinations provided in the .csv file
				NUM_ROWS = zips.size();
				// Number of resulting files to store data in
				NUM_FILES = (int) Math.ceil((double) zips.size() / 1000);
				// Initialize a new string matrix with these parameters
				io.close();
				// To keep track of progress
				total_rows = 0;
				finished_rows = 0;
				writeToFile("");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public static String[] setUpPartitioning(File mFile) {
		zips = new ArrayList<Item>();
		file = mFile;
		mFileName = file.getName();
		mFileName = mFileName.split("[.]")[0];
		// The grunt work of the program
		try {
			// Instantiate scanner for file io
			Scanner io = new Scanner(mFile);
			String inputLine = "";
			String splitLine[];
			// Reads in every line of .csv file
			while (io.hasNextLine()) {
				inputLine = io.nextLine();
				// Split line into zip and state tokens
				splitLine = inputLine.split(",");
				zips.add(new Item(splitLine));
				// zips.add(new Item(splitLine[0], splitLine[1]));
			}
			// The number of columns is 2 (zip, state) + the number of
			// parameters provided for 'get:'
			NUM_COLS = zips.get(0).headers.length;
			// The number of rows is simply the number of zip, state
			// combinations provided in the .csv file
			NUM_ROWS = zips.size();
			// Number of resulting files to store data in
			NUM_FILES = (int) Math.ceil((double) zips.size() / 1000);
			io.close();
			return writeToFile("temp-");
			// Initialize a new string matrix with these parameters

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Writes the data matrix to a .xlsx file with headers
	 *
	 * @throws IOException
	 */
	public static String[] writeToFile(String prefix) throws IOException {
		// Setting up workbook and sheet to write to
		Row row;
		Cell cell;
		int remaining = NUM_ROWS;
		// Adds the data retrieved along with the zip+state code from the array
		// into the excel file
		FileWriter writer;
		mFileNamesArray = new String[NUM_FILES];
		for (ITERATION_INDEX = 0; ITERATION_INDEX < NUM_FILES; ITERATION_INDEX++) {
			wb = new XSSFWorkbook();
			sh = wb.createSheet();
			// la.setText("Writing: " + ITERATION_INDEX + "/" + (NUM_FILES -
			// 1));
			int limit = NEW_ROW_LIMIT;
			if (ITERATION_INDEX == (NUM_FILES - 1)) {
				limit = remaining;
			}
			remaining = remaining - limit;
			mFileNamesArray[ITERATION_INDEX] = String.format("%s%s-%s.csv", prefix, mFileName,
					ITERATION_INDEX);
			writer = new FileWriter(
					String.format("%s%s-%s.csv", prefix, mFileName, ITERATION_INDEX));
			// For each row in the file
			for (int r = 0; r < limit; r++) {
				// For each header in the row
				if (zips.get(r + (ITERATION_INDEX * 1000)).headers.length >= 1) {
					writer.append(zips.get(r + (ITERATION_INDEX * 1000)).headers[0]);
				}
				for (int h = 1; h < zips.get(r + (ITERATION_INDEX * 1000)).headers.length; h++) {
					writer.append(",");
					writer.append(zips.get(r + (ITERATION_INDEX * 1000)).headers[h]);
				}
				writer.append("\n");
			}
			writer.flush();
			writer.close();
			// Saves the file as "results.xlsx" in current directory and closes
			// all open streams and workbooks
			// try {
			//
			// // File file = new File(String.format("state-zip-%s.csv",
			// // ITERATION_INDEX));
			// // FileOutputStream fout = new FileOutputStream(file);
			// // //wb.write(fout);
			// // fout.close();
			// // //wb.close();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
		}
		System.out.println(mFileNamesArray.length + " files created.");
		return mFileNamesArray;
	}
}
