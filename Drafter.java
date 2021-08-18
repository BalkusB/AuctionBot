import java.util.ArrayList;

public class Drafter 
{
	private int points;
	private ArrayList<String> players = new ArrayList<String>();
	private String teamName;
	private String[] caps;
	public Drafter(String team, String[] caps, int budget)
	{
		teamName = team;
		this.caps = caps;
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
	public String[] getCaps() {
		return caps;
	}
}
