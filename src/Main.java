import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;

public class Main extends JFrame implements ActionListener {

	private static final String API_URL = "http://ythogh.com/helpster/scripts/census_Data.php";
	private static final String API_KEY = "18474a817c3a29abd76f0b933b4879720009c551";
	static int CHUNK_SIZE = 40;
	static String API_YEAR = "2010";

	// static int bad = 0;
	static boolean isWritingToFile = false;
	static int ITERATION_INDEX = 0;
	static int NUM_COLS, NUM_ROWS;
	static int NUM_GET_PARAMS = 0;
	static int NUM_FOR_PARAMS = 1;
	static int NUM_IN_PARAMS = 1;
	static int NUM_FOR_STARS = 0;
	static int NUM_IN_STARS = 0;
	static int total_rows;
	static int finished_rows;
	static int chunks = 0;
	static String mGetParams = "P0100001,P0120002,P0120001,P0160001,P0190002,P0200002,P0230002,"
			+ "P0130001,P0120003,P0120004,P0120005,P0120006,P0120007,P0120008,P0120009,P0120010,"
			+ "P0120011,P0120012,P0120013,P0120014,P0120015,P0120016,P0120017,P0120018,P0120019,"
			+ "P0120020,P0120021,P0120022,P0120023,P0120024,P0120025,P0120026,P0120027,P0120028,"
			+ "P0120029,P0120030,P0120031,P0120032,P0120033,P0120034,P0120035,P0120036,P0120037,"
			+ "P0120038,P0120039,P0120040,P0120041,P0120042,P0120043,P0120044,P0120045,P0120046,"
			+ "P0120047,P0120048,P0120049,H0040004,H0040003,H0040002,H0040001";
	// static String qFor = "&for=";
	// static String qIn = "&in=";
	static String mFileName = "";
	static Set<Integer> bad;
	// for adding to get= line ("A,B,C","D,E,F"...)
	static ArrayList<String> getParamsLines;
	// for writing to file headers ("A","B","C","D"...)
	static String[] getParamsHeaders;
	static ArrayList<String> forParamsHeaders;
	static ArrayList<String> inParamsHeaders;
	static ArrayList<String> forStarParamsHeaders;
	static ArrayList<String> inStarParamsHeaders;
	// static String[] mGetArray;
	static String[] mFileNamesArray;
	static long startTime;

	String[][] fileArray;
	ArrayList<Item> zips;
	File file;

	Workbook wb;
	Sheet sh;

	JLabel la, laGet, laFor, laIn;
	JButton openButton, button;
	JTextArea tfGet;
	JTextField tfFor, tfIn;
	JFileChooser fc;
	JRadioButton ch2010, ch2012;

	public static void main(String args[]) {
		new Main();
	}

	public Main() {

		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		JPanel p0 = new JPanel(new GridBagLayout());
		JPanel p1 = new JPanel(new GridBagLayout());
		JPanel p2 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// For logging to the user. Can delete this and all it's references (or
		// hide) for much faster performance.
		ch2010 = new JRadioButton("2010");
		c.gridx = 0;
		c.gridy = 0;
		ch2010.setSelected(true);
		ch2010.addActionListener(this);
		p0.add(ch2010, c);

		ch2012 = new JRadioButton("2012");
		c.gridx = 1;
		c.gridy = 0;
		ch2012.setSelected(false);
		ch2012.addActionListener(this);
		p0.add(ch2012, c);

		// Label/status of operation for user
		la = new JLabel("No file chosen");
		c.gridy = 1;
		c.gridx = 0;
		c.anchor = GridBagConstraints.CENTER;
		p0.add(la, c);

		c = new GridBagConstraints();
		laGet = new JLabel("Get: ");
		c.gridy = 1;
		c.gridx = 0;
		p1.add(laGet, c);

		tfGet = new JTextArea();
		tfGet.setPreferredSize(new Dimension(650, 450));
		c.gridy = 1;
		c.gridx = 1;
		c.weightx = 1;
		c.weighty = 1;
		tfGet.setLineWrap(true);
		// c.fill = GridBagConstraints.BOTH;
		p1.add(tfGet, c);

		c = new GridBagConstraints();
		laFor = new JLabel("For: ");
		c.gridy = 3;
		c.gridx = 0;
		p1.add(laFor, c);

		tfFor = new JTextField();
		c.gridy = 3;
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		p1.add(tfFor, c);

		c = new GridBagConstraints();
		laIn = new JLabel("In: ");
		c.gridy = 4;
		c.gridx = 0;
		p1.add(laIn, c);

		tfIn = new JTextField();
		c.gridy = 4;
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		p1.add(tfIn, c);

		// For choosing a .csv file to use
		fc = new JFileChooser();

		// Button to open a file
		c.fill = GridBagConstraints.HORIZONTAL;
		openButton = new JButton("Choose file");
		c.gridy = 0;
		openButton.addActionListener(this);
		p2.add(openButton, c);

		// Button to begin running program
		button = new JButton("Work some magic!");
		c.gridy = 1;
		button.addActionListener(this);
		button.setEnabled(false);
		p2.add(button, c);

		frame.add(p0, BorderLayout.NORTH);
		frame.add(p1, BorderLayout.CENTER);
		frame.add(p2, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
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
			int returnVal = fc.showOpenDialog(Main.this);
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
			getParamsLines = new ArrayList<String>();
			inParamsHeaders = new ArrayList<String>();
			forParamsHeaders = new ArrayList<String>();
			forStarParamsHeaders = new ArrayList<String>();
			inStarParamsHeaders = new ArrayList<String>();
			// Making sure the program can read the file and reprompting
			// selection if not
			if (!file.getName().endsWith(".csv")) {
				la.setText("Choose a .csv file");
				openButton.doClick();
				return;
			}
			try {
				button.setEnabled(false);
				la.setText("Setting up");
				mFileNamesArray = SetUp.setUpPartitioning(file);
				chunks = 0;
				String[] temp_arr;

				// get parameter handling
				// Splitting api call into chunks with at most 45 get parameters
				mGetParams = tfGet.getText();
				mGetParams.replace("\"", "");
				mGetParams.replace(" ", "");
				System.out.println(mGetParams);
				getParamsHeaders = mGetParams.split(",");
				NUM_GET_PARAMS = getParamsHeaders.length;
				String thisParams;
				if (NUM_GET_PARAMS >= CHUNK_SIZE) {
					int total = NUM_GET_PARAMS;
					while (total > 0) {
						total = total - CHUNK_SIZE;
						thisParams = getParamsHeaders[chunks * CHUNK_SIZE];
						int thisLimit = Math
								.min(NUM_GET_PARAMS - (chunks * CHUNK_SIZE), CHUNK_SIZE);
						for (int i = 1; i < thisLimit; i++) {
							thisParams = thisParams + ","
									+ getParamsHeaders[(chunks * CHUNK_SIZE) + i];
						}
						getParamsLines.add(thisParams);
						chunks++;
					}
				} else {
					if (getParamsHeaders.length > 0) {
						thisParams = getParamsHeaders[0];
						for (int i = 1; i < getParamsHeaders.length; i++) {
							thisParams += "," + getParamsHeaders[i];
						}
						getParamsLines.add(thisParams);
						chunks = 1;
					} else {
						la.setText("Error parsing GET parameters");
						return;
					}

				}
				for (String s : getParamsHeaders) {
					System.out.println(s);
				}

				// for parameter handling
				String temp_for = tfFor.getText();
				temp_arr = temp_for.split(",");
				for (int i = 0; i < temp_arr.length; i++) {
					if (temp_arr[i].charAt(temp_arr[i].length() - 1) == '*') {
						forStarParamsHeaders.add(temp_arr[i]);
					} else {
						forParamsHeaders.add(temp_arr[i]);
					}
				}

				// parameter handling
				String temp_in = tfIn.getText();
				temp_arr = temp_in.split(",");
				for (int i = 0; i < temp_arr.length; i++) {
					if (temp_arr[i].charAt(temp_arr[i].length() - 1) == '*') {
						inStarParamsHeaders.add(temp_arr[i]);
					} else {
						inParamsHeaders.add(temp_arr[i]);
					}
				}
				NUM_FOR_STARS = forStarParamsHeaders.size();
				NUM_IN_STARS = inStarParamsHeaders.size();
				NUM_FOR_PARAMS = forParamsHeaders.size();
				NUM_IN_PARAMS = inParamsHeaders.size();
				NUM_GET_PARAMS = mGetParams.split(",").length;
				NUM_COLS = NUM_GET_PARAMS + NUM_FOR_PARAMS + NUM_IN_PARAMS;
				System.out.println("Columns: " + NUM_COLS);
				System.out.println("Get Parameters: " + NUM_GET_PARAMS);
				System.out.println("For Parameters" + NUM_FOR_PARAMS);
				System.out.println("In Parameters: " + NUM_IN_PARAMS);
				System.out.println("For Stars" + NUM_FOR_STARS);
				System.out.println("In Stars: " + NUM_IN_STARS);
				setupThisQuery();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		} else if (e.getSource() == ch2010) {
			API_YEAR = "2010";
			ch2012.setSelected(false);
		} else if (e.getSource() == ch2012) {
			API_YEAR = "2012";
			ch2010.setSelected(false);
		}
	}

	public void setupThisQuery() {

		try {

			total_rows = 0;
			finished_rows = 0;
			mFileName = mFileNamesArray[ITERATION_INDEX];

			zips = new ArrayList<Item>();
			startTime = Calendar.getInstance().getTime().getTime();
			file = new File(mFileName);
			Scanner io = new Scanner(file);
			String inputLine = "";
			String splitLine[];

			// Reads in every line of .csv file (reading in params)
			while (io.hasNextLine()) {
				inputLine = io.nextLine();
				System.out.println(inputLine);
				// Split line into zip and state tokens
				splitLine = inputLine.split(",");
				if (splitLine.length == (NUM_FOR_PARAMS + NUM_IN_PARAMS)) {
					zips.add(new Item(splitLine));
				}
			}
			// The number of rows is simply the number of zip, state
			// combinations provided in the .csv file
			NUM_ROWS = zips.size();

			io.close();

			// Initialize a new string matrix with these parameters
			fileArray = new String[NUM_ROWS][NUM_COLS];
			// To keep track of progress
			bad = new HashSet<Integer>();

			/*
			 * For each ROW read in from file, get data from
			 * Census API asynchronously. Keeping track of the number of
			 * items we've iterated through also allows the matrix to
			 * remain synchronized throughout the program and avoid
			 * overriding already added data in the matrix (fileArray)
			 */
			for (Item item : zips) {
				new GetDataTask(item.headers, total_rows).execute();
				total_rows++;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Writes the data matrix to a .xlsx file with headers
	 */
	public void writeToFile() {
		// Setting up workbook and sheet to write to
		wb = new XSSFWorkbook();
		sh = wb.createSheet();
		Row row;
		Cell cell;

		// Sets the titles for the columns in the first row
		row = sh.createRow(0);
		for (int h = 0; h < NUM_FOR_PARAMS; h++) {
			cell = row.createCell(h);
			cell.setCellValue(forParamsHeaders.get(h));
		}
		for (int h = 0; h < NUM_IN_PARAMS; h++) {
			cell = row.createCell(h + NUM_FOR_PARAMS);
			cell.setCellValue(inParamsHeaders.get(h));
		}
		for (int h = 0; h < NUM_GET_PARAMS; h++) {
			cell = row.createCell(h + NUM_FOR_PARAMS + NUM_IN_PARAMS);
			cell.setCellValue(getParamsHeaders[h]);
		}

		// Adds data in for the rest of the rows
		for (int r = 0; r < fileArray.length; r++) {
			la.setText("row: " + r + "/" + fileArray.length);

			// If the row was not a failed query, add in all data
			if (!bad.contains(r)) {
				row = sh.createRow(r + 1);

				// Puts the parameter data from excel file first
				for (int c = 0; c < NUM_FOR_PARAMS; c++) {
					cell = row.createCell(c);
					cell.setCellValue(zips.get(r).headers[c]);
				}
				for (int c = 0; c < NUM_IN_PARAMS; c++) {
					cell = row.createCell(c + NUM_FOR_PARAMS);
					cell.setCellValue(zips.get(r).headers[c + NUM_FOR_PARAMS]);
				}

				// Then iterates through the returned data
				for (int c = 0; c < fileArray[r].length; c++) {
					cell = row.createCell(c + NUM_FOR_PARAMS + NUM_IN_PARAMS);
					cell.setCellValue(fileArray[r][c]);
				}
			}
		}

		// Saves the file as "results.xlsx" in current directory and closes all
		// open streams and workbook
		try {
			String s = mFileName.split("[.]")[0];
			File file = new File(String.format("%d-results-%s.xlsx", ITERATION_INDEX, s));
			FileOutputStream fout = new FileOutputStream(file);
			wb.write(fout);
			fout.close();
			wb.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ITERATION_INDEX++;
		isWritingToFile = false;
		System.out.println("Failed: " + bad.size() + " queries");
		double ft = ((Calendar.getInstance().getTime().getTime() - startTime) / 1000.00);
		System.out.println(ft + "s");
		System.out.println((NUM_ROWS / (ft / 60.00)) + " rows/min");

		if (ITERATION_INDEX < mFileNamesArray.length) {
			System.out.println("Next file: " + ITERATION_INDEX + "/" + mFileNamesArray.length);
			setupThisQuery();
		} else {
			la.setText("Choose next file");
		}
		return;
	}

	/**
	 * Called each time an asynchronized task finishes, incrementing
	 * 'finished_rows' and allowing the program to know when all tasks have been
	 * completed. When all tasks have been completed, write the data array to
	 * the file
	 */
	public void OnGetInfoReturn() {
		finished_rows++;
		la.setText("Current progress: " + finished_rows + "/" + total_rows);
		if ((finished_rows > ((total_rows - 1) * .99)) && !isWritingToFile) {
			// log.setText("Writing to file");
			isWritingToFile = true;
			writeToFile();
			return;
		}
	}

	private class GetDataTask extends SwingWorker<Void, Object> {

		String _for, _in;
		int _row, _startCol;
		String[] forParamsData, inParamsData, allParamsData;

		public GetDataTask(String[] forData, String[] inData, int row) {
			this._startCol = 0;
			this.forParamsData = forData;
			this.inParamsData = inData;
			this._row = row;
			this._for = "";
			this._in = "";
		}

		public GetDataTask(String[] allData, int row) {
			this._startCol = 0;
			this.allParamsData = allData;
			this._row = row;
			this._for = "";
			this._in = "";
		}

		/**
		 * Connects to 'API_URL' and posts the data provided in the constructor,
		 * also iterates through results returned as JSON objects
		 */
		@Override
		protected Void doInBackground() throws Exception {
			String query = "", result = "", agent = "Applet", type = "application/x-www-form-urlencoded", inputLine = "";
			HttpURLConnection conn = null;
			URL url;
			OutputStream out;
			BufferedReader in;
			JSONArray arr, data;
			int index1, index2;
			// System.out.println("background: " + _row);

			// Split data from row into for/in parameters
			if (allParamsData != null) {
				int pos = 0;
				// assign for parameters
				forParamsData = new String[NUM_FOR_PARAMS];
				for (; pos < NUM_FOR_PARAMS; pos++) {
					forParamsData[pos] = allParamsData[pos];
				}
				// assign in paramters
				inParamsData = new String[NUM_IN_PARAMS];
				for (; pos < (NUM_IN_PARAMS + NUM_FOR_PARAMS); pos++) {
					inParamsData[pos - NUM_FOR_PARAMS] = allParamsData[pos];
				}
			} else {
			}

			// Adding for parameters
			if (forParamsHeaders.size() > 0) {
				_for = String.format("&for=%s:%s", forParamsHeaders.get(0), forParamsData[0]);
				index1 = 1;
				index2 = 0;
			} else if (forStarParamsHeaders.size() > 0) {
				_for = String.format("&for=%s:*", forStarParamsHeaders.get(0));
				index1 = 0;
				index2 = 1;
			} else {
				index1 = 1;
				index2 = 1;
				bad.add(_row);
				OnGetInfoReturn();
				return null;
			}

			for (; index1 < forParamsData.length; index1++) {
				_for += String
						.format("+%s:%s", forParamsHeaders.get(index1), forParamsData[index1]);
			}
			for (; index2 < forStarParamsHeaders.size(); index2++) {
				_for += String.format("+%s:*", forStarParamsHeaders.get(index2));
			}

			// Adding in parameters
			if (inParamsHeaders.size() > 0) {
				_in = String.format("&in=%s:%s", inParamsHeaders.get(0), inParamsData[0]);
				index1 = 1;
				index2 = 0;
			} else if (inStarParamsHeaders.size() > 0) {
				_in = String.format("&in=%s:*", inStarParamsHeaders.get(0));
				index1 = 0;
				index2 = 1;
			} else {
				index1 = 1;
				index2 = 1;
				OnGetInfoReturn();
				return null;
			}
			for (; index1 < inParamsData.length; index1++) {
				_in += String.format("+%s:%s", inParamsHeaders.get(index1), inParamsData[index1]);
			}
			for (; index2 < inStarParamsHeaders.size(); index2++) {
				_in += String.format("+%s:*", inStarParamsHeaders.get(index2));
			}

			// Adding get parameters
			for (String _get : getParamsLines) {
				result = "";
				query = String.format("get=%s%s%s&year=%s&key=%s", _get, _for, _in, API_YEAR,
						API_KEY);
				try {
					url = new URL(API_URL);
					conn = (HttpURLConnection) url.openConnection();
					conn.setDoInput(true);
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setRequestProperty("User-Agent", agent);
					conn.setRequestProperty("Content-Type", type);
					conn.setRequestProperty("Content-Length", "" + query.length());
					out = conn.getOutputStream();
					out.write(query.getBytes());
					in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					inputLine = "";
					while ((inputLine = in.readLine()) != null) {
						result = result + inputLine;
					}
					arr = new JSONArray(result);
					// arr[0] is the titles
					// arr[1] is the data (getParams..., zip, state)
					data = arr.getJSONArray(1);
					// Fills in data matrix with requested data
					int c = _startCol;
					// if (_row == 0) {
					// System.out.println("before:" + _startCol);
					// }
					for (c = 0; c < ((data.length() - 2)); c++) {
						// if (_row == 0) {
						// System.out.println(c + _startCol);
						// System.out.println(arr);
						// System.out.println(_get);
						// }
						fileArray[_row][c + _startCol] = data.getString(c);
					}
					_startCol += c;
					// if (_row == 0) {
					// System.out.println("after:" + _startCol);
					// }
					conn.disconnect();
					out.close();
					in.close();
				} catch (Exception e) {
					bad.add(_row);
					System.out.println("bad: " + _row);
					e.printStackTrace();
					conn.disconnect();
				}
			}

			OnGetInfoReturn();
			return null;
		}
	}
}
