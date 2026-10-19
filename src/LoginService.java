public class LoginService{
	public boolean login(String username, String password) {
		if(username.isEmpty() || password.isEmpty())
			return False;

		return true;	
	}
}