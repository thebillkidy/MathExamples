import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;


public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("./STDIN.txt"));
		
		int numberOfTests = scanner.nextInt();
		scanner.nextLine();
		
		processTests(scanner, numberOfTests);
	}

	private static void processTests(Scanner scanner, int numberOfTests) {
		for (int i = 0; i < numberOfTests; i++) {
			int aantalIngredienten = scanner.nextInt();
			scanner.nextLine();
			
			Map<String, Integer> ingredienten = new HashMap<String, Integer>();
			Map<String, HashMap<String, Integer>> recepten = new HashMap<String, HashMap<String, Integer>>();
			
			// Read available ingredients
			for (int j = 0; j < aantalIngredienten; j++) {
				String line = scanner.nextLine();
				String[] lineResult;
				int amount;
				String name;
				
				if (line.contains(" g ")) {
					lineResult = line.split(" g ");
					amount = Integer.parseInt(lineResult[0]);
					name = lineResult[1];
				} else if (line.contains(" ml ")) {
					lineResult = line.split(" ml ");
					amount = Integer.parseInt(lineResult[0]);
					name = lineResult[1];
				} else {
					lineResult = line.split(" ");
					amount = Integer.parseInt(lineResult[0]);
					name = lineResult[1];
				}
				
				ingredienten.put(name, amount);
			}
			
			int amountOfRecipes = scanner.nextInt();
			scanner.nextLine();
			
			// Read amount of recipes
			for (int j = 0; j < amountOfRecipes; j++) {
				String nameRecipe = scanner.nextLine();
				int amountOfIngredients = scanner.nextInt();
				scanner.nextLine();
				
				HashMap<String, Integer> ingredientenBijRecept = new HashMap<String, Integer>();
				
				for (int k = 0; k < aantalIngredienten; k++) {
					String line = scanner.nextLine();
					String[] lineResult;
					int amount;
					String name;
					
					if (line.contains(" g ")) {
						lineResult = line.split(" g ");
						amount = Integer.parseInt(lineResult[0]);
						name = lineResult[1];
					} else if (line.contains(" ml ")) {
						lineResult = line.split(" ml ");
						amount = Integer.parseInt(lineResult[0]);
						name = lineResult[1];
					} else {
						lineResult = line.split(" ");
						amount = Integer.parseInt(lineResult[0]);
						name = lineResult[1];
					}
					
					ingredientenBijRecept.put(name, amount);
				}
				
				recepten.put(nameRecipe, ingredientenBijRecept);
			}
			
			_processTestCase(ingredienten, recepten);
		}
	}
	
	private static void _processTestCase(Map<String, Integer> ingredienten, Map<String, HashMap<String, Integer>> recepten) {
		//Iterator it = recepten.
		Iterator it = recepten.entrySet().iterator();
		
		while (it.hasNext()) {
			Map<String, HashMap<String, Integer>> recept = (Map<String, HashMap<String, Integer>>)it.next();
			System.out.println(recept);
		}
	}
}

