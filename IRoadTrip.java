import java.util.List;
import java.util.Scanner;

public class IRoadTrip {

    Graph graph; // bulk of logic within Graph, IRoadTrip mostly just main() functions

    public IRoadTrip () {
            graph = new Graph();
    }

    public int getDistance (String country1, String country2) {
        return graph.getDistBetweenCountries(country1, country2);
    }

    public List<String> findPath (String country1, String country2) {
        return graph.findShortestPath(country1, country2);
    }


    public void acceptUserInput() {
        Scanner in = new Scanner(System.in);
        String country1;
        String country2;
        while (true) {
            while (true) {
                System.out.print("Enter the name of the first country (type EXIT to quit): ");
                country1 = in.nextLine();
                if (checkValidity(country1))
                    break;
            }
            while (true) {
                System.out.print("Enter the name of the second country (type EXIT to quit): ");
                country2 = in.nextLine();
                if (checkValidity(country2))
                    break;
            }
            break;
        }
        List<String> path = findPath(country1, country2);
        if (!path.isEmpty()) {
            System.out.println("Route from " + country1 + " to " + country2 + ":");
            for (String p : path) {
                System.out.println(p);
            }
            acceptUserInput();
        } else {
            System.out.println("Path does not exist between these two countries.");
            acceptUserInput();
        }
    }

    private boolean checkValidity (String country) {
        if (country.equalsIgnoreCase("EXIT")) {
            System.out.println("Exiting...");
            System.exit(0);
        }
        if (!graph.checkExistence(country)) {
            System.out.println("Invalid country name. Please enter a valid country name.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip();
        a3.acceptUserInput();
    }

}

