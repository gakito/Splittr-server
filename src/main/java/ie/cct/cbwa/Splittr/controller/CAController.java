package ie.cct.cbwa.Splittr.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

//	@ModelAttribute
//	public void setResponseHeader(HttpServletResponse response) {
//		response.setHeader("Access-Control-Allow-Origin", "*");
//	}

	@RequestMapping(value = "test")
	public String test() {
		return "SUCCESS";
	}

	
	@CrossOrigin(origins = "http://localhost:19006") //this allows a mapping from this specific origin.
	@GetMapping("/login")
	public String login(@RequestParam(name = "username", required = true) String username,
			@RequestParam(name = "password", required = true) String password) {

		// checking if user and password are valid
		if (list.getUsers().get(username.toLowerCase()) != null
				&& list.getUsers().get(username.toLowerCase()).contentEquals(password)) {
			this.username = username;
			this.token = JWTIssuer.createJWT(username, "splittr", username, 86400000);
			return token;
		} else {
			sendViaException();
			return "Invalid username or password";
		}
	}

	@CrossOrigin(origins = "http://localhost:19006")
	@PostMapping("/{trip}/expense") // Authorization: Bearer <token>
	public Map<String, ArrayList<Expense>> addExpense(@PathVariable("trip") String trip,
			@RequestHeader(name = "Authorization", required = true) String token, // how to get insertion from users
			@RequestBody(required = true) Expense expense) {

		// TODO return 401 instead of 500.
		Claims claims = JWTIssuer.decodeJWT(token.split(" ")[1]);
		String subClaim = claims.get("sub", String.class);
		if (!username.contentEquals(subClaim)) {
			sendViaException();
		}

		if (trips.get(trip) == null) {
			trips.put(trip, new ArrayList<Expense>());
			checkTrip.put(trip, true);
		}

		System.out.println("Trip status: " + checkTrip.get(trip));
		if (checkTrip.get(trip)==true) {
			trips.get(trip).add(expense);
			return trips;
		} else {
			return null;
		}

	}

	@CrossOrigin(origins = "http://localhost:19006")
	@GetMapping("/{trip}")
	public ArrayList<Expense> getTrip(@PathVariable(name = "trip", required = true) String trip) {
		return trips.get(trip);
	}

	@CrossOrigin(origins = "http://localhost:19006")
	@PostMapping("/{trip}/close")
	public boolean closeTrip(@PathVariable("trip") String trip) {

		if (checkTrip.containsKey(trip)) {
			checkTrip.put(trip, false);
			return true;
		} else {
			return false;
		}
	}

	@CrossOrigin(origins = "http://localhost:19006")
	@GetMapping("/{trip}/summary")
	public Map<String, Integer> getSummary(@PathVariable("trip") String trip) {

		Map<String, Integer> splitCheck = new HashMap<String, Integer>();

		for (int i = 0; i < trips.get(trip).size(); i++) {

			if (!splitCheck.containsKey(trips.get(trip).get(i).getName())) {
				splitCheck.put(trips.get(trip).get(i).getName(), 0);
			}

			String currentName = trips.get(trip).get(i).getName();
			int temp = splitCheck.get(trips.get(trip).get(i).getName());
			int valueToAdd = trips.get(trip).get(i).getAmount();
			int updatedAmount = temp + valueToAdd;
	
			splitCheck.put(currentName, updatedAmount);
			
		}
		
		System.out.println(splitCheck);
		
		return splitCheck;
	}
}
