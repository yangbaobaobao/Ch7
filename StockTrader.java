import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class StockTrader {

    public static void main(String[] args) throws FileNotFoundException {
        Scanner in = new Scanner(new File("rising.txt"));
        String symbol = in.next();
        int n = in.nextInt();

        int[] years = new int[n];
        int[] months = new int[n];
        double[] prices = new double[n];

		loadData(in, years, months, prices);
        printData(years, months, prices);
        double result = simulate(years, months, prices);
		System.out.printf("Final value: $%,.2f%n", result);
    }
    
    public static void printData(int[] years, int[] months, double[] prices) {
		for (int i = 0; i < years.length; i++) {
			System.out.println(years[i] + " " + monthName(months[i]) + ": $" + prices[i]);
		}
	}
	
	public static int indexOfLowest(double[] prices) {
		double lowest = Double.POSITIVE_INFINITY;
		int idx = 0;
		for (int i = 0; i < prices.length; i++) {
			if (prices[i] < lowest) {
				lowest = prices[i];
				idx = i;
			}
		}
		return idx;
	}
	
	public static int indexOfHighest(double[] prices) {
		double highest = -1 * Double.POSITIVE_INFINITY;
		int idx = 0;
		for (int i = 0; i < prices.length; i++) {
			if (prices[i] > highest) {
				highest = prices[i];
				idx = i;
			}
		}
		return idx;
	}
	
	public static int indexOfBiggestDrop(double[] prices) {
		double highest = -1 * Double.POSITIVE_INFINITY;
		int idx = 0;
		for (int i = 1; i < prices.length; i++) {
			double drop = prices[i-1] - prices[i];
			if (drop > highest) {
				highest = drop;
				idx = i;
			}
		}
		return idx;
	}
    
    public static void loadData(Scanner in, int[] years, int[] months, double[] prices) {
		for (int i = 0; i < years.length; i++) {
			years[i] = in.nextInt();
			months[i] = in.nextInt();
			prices[i] = in.nextDouble();
		}
    }
    
    public static String monthName(int month) {
		String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		return months[month-1];
    }
    
    public static String decide(double[] history, boolean holding, double boughtAt) {
		//return buyAndHold(history, holding, boughtAt);   // change this line to switch strategies
		return dipBuyer(history, holding, boughtAt);
	}
	
	public static String dipBuyer(double[] history, boolean holding, double boughtAt) {
		int dipRow = 0;
		boolean bought = false;
		double priceBought = 0;
		for (int i = 1; i < history.length; i++) {
			if (history[i-1] > history[i]) {
				dipRow++;
				if (dipRow > 1) {
					if (i == history.length-1)
						return "BUY";
					else {
						bought = true;
						priceBought = history[i];
					}
				}
			}
			else {
				dipRow = 0;
				if (bought && (history[i] / priceBought > 1.1)) {
					bought = false;
					return "SELL";
				}
			}
		}
		return "HOLD";
	}
	
	public static String movingAverage(double[] history, boolean holding, double boughtAt) {
		boolean bought = false;
		if (history.length < 12) return "HOLD";
		else {
			int avg = 0;
			for (int i = 0; i < 12; i++) {
				avg += history[i];
			}
			avg /= 12;
		}
		
	}
	
	public static String buyAndHold(double[] history, boolean holding, double boughtAt) {
		return "BUY";
	}
	
	public static final double FEE = 10.0;
	public static final double START_CASH = 10000.0;
	public static double simulate(int[] years, int[] months, double[] prices) {
		double cash = START_CASH;
		int shares = 0;
		double boughtAt = 0.0;

		for (int i = 0; i < prices.length; i++) {
			double[] history = Arrays.copyOf(prices, i + 1);
			String action = decide(history, shares > 0, boughtAt);

			if (action.equals("BUY") && shares == 0) {
				int canBuy = (int) ((cash - FEE) / prices[i]);
				if (canBuy > 0) {
					shares = canBuy;
					cash = cash - shares * prices[i] - FEE;
					boughtAt = prices[i];
					System.out.printf("%s %d: BUY %d shares at $%.2f%n",
							monthName(months[i]), years[i], shares, prices[i]);
				}
			} else if (action.equals("SELL") && shares > 0) {
				cash = cash + shares * prices[i] - FEE;
				System.out.printf("%s %d: SELL %d shares at $%.2f%n",
						monthName(months[i]), years[i], shares, prices[i]);
				shares = 0;
				boughtAt = 0.0;
			}
		}
		return cash + shares * prices[prices.length - 1];
	}
}
