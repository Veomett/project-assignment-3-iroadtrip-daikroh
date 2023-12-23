import java.io.*;
import java.util.*;

public class Graph {
    private String[] countryAdjList;
    private LinkedList<Edge>[] adjacencyList;
    public List<CapDist> cdDetails = new ArrayList<>(); // holds all the information from
    private List<StateName> snDetails = new ArrayList<>();
    private List<Borders> bDetails = new ArrayList<>();
    private final int maxval = Integer.MAX_VALUE;
    public Graph () { // constructor
        makeBorderData("./borders.txt");
        makeCapDistData("./capdist.csv");
        makeStateNameData("./state_name.tsv");
        countryAdjList = new String[snDetails.size()];
        adjacencyList = new LinkedList[snDetails.size()];
        for (int i = 0; i < snDetails.size(); i++) {
            adjacencyList[i] = new LinkedList<>();
            countryAdjList[i] = snDetails.get(i).countryName;
        }
        for (int i = 0; i < cdDetails.size(); i++) { //creating all the ecdges; world map
            String source = getCountryWithID(cdDetails.get(i).ida);
            String dest = getCountryWithID(cdDetails.get(i).idb);
            if (source != null && dest != null) {
                for (Borders b: bDetails) {
                    if (source.equalsIgnoreCase(b.country) || checkAliases(b.country, source)) {
                        for (int j = 0; j < b.connectingCountry.size(); j++) {
                            if (b.connectingCountry.get(j).equalsIgnoreCase(dest) || checkAliases(b.connectingCountry.get(j), dest)) {
                                addEdge(source, dest, cdDetails.get(i).kmdist);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private void dijkstra(PriorityQueue<Edge> edgeQueue, String[] path, int[] finalDistances, String source) {
        for (int i = 0; i < snDetails.size(); i++) { // clear path and weights
            path[i] = null;
            finalDistances[i] = maxval;
        }

        for (Edge e : adjacencyList[getIndex(source)]) { // add edges from source
            if (!edgeQueue.contains(e)) {
                e.weight += e.distance;
                edgeQueue.add(e);
                e.visited = true;
            }
        }

        while (!edgeQueue.isEmpty()) { // handle all the edges within the queue
            Edge toHandle = edgeQueue.poll();
            if (toHandle.weight < finalDistances[getIndex(toHandle.destination)]) { // if less, update the distance
                finalDistances[getIndex(toHandle.destination)] = toHandle.weight;
                path[getIndex(toHandle.destination)] = toHandle.source;
            }

            for (Edge e : adjacencyList[getIndex(toHandle.destination)]) { // update edge for future loops
                if (!edgeQueue.contains(e) && !e.visited) {
                    e.weight = finalDistances[getIndex(toHandle.destination)] + e.distance;
                    e.visited = true;
                    edgeQueue.add(e);
                }
            }
        }
    }

    public List<String> findShortestPath(String source, String dest) { // find shortest path for 2 countries
        int max = snDetails.size();
        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>();
        String[] path = new String[max];
        int[] finalDistances = new int[max];

        dijkstra(edgeQueue, path, finalDistances, source); // grabs all the potential paths
        for (int i = 0; i < adjacencyList.length; i++) { // reset values for next dijkstra
            for (Edge e : adjacencyList[i]) {
                e.weight = 0;
                e.visited = false;
            }
        }

        String current = dest; //build return value
        Stack<String> pathStack = new Stack<>();
        while (!current.equalsIgnoreCase(source)) {
            int index = getIndex(current);
            if (path[index] != null) { // path exists, create print statement based on path
                String toAdd = path[index] + " -> " + current + ": " + getDistBetweenCountries(path[index], current) + " km";
                pathStack.add(toAdd);
                current = path[index];
            } else { // reset the stack, no path exists
                pathStack = new Stack<>();
                break;
            }
            if (checkAliases(source, current))
                break;
        }
        LinkedList<String> toReturn = new LinkedList<>();
        while (!pathStack.isEmpty()) {
            toReturn.add(pathStack.pop());
        }
        return toReturn;
    }
    public int getDistBetweenCountries(String source, String destination) { // utilizes
        for (int i = 0; i < adjacencyList.length; i++) {
            if (source.equalsIgnoreCase(countryAdjList[i]) || checkAliases(source, countryAdjList[i])) {
                for (Edge e: adjacencyList[i]) {
                    if (destination.equalsIgnoreCase(e.destination) || checkAliases(destination, e.destination)) {
                        return e.distance;
                    }
                }
            }
        }
        return -1;
    }



    private int getIndex(String country) {
        int count = 0;
        for (StateName s: snDetails) {
            if (s.countryName.equalsIgnoreCase(country) || checkAliases(country, s))
                return count;
            count++;
        }
        return -1;
    }


    private String getCountryWithID(String id) {
        for (StateName s: snDetails) {
            if (s.stateID.equalsIgnoreCase(id))
                return s.countryName;
        }
        return null;
    }


    private void addEdge(String v1, String v2, int weight) { // builds and adds edge
        Edge e = new Edge(v1, v2, weight);
        int index = -1;
        int count = 0;
        for (StateName s: snDetails) {
            if (s.countryName.equalsIgnoreCase(v1)) {
                index = count;
                break;
            }
            count++;
        }
        adjacencyList[index].add(e); // returns index from adjacencyList if exists, -1 if it does not
    }


    private boolean checkAliases(String country, StateName s) { // checks existence in state name
        for (int i = 0; i < s.alias.size(); i++) {
            if (s.alias.get(i).equalsIgnoreCase(country))
                return true;
        }
        return false;
    }

    private boolean checkAliases(String country, String source) { // checks existance in state name
        for (StateName s: snDetails) {
            if (s.countryName.equalsIgnoreCase(source)) {
                if (checkAliases(country, s))
                    return true;
            }
        }
        return false;
    }

    public boolean checkExistence(String country) { // checks if country exists from statename file
        for (StateName s: snDetails) {
            if (country.equalsIgnoreCase(s.countryName) || checkAliases(country, s))
                return true;
        }
        return false;
    }

    private ArrayList<String> getBorderingCountries (String data) { // helper function to create array of bordering country names
        ArrayList<String> borderList = new ArrayList<>();
        String[] dataArr = data.split(";");
        for (int i = 0; i < dataArr.length; i++) {
            String country = (dataArr[i].split("[0-9]")[0]).replaceAll("\\(.*?\\)","").strip();
            borderList.add(country);
        }
        return borderList;
    }

    private void makeBorderData(String file) { // parses data from border
        File f = new File(file);
        if (!f.exists()) {
            System.out.println("File not found");
            System.exit(-1);
        }

        try {
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String data = bf.readLine();
            while (data != null) {
                String[] dataArr = data.split("=");
                Borders b = new Borders();
                b.country = dataArr[0].replaceAll("\\(.*?\\)","").strip();
                if (!dataArr[1].isBlank()) {
                    b.connectingCountry = getBorderingCountries(dataArr[1]);
                }
                bDetails.add(b);
                data = bf.readLine();
            }
            bf.close();
        } catch (Exception e) {
            System.out.println("Error scanning file: " + e);
            System.exit(-1);
        }
    }

    private void makeCapDistData(String fileName) { // parses data from capdist
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist");
            System.exit(-1);
        }
        try {
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String data = bf.readLine(); //skip first line
            while (true) {
                data = bf.readLine(); //read second line
                if (data == null) {
                    break;
                }
                String[] dataArr = data.split(","); //split lines from csv file using ","
                CapDist c = new CapDist();
                c.numa = Integer.parseInt(dataArr[0]);
                c.ida = dataArr[1];
                c.numb = Integer.parseInt(dataArr[2]);
                c.idb = dataArr[3];
                c.kmdist = Integer.parseInt(dataArr[4]);
                cdDetails.add(c);
            }
            bf.close();
        } catch (Exception e) {
            System.out.println("Error scanning file: " + e);
            System.exit(-1);
        }
    }

    private void addExtraAliases(StateName s) { // helper function to add different aliases for same countries
        s.alias.add(s.stateID);
        switch (s.stateID) {
            case "DRC":
                s.alias.add("Congo, Democratic Republic of");
                s.alias.add("Congo, Democratic Republic of the");
                s.alias.add("Democratic Republic of the Congo");
                break;
            case "PRK":
                s.alias.add("North Korea");
                s.alias.add("Korea, North");
                break;
            case "ROK":
                s.alias.add("South Korea");
                s.alias.add("Korea, South");
                break;
            case "DRV":
                s.alias.add("Vietnam");
                s.alias.add("Annam");
                s.alias.add("Cochin China");
                s.alias.add("Tonkin");
                s.alias.add("VNM");
                s.alias.add("RVN");
                break;
            case "TAZ":
                s.alias.add("Tanzania");
                s.alias.add("Tanganyika");
                s.alias.add("Zanzibar");
                s.alias.add("ZAN");
                s.alias.add("TAN");
                break;
            case "USA":
                s.alias.add("United States");
                s.alias.add("US");
                break;
            case "BHM":
                s.alias.add("Bahamas, The");
                s.alias.add("The Bahamas");
                break;
            case "MOR":
                s.alias.add("Morocco (Ceuta)");
                break;
            case "SPN":
                s.alias.add("Spain (Ceuta)");
                break;
            case "GFR":
                s.alias.add("Germany");
                break;
        }
    }

    private void makeStateNameData(String file) {
        File f = new File(file);
        if (!f.exists()) {
            System.out.println("File does not exist");
            System.exit(-1);
        }
        try {
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String data = bf.readLine(); //read first line
            while (true) {
                data = bf.readLine(); //read second line
                if (data == null) {
                    break;
                }
                String[] dataArr = data.split("\t"); //split lines from tsv file using the tab character
                if (dataArr[4].equalsIgnoreCase("2020-12-31")) {
                    StateName s = new StateName(); //initialize object
                    s.stateNum = Integer.parseInt(dataArr[0]);
                    s.stateID = dataArr[1];
                    if (dataArr[2].contains("(")) {
                        String[] split = dataArr[2].split("\\(");
                        s.countryName = split[0].trim();
                        s.alias.add(split[1].replace(")", "").trim());
                    } else {
                        s.countryName = dataArr[2];
                    }
                    addExtraAliases(s);
                    snDetails.add(s);
                }


            }
            bf.close();
        } catch (IOException e) {
            System.out.println("Error reading State Name file: " + e);
            System.exit(-1);
        }
    }
}




