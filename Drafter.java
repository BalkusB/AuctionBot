import java.util.ArrayList;

public class Drafter 
{
	private int points;
	private ArrayList<String> players = new ArrayList<String>();
	private String teamName;
	private String cap1;
	public Drafter(String team, String cap, int budget)
	{
		teamName = team;
		cap1 = cap;
		points = budget;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public int getPlayers() {
		return players.size();
	}
	public String getPlayer(int i) {
		return players.get(i);
	}
	public void addPlayer(String player) {
		this.players.add(player);
	}
	public String getTeamName() {
		return teamName;
	}
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	public String getCap1() {
		return cap1;
	}
	public void setCap1(String cap1) {
		this.cap1 = cap1;
	}
}
