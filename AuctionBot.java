import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

public class AuctionBot 
{
	public static void main(String[] args) throws LoginException
	{   
		MyEventListener event = new MyEventListener();
		JDA jda = JDABuilder.createDefault("Token").addEventListeners(event).build();
		event.populateDrafters();
		event.run();
	}
}
