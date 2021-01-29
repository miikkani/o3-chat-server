import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

/**
 * MyMessages is a simple program that saves user inputted messages to a text file and
 * allows reading previous messages. 
 * <p>
 * User must give a username before writing message. Username must be a sequence of non-blank characters.
 * Previous messages are printed in format: (HH:mm:ss)<$username>$message
 *
 * @author Miikka Niemi
 * @version %I%
 */
public class MyMessages {
    /**
     * Shows menu and asks user input for desired action.
     */
    public static void main(String[] args) throws Exception {


	int choice = -1;

	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	
	while(choice != 0) {
	    printMenu();
	    try {
	    choice = Integer.parseInt(stdin.readLine());
	    } catch (NumberFormatException nme) {choice = -1;}

	    switch(choice) {
		case 1:
			saveMessage(stdin);
			break;
		case 2:
			printMessages();
			break;
		case 0:
			break; 
		default:
			System.out.print("tuntematon valinta...");
			break;
	    }
	}
    }


    /**
     * Prints menu to console window.
     * 
     */
    static void printMenu() {
	System.out.print(
			"\n-------------\n"+
			"1. Kirjoita viesti\n"+
			"2. Lue viestit\n"+
			"0. Lopeta\n"+
			"\n"+
			"valinta .> "
			);
    }

    /**
     * Asks username and then saves formatted message to data.txt.
     *
     * @param stdin keyboard input from the command line
     */
    static void saveMessage(BufferedReader stdin) throws Exception {
	File data = new File("data.txt");



	String message = null;
	String username = "";
	Calendar time = Calendar.getInstance();
	SimpleDateFormat fm = new SimpleDateFormat("(HH:mm:ss)");


	do {
	    System.out.print( "nimimerkki .> ");	
	    username = stdin.readLine();
	} while(username.contains(" ") || username.isBlank());



	PrintWriter out = new PrintWriter(
			new BufferedWriter(
			new FileWriter(data,true)));


	System.out.print( "\nAnna viesti: ");	

	message = stdin.readLine();

	out.print(fm.format(new Date(time.getTimeInMillis())));
	out.println("<" + username + "> " + message);
	out.close();
	System.out.print("\nViesti tallennettu!");	
	Thread.sleep(300);
    }


    /**
     * Prints all messages from the data.txt file.
     */
    static void printMessages() throws Exception {
	File data = new File("data.txt");
	if(!data.exists()){
		System.out.println("\nEi tallennettuja viestejä.");
		Thread.sleep(300);
		return;
	}



	String message = null;


	BufferedReader in = new BufferedReader(
			new FileReader(data));

	System.out.print("\nViestit\n" +
			"============\n\n");

	while((message = in.readLine()) != null) {
	    System.out.println(message);
	}

	in.close();

	System.out.print("\n============");

    }
}
