package moetune.core;

/**
 * Created by Encode_X on 14-9-14.
 */
public class MoeTuneUser {
	public int userId;
	public String accessToken;
	public String accessTokenSecret;

	public MoeTuneUser(int userId, String accessToken, String accessTokenSecret) {
		this.userId = userId;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
	}
	public MoeTuneUser( String accessToken, String accessTokenSecret) {
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
	}
}
