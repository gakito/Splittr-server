package ie.cct.cbwa.Splittr.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ie.cct.cbwa.Splittr.userList;
import ie.cct.cbwa.Splittr.model.Expense;
import ie.cct.cbwa.Splittr.util.JWTIssuer;
import io.jsonwebtoken.Claims;

@RestController
public class CAController {
	
	/**
	 * @param userList - a list containing the users hard coded
	 * @param trips - a hashmap to store a trip as a key and all its expenses as values
	 * @param active - boolean to allow or not an expense to be added on a trip
	 * @param checkTrip - a hashmap to give each trip an active boolean
	 * @param username - name inserted by the user
	 * @param token - auth string to make a login valid 
	 */

	private userList list = new userList();
	private Map<String, ArrayList<Expense>> trips;
	public boolean active;
	private Map<String, Boolean> checkTrip;
	public String username;
	public String token;

	/**
	 * specifics to detail method
	 * @param labelMax - label for the highest expense
	 * @param labelMin - label for the lowest expense
	 * @param userMax - user that made the highest expense
	 * @param userMin - user that made the lowest expense
	 * @param maxString - the highest expense as string
	 * @param minString - the lowest expense as string 
	*/
	
	
	String labelMax;
	String labelMin;
	String userMax;
	String userMin;
	String maxString;
	String minString;

	public CAController() {
		trips = new HashMap<>();
		checkTrip = new HashMap<>();
	}

	// Error 401 for auth error
	// from https://www.baeldung.com/spring-mvc-controller-custom-http-status-code
	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity sendViaException() {
		throw new UnauthorizedException();
	}
	
	//method to log in. 
	@CrossOrigin(origins = "http://localhost:19006") //this allows a mapping from this specific origin.
	@GetMapping("/login")
	public String login(@RequestParam(name = "username", required = true) String username,
			@RequestParam(name = "password", required = true) String password) {

		//checking if user and password are valid. 
		//compares the username and password hard coded and returns a token		
		if (list.getUsers().get(username.toLowerCase()) != null
				&& list.getUsers().get(username.toLowerCase()).contentEquals(password)) {
			this.username = username;
			this.token = JWTIssuer.createJWT(username, "splittr", username, 86400000);
			return token;
		} else {
			sendViaException();  //returns a 401 error if not valid login
			return "Invalid username or password";
		}
	}

	@CrossOrigin(origins = "http://localhost:19006")
	@PostMapping("/{trip}/expense") // Authorization: Bearer <token>
	public Map<String, ArrayList<Expense>> addExpense(@PathVariable("trip") String trip,
			@RequestHeader(name = "Authorization", required = true) String token, // how to get insertion from users
			@RequestBody(required = true) Expense expense) {

		//to add expense checks if the user has a valid token. returns a 401 error if doesn't
		Claims claims = JWTIssuer.decodeJWT(token.split(" ")[1]);
		String subClaim = claims.get("sub", String.class);
		if (!username.contentEquals(subClaim)) {
			sendViaException();
		}

		//creates an arraylist for the trip if it doesn't exist already
		if (trips.get(trip) == null) {
			trips.put(trip, new ArrayList<Expense>());
			checkTrip.put(trip, true);
		}

		//checking if the trip is active. if it is, an expense can be added. if not, returns null
		if (checkTrip.get(trip)==true) {
			trips.get(trip).add(expense);
			return trips;
		} else {
			return null;
		}

	}

	//method to list all expenses from a trip
	@CrossOrigin(origins = "http://localhost:19006")
	@GetMapping("/{trip}")
	public ArrayList<Expense> getTrip(@PathVariable(name = "trip", required = true) String trip) {
		return trips.get(trip);
	}

	//method to close a trip so more expenses can not be added.  
	@CrossOrigin(origins = "http://localhost:19006")
	@PostMapping("/{trip}/close")
	public boolean closeTrip(@PathVariable("trip") String trip) {

		//this if checks if the trip name exists. if it does, change "active" value to close the trip. 
		//if not, return false for closing a trip. 
		if (checkTrip.containsKey(trip)) {
			checkTrip.put(trip, false);
			return true;
		} else {
			return false;
		}
	}

	//gets summary from the trip at any time. 
	//returns a map that shows how much each user spent.
	@CrossOrigin(origins = "http://localhost:19006")
	@GetMapping("/{trip}/summary")
	public Map<String, Integer> getSummary(@PathVariable("trip") String trip) {

		//map relating users (keys) to their spending(values)
		Map<String, Integer> splitCheck = new HashMap<String, Integer>();

		//a linear search for each element of the expense list
		for (int i = 0; i < trips.get(trip).size(); i++) {

			//this if checks if the splitCheck map contain a key that will receive the value at this loop
			if (!splitCheck.containsKey(trips.get(trip).get(i).getName())) {
				splitCheck.put(trips.get(trip).get(i).getName(), 0);
			}

			//temporary string to store username for the current loop
			String currentName = trips.get(trip).get(i).getName();
			
			//temporary integer to store the current expense for this user
			int temp = splitCheck.get(trips.get(trip).get(i).getName());
			
			//gets the amount that should be added to that user's expense
			int valueToAdd = trips.get(trip).get(i).getAmount();
			
			//update the expense for this user
			int updatedAmount = temp + valueToAdd;
			
			//update the value for this key on the map
			splitCheck.put(currentName, updatedAmount);
			
		}
		
		return splitCheck;
	}
	
	//returns a map with information about the highest and lowest expenses
	@CrossOrigin(origins = "http://localhost:19006")
	@GetMapping("/{trip}/details")
	public Map<String, ArrayList<String>> getDetails(@PathVariable("trip") String trip){
		
		//setting variables to search for highest and lowest expense
		int max = 0;
		int min = 0;
		
		//map that will have two keys (max and min) and their values will be an arrayList of expenses 
		Map<String, ArrayList<String>> tripDetails = new HashMap<String, ArrayList<String>>();
		
		//linear search through all expenses
		for (int i = 0; i < trips.get(trip).size(); i++) {
			
			if (trips.get(trip).get(i).getAmount()>max) {
				System.out.println(trips.get(trip).get(i).getAmount());
				max=trips.get(trip).get(i).getAmount();
				maxString=trips.get(trip).get(i).getAmount().toString();
				labelMax=trips.get(trip).get(i).getLabel();
				userMax=trips.get(trip).get(i).getName();
			}
			//an else if is used because doesn't make sense to check if a maximum is a minimum
			//however, if the trip has only one expense, there won't be a minimum
			else if(trips.get(trip).get(i).getAmount()<min||min==0) {
				min=trips.get(trip).get(i).getAmount();
				minString=trips.get(trip).get(i).getAmount().toString();
				labelMin=trips.get(trip).get(i).getLabel();
				userMin=trips.get(trip).get(i).getName();
			}
		}
		
		//setting maxList values
		ArrayList<String> maxList = new ArrayList<>();
		maxList.add(userMax);
		maxList.add(maxString);
		maxList.add(labelMax);
		
		//setting minList values
		ArrayList<String> minList = new ArrayList<>();
		minList.add(userMin);
		minList.add(minString);
		minList.add(labelMin);

		//setting tripDetails map values
		tripDetails.put("max", maxList);
		tripDetails.put("min", minList);
		
		return tripDetails;
		
	}
	
	
	
}
