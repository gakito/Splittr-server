package ie.cct.cbwa.Splittr;

import java.util.HashMap;

public class userList {

	HashMap<String, String> users;

	public userList() {
		this.users = new HashMap<>();
		users.put("amilcar", "yoda");
		users.put("david", "matrix");
		users.put("greg", "password");
	}

	
	public HashMap<String, String> getUsers() {
		return users;
	}
	
	public void setUsers(String key, String value) {
		users.put(key, value);
	}
	
	public void removeUser(String key) {
		users.remove(key);
	}
	
	
}
