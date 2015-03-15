import java.util.Scanner;

public class PraseGetInput {

	public static void main(String args[]) {
		Scanner kb = new Scanner(System.in);
		String inputLine = "";
		System.out.println("--Input--");
		inputLine = kb.nextLine();
		String arr[] = inputLine.split("\", \"");
		String result = "";
		result = arr[0];
		for (int i = 1; i < arr.length; i++) {
			result = result + "," + arr[i];
		}
		int chunks = 1;
		int total = arr.length;
		System.out.println(result);
		System.out.println("Size: " + arr.length);
		if (arr.length >= 50) {
			while (total >= 50) {
				total = total - 50;
				chunks++;
			}
			System.out.println("Should probably break it up into " + chunks + " chunks.");
		}
	}

	public static String Parse(String s) {
		String arr[] = s.split("\", \"");
		String result = "";
		result = arr[0];
		for (int i = 1; i < arr.length; i++) {
			result = result + "," + arr[i];
		}
		int chunks = 1;
		int total = arr.length;
		System.out.println(result);
		System.out.println("Size: " + arr.length);
		if (arr.length >= 50) {
			while (total >= 50) {
				total = total - 50;
				chunks++;
			}
			System.out.println("Should probably break it up into " + chunks + " chunks.");
		}
		return result;
	}
}
