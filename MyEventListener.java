import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MyEventListener extends ListenerAdapter implements Runnable
{
	private ArrayList<Drafter> drafters = new ArrayList<Drafter>();
	
	//track current player nominated
	private String nom;
	
	//track time left before player sold
	private int timer = -1;
	
	//track current highest bid
	private int bid;
	
	//track uder that is current highest bidder
	private String topBidder;
	
	//track current channel
	private MessageChannel channel;
	
	//track who's turn it is to nominate
	private int turn = 0;
	
	//track direction turn is currently moving
	private boolean turnUp;
	
	//auction modifiable parameters
	private final int minBid = 5000;
	private final int minPlayers = 7;
	private final int maxPlayers = 10;
	private final int timerTop = 20;
	private final int budget = 120000;
	
	private String[] admins = {};
	
	public void populateDrafters()
	{	
		//Add drafters here
		
		turnUp = true;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		try
		{
			Message message = event.getMessage();
			String content = message.getContentRaw();
			
			if(content.startsWith("!auctionHelp"))
			{
				help(event);
			}
			
			if(content.startsWith("!drafted"))
			{
				printDrafted(event);
			}
			
			if(content.startsWith("!skipNom"))
			{
				skip(event);
			}
			
			if(content.startsWith("!nom"))
			{
				nom(event, content);
			}
			if(content.startsWith("!bid"))
			{
				bid(event, content);
			}
			if(content.startsWith("!stopNom"))
			{
				stopNom(event);
			}
			if(content.startsWith("!restartAuction"))
			{
				restartAuction(event);
			}
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	private void help(MessageReceivedEvent event)
	{
		String bidder = event.getAuthor().getName();
		int index = getIndex(bidder);
		if(timer == -1 && index >= 0)
		{
			channel = event.getChannel();
			channel.sendMessage("Command !nom (name): Nominate a player").queue();
			channel.sendMessage("Command !bid (amount): Place a bid").queue();
		}
	}
	
	private void printDrafted(MessageReceivedEvent event)
	{
		String bidder = event.getAuthor().getName();
		int index = getIndex(bidder);
		if(isAdmin(bidder));
			index = 10;
		if(timer == -1 && index >= 0)
		{
			channel = event.getChannel();
			printPoints();
		}
	}
	
	public void skip(MessageReceivedEvent event)
	{
		String bidder = event.getAuthor().getName();
		if(timer == -1 && isAdmin(bidder))
		{
			channel = event.getChannel();
			advanceOrder();
			channel.sendMessage("It is " + drafters.get(turn).getTeamName() + "'s turn to nominate a player!").queue();
		}
	}
	
	private void nom(MessageReceivedEvent event, String content)
	{
		if(timer == -1)
		{
			String bidder = event.getAuthor().getName();
			if(isTeamCap(bidder, turn))
			{
				channel = event.getChannel();
				if(content.contains("@"))
					channel.sendMessage("Do not @ players in your nom!").queue();
				else
				{
					nom = content.substring(5);
					topBidder = event.getAuthor().getName();
					channel.sendMessage(content.substring(5) + " has been nominated!").queue();
					timer = timerTop;
					bid = minBid;
				}
			}
		}
	}
	
	private void bid(MessageReceivedEvent event, String content)
	{
		if(timer != -1)
		{
			String bidder = event.getAuthor().getName();
			int index = getIndex(bidder);
			if(index >= 0)
			{
				channel = event.getChannel();
				
				//convert "k"s to 1000s
				String transTemp = content.substring(5);
				if(transTemp.contains("."))
					transTemp = transTemp.replaceAll(".5k", "500");
				transTemp = transTemp.replaceAll("k", "000");
				int temp = Integer.parseInt(transTemp);
				
				if(temp % 500 == 0 && temp > bid && isLegal(index, temp))
				{
					bid = temp;
					timer = timerTop;
					topBidder = event.getAuthor().getName();
					String stringBid = Integer.toString(bid);
					stringBid = stringBid.replaceAll("500$", ".5k");
					stringBid = stringBid.replaceAll("000$", "k");
					channel.sendMessage(drafters.get(index).getTeamName() + " upped the bid to " + stringBid + "!").queue();
				}
				else
					channel.sendMessage("Bid is illegal!").queue();
			}
		}
	}
	
	private void stopNom(MessageReceivedEvent event)
	{
		String bidder = event.getAuthor().getName();
		if(timer != -1 && isAdmin(bidder))
		{
			channel = event.getChannel();
			channel.sendMessage("Stopped").queue();
			reset();
		}
	}
	
	private void restartAuction(MessageReceivedEvent event)
	{
		String bidder = event.getAuthor().getName();
		if(timer == -1 && isAdmin(bidder))
		{
			channel = event.getChannel();
			channel.sendMessage("Restarting").queue();
			restart();
		}
	}
	
	public void printPoints()
	{
		if(!checkIfOver())
		{
			String temp = "------------------------------------------------------------------------ \n";
			for(Drafter d : drafters)
			{
				temp += d.getTeamName() + " has " + d.getPoints() + " remaining and has drafted: ";
				for(int i = 0; i < d.getPlayers(); i++)
				{
					temp += d.getPlayer(i) + ", ";
				}
				temp = temp.substring(0, temp.length() - 2);
				temp += "\n";
			}
			temp += "------------------------------------------------------------------------";
			channel.sendMessage(temp).queue();
		}
		else
		{
			channel.sendMessage("The draft is now over. Here are the final teams: \n").queue();
			String temp = "------------------------------------------------------------------------ \n";
			for(Drafter d : drafters)
			{
				temp += d.getTeamName() + " have " + d.getPoints() + " remaining and has drafted: ";
				for(int i = 0; i < d.getPlayers(); i++)
				{
					temp += d.getPlayer(i) + ", ";
				}
				temp = temp.substring(0, temp.length() - 2);
				temp += "\n";
			}
			temp += "------------------------------------------------------------------------";
			channel.sendMessage(temp).queue();
		}
	}
	
	public boolean isLegal(int index, int bid)
	{
		//ensure bid leaves enough point to reach minimum players but does not exceed max 
		if(((minPlayers - drafters.get(index).getPlayers() - 1) * minBid) + bid > drafters.get(index).getPoints() 
				|| drafters.get(index).getPlayers() >= maxPlayers)
			return false;
		return true;
	}
	
	public int getIndex(String s)
	{
		int i = 0;
		for(Drafter d: drafters)
		{
			if(isTeamCap(s, d))
				return i;
			i++;
		}
		return -1;
	}
	
	public boolean isAdmin(String author)
	{
		for(int i = 0; i < admins.length; i++)
			if(admins[i].equals(author))
				return true;
		return false;
	}
	
	public boolean isTeamCap(String author, int index)
	{
		for(int i = 0; i < drafters.get(index).getCaps().length; i++)
			if(drafters.get(index).getCaps()[i].equals(author))
				return true;
		return false;
	}
	
	public boolean isTeamCap(String author, Drafter d)
	{
		for(int i = 0; i < d.getCaps().length; i++)
			if(d.getCaps()[i].equals(author))
				return true;
		return false;
	}
	
	public void reset()
	{
		timer = -1;
		nom = "";
		bid = 0;
		topBidder = "";
	}
	
	public void restart()
	{
		timer = -1;
		nom = "";
		bid = 0;
		topBidder = "";
		turn = 0;
		
		drafters.clear();
		populateDrafters();
	}
	
	public boolean checkIfOver()
	{
		for(Drafter d : drafters)
			if(d.getPoints() >= minBid && d.getPlayers() < maxPlayers)
				return false;
		return true;
	}
	
	public void advanceOrder()
	{
		if(turnUp == true)
		{
			turn++;
			if(turn == drafters.size())
			{
				turnUp = false;
				turn--;
			}
		}
		else
		{
			turn--;
			if(turn == -1)
			{
				turnUp = true;
				turn = 0;
			}
		}
		if(drafters.get(turn).getPoints() < minBid || drafters.get(turn).getPlayers() >= maxPlayers)
			advanceOrder();
	}
	
	public void run()
	{
		boolean loop = true;
		while(loop)
		{
			if(timer > 0)
			{
				timer--;
				if(timer == 15)
					channel.sendMessage("15 Seconds Remaining!").queue();
				if(timer == 10)
					channel.sendMessage("10 Seconds Remaining!").queue();
				if(timer == 5)
					channel.sendMessage("5 Seconds Remaining!").queue();
				if(timer == 4)
					channel.sendMessage("4 Seconds Remaining!").queue();
				if(timer == 3)
					channel.sendMessage("3 Seconds Remaining!").queue();
				if(timer == 2)
					channel.sendMessage("2 Seconds Remaining!").queue();
				if(timer == 1)
					channel.sendMessage("1 Seconds Remaining!").queue();
				if(timer == 0)
				{
					int index = getIndex(topBidder);
					String stringBid = Integer.toString(bid);
					stringBid = stringBid.replaceAll("500$", ".5k");
					stringBid = stringBid.replaceAll("000$", "k");
					
					channel.sendMessage(nom + " has been sold to " + drafters.get(index).getTeamName() + " for " + stringBid + "!").queue();
					drafters.get(index).setPoints(drafters.get(index).getPoints() - bid);
					drafters.get(index).addPlayer(nom);
					
					reset();
					printPoints();
					if(!checkIfOver())
					{
						advanceOrder();
						channel.sendMessage("It is " + drafters.get(turn).getTeamName() + "'s turn to nominate a player!").queue();
					}
					
				}
			}
			
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();		
			}
		}
	}
}
